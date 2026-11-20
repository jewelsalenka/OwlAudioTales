package com.example.owlaudiotales.model

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.example.owlaudiotales.R

enum class BackgroundTrack(
    val id: String,
    @StringRes val title: Int,
    @RawRes val audio: Int
) {
    DEFAULT("default", R.string.background_track_default, R.raw.background_music)
}
