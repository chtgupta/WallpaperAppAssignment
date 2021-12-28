package chtgupta.wallpaperapp.data

import com.squareup.moshi.Json

data class PexelsResponse(@Json(name = "photos") val wallpapers: MutableList<Wallpaper>)
