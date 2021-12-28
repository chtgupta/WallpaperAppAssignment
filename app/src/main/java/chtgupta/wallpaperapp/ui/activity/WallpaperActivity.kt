package chtgupta.wallpaperapp.ui.activity

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import chtgupta.wallpaperapp.R
import chtgupta.wallpaperapp.constant.EXTRA_WALLPAPER
import chtgupta.wallpaperapp.data.Wallpaper
import chtgupta.wallpaperapp.ui.activity.ui.theme.WallpaperAppTheme
import chtgupta.wallpaperapp.util.toast
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

data class WallpaperActivityState(val bitmap: Bitmap? = null) : MavericksState

class WallpaperActivityViewModel(initialState: WallpaperActivityState) : MavericksViewModel<WallpaperActivityState>(initialState) {

    fun loadBitmap(url: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val bitmap = bitmapFromUrl(url) ?: return@launch
            setState { copy(bitmap = bitmap) }

        }
    }

}

class WallpaperActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperAppTheme {

                val wallpaper: Wallpaper = intent.getSerializableExtra(EXTRA_WALLPAPER) as Wallpaper
                Interface(wallpaper = wallpaper)
            }
        }
    }
}

suspend fun bitmapFromUrl(url: String): Bitmap? {

    return try {
        BitmapFactory.decodeStream(URL(url).openStream()) ?: return null
    } catch (e: IOException) {
        null
    }

}

@SuppressLint("UnrememberedMutableState")
@Composable
fun Interface(wallpaper: Wallpaper) {

    val viewModel: WallpaperActivityViewModel = mavericksViewModel()
    val state by mutableStateOf(viewModel.collectAsState {it.bitmap})

    val bitmap by rememberSaveable{ mutableStateOf(state) }
    val shouldShowLoading = bitmap.value == null

    if (shouldShowLoading) {
        LoadingOriginal(wallpaper.original!!)
    } else {
        ShowOptions(bitmap.value!!)
    }

}

@Composable
fun ShowOptions(bitmap: Bitmap) {

    val context = LocalContext.current

    Surface(color = MaterialTheme.colors.background) {

        Box {

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentScale = ContentScale.Crop,
                contentDescription = null
                )

            Button(onClick = {

                //todo this is blocking the thread
                val manager = WallpaperManager.getInstance(context)
                manager.setBitmap(bitmap)
                context.toast("Wallpaper set!")

            }) {
                Text(text = stringResource(id = R.string.label_apply_wallpaper))
            }

        }

    }

}

@Composable
fun LoadingOriginal(url: String) {

    Surface(color = MaterialTheme.colors.background) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator()

        }

    }

    val viewModel: WallpaperActivityViewModel = mavericksViewModel()
    viewModel.loadBitmap(url)

}
