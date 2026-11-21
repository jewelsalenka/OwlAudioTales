package com.example.owlaudiotales.ui.screens.record

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.owlaudiotales.R
import com.example.owlaudiotales.ui.components.SecondaryScreenTopBar

@Composable
fun RecordScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: RecordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(RecordEvent.ClearError)
        }
    }

    LaunchedEffect(state.savedTitle) {
        state.savedTitle?.let { title ->
            Toast.makeText(
                context,
                context.getString(R.string.fairy_tale_saved_format, title),
                Toast.LENGTH_SHORT
            ).show()
            onBack()
        }
    }

    var showLeaveConfirmation by rememberSaveable { mutableStateOf(false) }
    val hasActiveRecording = state.isStarting || state.isRecording
    val requestBack: () -> Unit = {
        when {
            hasActiveRecording -> showLeaveConfirmation = true
            !state.isSaving -> onBack()
        }
    }
    BackHandler(enabled = hasActiveRecording || state.isSaving, onBack = requestBack)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.onEvent(RecordEvent.StartRecording)
            } else {
                val canShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity ?: return@rememberLauncherForActivityResult,
                    Manifest.permission.RECORD_AUDIO
                )
                viewModel.onEvent(RecordEvent.PermissionDenied(canShowRationale))
            }
        }
    )

    Scaffold(
        topBar = {
            SecondaryScreenTopBar(stringResource(R.string.create_recording), onBack = requestBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isRecording) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = {
                        viewModel.onEvent(
                            if (state.isPaused) RecordEvent.ResumeRecording else RecordEvent.PauseRecording
                        )
                    }) {
                        Text(stringResource(if (state.isPaused) R.string.record_resume else R.string.record_pause))
                    }
                    Button(onClick = { viewModel.onEvent(RecordEvent.StopRecording) }) {
                        Text(stringResource(R.string.record_stop))
                    }
                }
            } else {
                Button(
                    enabled = !state.isStarting && state.filePath == null && state.savedTitle == null,
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                ) {
                    Text(stringResource(if (state.isStarting) R.string.record_preparing else R.string.record_start))
                }
            }
        }
    }

    if (state.showPermissionSettings) {
        RecordPermissionDialog(
            context = context,
            onDismiss = { viewModel.onEvent(RecordEvent.DismissPermissionSettings) }
        )
    }

    if (!state.isRecording && state.filePath != null) {
        RecordMetadataDialog(
            isSaving = state.isSaving,
            onDismiss = { viewModel.onEvent(RecordEvent.DiscardRecording) },
            onSave = viewModel::saveMetadata
        )
    }

    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text(stringResource(R.string.discard_active_recording)) },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirmation = false
                    viewModel.onEvent(RecordEvent.DiscardRecording)
                    onBack()
                }) {
                    Text(stringResource(R.string.discard_recording))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
