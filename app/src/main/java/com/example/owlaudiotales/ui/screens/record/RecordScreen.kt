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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.io.File

@Composable
fun RecordScreen() {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val viewModel: RecordViewModel = hiltViewModel()

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showMetadataDialog by remember { mutableStateOf(false) }
    var lastFilePath by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.onEvent(RecordEvent.StartRecording)
            } else {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity ?: return@rememberLauncherForActivityResult,
                    Manifest.permission.RECORD_AUDIO
                )
                if (!showRationale) {
                    showPermissionDialog = true
                } else {
                    Toast.makeText(context, "Дозвіл на мікрофон не надано", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val state by viewModel.state.collectAsState()

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
                        Text("Пауза")
                    }
                    Button(onClick = {
                        lastFilePath = state.filePath
                        viewModel.onEvent(RecordEvent.StopRecording)
                        showMetadataDialog = true
                    }) {
                        Text("Зупинити")
                    }
                }
            }
            state.isRecording && state.isPaused -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { viewModel.onEvent(RecordEvent.ResumeRecording) }) {
                        Text("Продовжити")
                    }
                    Button(onClick = {
                        lastFilePath = state.filePath
                        viewModel.onEvent(RecordEvent.StopRecording)
                        showMetadataDialog = true
                    }) {
                        Text("Зупинити")
                    }
                }
            }
            else -> {
                Button(onClick = {
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Записати")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.filePath != null && !state.isRecording) {
            Text("Файл збережено: ${state.filePath}")
        }

        state.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        if (showPermissionDialog) {
            RecordPermissionDialog(context = context) {
                showPermissionDialog = false
            }
        }
        if (showMetadataDialog && lastFilePath != null) {
            RecordMetadataDialog(
                onDismiss = {
                    lastFilePath?.let { File(it).delete() }
                    showMetadataDialog = false
                },
                onSave = { title, author, coverPath ->
                    Toast.makeText(context, "Казку $title збережено", Toast.LENGTH_SHORT).show()
                    viewModel.saveMetadata(title, author, coverPath)
                    showMetadataDialog = false
                }
            )
        }
    }
}
