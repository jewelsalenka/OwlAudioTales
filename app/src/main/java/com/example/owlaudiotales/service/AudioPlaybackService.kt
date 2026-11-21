package com.example.owlaudiotales.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import com.example.owlaudiotales.R
import com.example.owlaudiotales.datastore.SettingsDataStore
import com.example.owlaudiotales.model.BackgroundTrack
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class AudioPlaybackService : MediaSessionService() {

    private companion object {
        const val ACTION_STOP = "com.example.owlaudiotales.STOP"
        val STOP_COMMAND = SessionCommand(ACTION_STOP, Bundle())
        val RECORDING_PLAYER_COMMANDS = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
            .buildUpon()
            .remove(Player.COMMAND_PLAY_PAUSE)
            .build()
    }

    @Inject lateinit var settingsDataStore: SettingsDataStore

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate)
    private var mediaSession: MediaSession? = null
    private var recordingController: MediaSession.ControllerInfo? = null
    private var backgroundPlayer: ExoPlayer? = null
    private var backgroundTrack: BackgroundTrack? = null
    private var backgroundVolume = 0
    private var previewTrack: BackgroundTrack? = null
    private var previewJob: Job? = null
    private var sleepTimerSeconds = 0
    private var sleepTimerJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(true)
            .setPauseAtEndOfMediaItems(true)
            .build()

        var autoplayNext = false
        player.addListener(object : Player.Listener {
            override fun onRepeatModeChanged(repeatMode: Int) {
                player.setPauseAtEndOfMediaItems(!autoplayNext && repeatMode != Player.REPEAT_MODE_ONE)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateBackgroundMusic(isPlaying)
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.containsAny(
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED
                    )
                ) {
                    updateSleepTimer(player)
                }
            }
        })

        serviceScope.launch {
            settingsDataStore.settingsFlow
                .map { settings ->
                    val timerSeconds = if (settings.sleepTimerEnabled) settings.sleepTimerSeconds else 0
                    settings.autoplayNext to timerSeconds
                }
                .distinctUntilChanged()
                .collect { (autoplayEnabled, timerSeconds) ->
                    autoplayNext = autoplayEnabled
                    player.setPauseAtEndOfMediaItems(
                        !autoplayNext && player.repeatMode != Player.REPEAT_MODE_ONE
                    )
                    if (sleepTimerSeconds != timerSeconds) {
                        sleepTimerSeconds = timerSeconds
                        sleepTimerJob?.cancel()
                        sleepTimerJob = null
                        updateSleepTimer(player)
                    }
                }
        }

        serviceScope.launch {
            settingsDataStore.settingsFlow
                .map { settings ->
                    settings.backgroundTrack.takeIf { settings.backgroundMusic } to
                        settings.backgroundVolume
                }
                .distinctUntilChanged()
                .collect { (track, volume) ->
                    backgroundTrack = track
                    backgroundVolume = volume
                    if (track == null) {
                        previewJob?.cancel()
                        previewTrack = null
                    }
                    updateBackgroundMusic(player.isPlaying)
                }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                @OptIn(UnstableApi::class)
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    return MediaSession.ConnectionResult.accept(
                        sessionCommands(controller),
                        availablePlayerCommands()
                    )
                }

                override fun onDisconnected(session: MediaSession, controller: MediaSession.ControllerInfo) {
                    if (recordingController == controller) {
                        recordingController = null
                        refreshControllerCommands(session)
                    }
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    if (customCommand.customAction == ACTION_STOP) {
                        previewBackground(null, session.player)
                        session.player.stop()
                        stopSelf()
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    if (customCommand == BackgroundPreviewCommand.command &&
                        controller.uid == applicationInfo.uid
                    ) {
                        if (recordingController != null) {
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_INVALID_STATE))
                        }
                        val track = BackgroundTrack.entries.firstOrNull {
                            it.id == args.getString(BackgroundPreviewCommand.TRACK_ID)
                        }
                        previewBackground(track, session.player)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    if (customCommand == RecordingPlaybackCommand.begin && controller.uid == applicationInfo.uid) {
                        if (recordingController != null && recordingController != controller) {
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_INVALID_STATE))
                        }
                        recordingController = controller
                        refreshControllerCommands(session)
                        session.player.pause()
                        previewBackground(null, session.player)
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return super.onCustomCommand(session, controller, customCommand, args)
                }
            })
            .build()

        mediaSession?.setCustomLayout(
            listOf(
                CommandButton.Builder(CommandButton.ICON_STOP)
                    .setDisplayName(getString(R.string.action_stop))
                    .setSessionCommand(STOP_COMMAND)
                    .build()
            )
        )
    }

    private fun sessionCommands(controller: MediaSession.ControllerInfo): SessionCommands {
        val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().add(STOP_COMMAND)
        if (controller.uid == applicationInfo.uid) {
            commands.add(BackgroundPreviewCommand.command)
            commands.add(RecordingPlaybackCommand.begin)
        }
        return commands.build()
    }

    private fun availablePlayerCommands(): Player.Commands =
        if (recordingController == null) {
            MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
        } else {
            RECORDING_PLAYER_COMMANDS
        }

    private fun refreshControllerCommands(session: MediaSession) {
        val playerCommands = availablePlayerCommands()
        session.connectedControllers.forEach { controller ->
            session.setAvailableCommands(controller, sessionCommands(controller), playerCommands)
        }
    }

    private fun previewBackground(track: BackgroundTrack?, storyPlayer: Player) {
        previewJob?.cancel()
        previewTrack = track
        if (track != null) {
            backgroundPlayer?.seekTo(0)
            previewJob = serviceScope.launch {
                delay(10.seconds)
                previewTrack = null
                updateBackgroundMusic(storyPlayer.isPlaying)
            }
        }
        updateBackgroundMusic(storyPlayer.isPlaying)
    }

    private fun updateBackgroundMusic(isStoryPlaying: Boolean) {
        val track = if (recordingController == null) {
            previewTrack ?: backgroundTrack?.takeIf { isStoryPlaying }
        } else null
        if (track == null) {
            backgroundPlayer?.pause()
            return
        }

        val player = backgroundPlayer ?: ExoPlayer.Builder(this).build().also {
            it.repeatMode = Player.REPEAT_MODE_ONE
            backgroundPlayer = it
        }
        player.volume = backgroundVolume / 100f
        if (player.currentMediaItem?.mediaId != track.id) {
            val uri = Uri.parse("android.resource://$packageName/${track.audio}")
            player.setMediaItem(MediaItem.Builder().setMediaId(track.id).setUri(uri).build())
            player.prepare()
        }
        player.play()
    }

    private fun updateSleepTimer(player: Player) {
        val isPlaybackRequested = player.playWhenReady &&
            (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING)

        if (sleepTimerSeconds <= 0 || !isPlaybackRequested) {
            sleepTimerJob?.cancel()
            sleepTimerJob = null
        } else if (sleepTimerJob == null) {
            sleepTimerJob = serviceScope.launch {
                delay(sleepTimerSeconds.seconds)
                sleepTimerJob = null
                previewBackground(null, player)
                player.pause()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        previewTrack = null
        backgroundTrack = null
        backgroundPlayer?.release()
        backgroundPlayer = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
