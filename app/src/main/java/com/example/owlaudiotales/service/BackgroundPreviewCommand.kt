package com.example.owlaudiotales.service

import android.os.Bundle
import androidx.media3.session.SessionCommand

internal object BackgroundPreviewCommand {
    val command = SessionCommand("com.example.owlaudiotales.PREVIEW_BACKGROUND", Bundle())
    const val TRACK_ID = "track_id"
}
