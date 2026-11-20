package com.example.owlaudiotales.model

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.example.owlaudiotales.R

enum class BackgroundTrack(
    val id: String,
    @StringRes val title: Int,
    @RawRes val audio: Int
) {
    DEFAULT("default", R.string.background_track_default, R.raw.background_music_00),
    FIRST("01", R.string.background_track_01, R.raw.background_music_01),
    SECOND("02", R.string.background_track_02, R.raw.background_music_02),
    THIRD("03", R.string.background_track_03, R.raw.background_music_03)
}
