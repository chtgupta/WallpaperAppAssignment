package chtgupta.wallpaperapp.ui.activity.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.airbnb.mvrx.MavericksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

class PreviewActivityViewModel(initialState: PreviewActivityState) : MavericksViewModel<PreviewActivityState>(initialState) {

    fun loadBitmap(url: String) {

        viewModelScope.launch(Dispatchers.IO) {

            val bitmap = bitmapFromUrl(url) ?: return@launch
            setState { copy(bitmap = bitmap) }

        }
    }

    private suspend fun bitmapFromUrl(url: String): Bitmap? {

        return try {
            BitmapFactory.decodeStream(URL(url).openStream()) ?: return null
        } catch (e: IOException) {
            null
        }

    }

}