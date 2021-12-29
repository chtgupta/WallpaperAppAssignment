package chtgupta.wallpaperapp.ui.activity.home

import chtgupta.wallpaperapp.constant.SPAN_GRID
import chtgupta.wallpaperapp.data.Wallpaper
import com.airbnb.mvrx.MavericksState

data class HomeActivityState(

    val wallpapers: MutableList<Wallpaper> = mutableListOf(),
    val span: Int = SPAN_GRID

) : MavericksState