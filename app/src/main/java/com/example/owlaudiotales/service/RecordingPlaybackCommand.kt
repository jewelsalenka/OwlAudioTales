package com.example.owlaudiotales.service

import android.os.Bundle
import androidx.media3.session.SessionCommand

internal object RecordingPlaybackCommand {
    val begin = SessionCommand("com.example.owlaudiotales.BEGIN_RECORDING", Bundle())
}
