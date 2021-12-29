package chtgupta.wallpaperapp.ui.activity.preview

import android.graphics.Bitmap
import com.airbnb.mvrx.MavericksState

data class PreviewActivityState(val bitmap: Bitmap? = null) : MavericksState