package com.example.owlaudiotales.ui.screens.record

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.owlaudiotales.R

@Composable
fun RecordScreen() {
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

    var showMetadataDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isRecording && !state.isPaused -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { viewModel.onEvent(RecordEvent.PauseRecording) }) {
                        Text(stringResource(R.string.record_pause))
                    }
                    Button(onClick = {
                        viewModel.onEvent(RecordEvent.StopRecording)
                        showMetadataDialog = true
                    }) {
                        Text(stringResource(R.string.record_stop))
                    }
                }
            }
            state.isRecording && state.isPaused -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { viewModel.onEvent(RecordEvent.ResumeRecording) }) {
                        Text(stringResource(R.string.record_resume))
                    }
                    Button(onClick = {
                        viewModel.onEvent(RecordEvent.StopRecording)
                        showMetadataDialog = true
                    }) {
                        Text(stringResource(R.string.record_stop))
                    }
                }
            }
            else -> {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text(stringResource(R.string.record_start))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.filePath != null && !state.isRecording) {
            Text(stringResource(R.string.file_saved_format, state.filePath!!))
        }
    }

    if (state.showPermissionSettings) {
        RecordPermissionDialog(
            context = context,
            onDismiss = { viewModel.onEvent(RecordEvent.DismissPermissionSettings) }
        )
    }

    if (showMetadataDialog) {
        RecordMetadataDialog(
            onDismiss = {
                viewModel.onEvent(RecordEvent.DiscardRecording)
                showMetadataDialog = false
            },
            onSave = { title, author, coverPath ->
                Toast.makeText(context, context.getString(R.string.fairy_tale_saved_format, title), Toast.LENGTH_SHORT).show()
                viewModel.saveMetadata(title, author, coverPath)
                showMetadataDialog = false
            }
        )
    }
}
