package chtgupta.wallpaperapp.ui.activity.preview

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chtgupta.wallpaperapp.R
import chtgupta.wallpaperapp.constant.EXTRA_WALLPAPER
import chtgupta.wallpaperapp.data.Wallpaper
import chtgupta.wallpaperapp.ui.activity.preview.PreviewActivityViewModel
import chtgupta.wallpaperapp.ui.theme.WallpaperAppTheme
import chtgupta.wallpaperapp.util.toast
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

class PreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperAppTheme {

                val wallpaper: Wallpaper = intent.getSerializableExtra(EXTRA_WALLPAPER) as Wallpaper
                MainInterface(wallpaper = wallpaper)
            }
        }
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun MainInterface(wallpaper: Wallpaper) {

        val viewModel: PreviewActivityViewModel = mavericksViewModel()
        val state by mutableStateOf(viewModel.collectAsState {it.bitmap})

        val bitmap by rememberSaveable{ mutableStateOf(state) }
        val shouldShowLoading = bitmap.value == null

        if (shouldShowLoading) {
            LoadingInterface(wallpaper.original!!)
        } else {
            OptionsInterface(bitmap.value!!)
        }

    }

    @Composable
    fun OptionsInterface(bitmap: Bitmap) {

        val context = LocalContext.current

        Surface(color = MaterialTheme.colors.background) {

            Box (modifier = Modifier.fillMaxSize()) {

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )

                OutlinedButton(onClick = {

                    val manager = WallpaperManager.getInstance(context)
                    manager.setBitmap(bitmap)
                    context.toast(R.string.message_wallpaper_set)

                }, modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(24.dp)
                    .fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.label_apply_wallpaper), fontSize = 18.sp, modifier = Modifier.padding(vertical = 12.dp))
                }

            }

        }

    }

    @Composable
    fun LoadingInterface(url: String) {

        Surface(color = MaterialTheme.colors.background) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CircularProgressIndicator()

                Text(
                    text = stringResource(id = R.string.message_downloading),
                    modifier = Modifier.padding(top = 24.dp)
                )

            }

        }

        val viewModel: PreviewActivityViewModel = mavericksViewModel()
        viewModel.loadBitmap(url)

    }

}
