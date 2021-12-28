package chtgupta.wallpaperapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import chtgupta.wallpaperapp.R
import chtgupta.wallpaperapp.constant.AUTH_KEY
import chtgupta.wallpaperapp.constant.EXTRA_WALLPAPER
import chtgupta.wallpaperapp.constant.PHOTOS_URL
import chtgupta.wallpaperapp.data.PexelsResponse
import chtgupta.wallpaperapp.data.Wallpaper
import chtgupta.wallpaperapp.ui.theme.WallpaperAppTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.airbnb.mvrx.*
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.skydoves.landscapist.glide.GlideImage

data class MainActivityState(val wallpapers: MutableList<Wallpaper> = mutableListOf()) : MavericksState

class MainActivityViewModel(initialState: MainActivityState) : MavericksViewModel<MainActivityState>(initialState) {

    fun loadWallpapers() {
        viewModelScope.launch(Dispatchers.IO) {

            val list = getPhotos() ?: return@launch
            /*Log.d(MainActivity.TAG, "loadWallpapers: received ${list.size} wallpapers")*/
            setState { copy(wallpapers = list) }

        }
    }

}

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivityAPI"
    }

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperAppTheme {
                Interface()
            }
        }
    }

}

private suspend fun getPhotos(): MutableList<Wallpaper>? {

    Log.d(MainActivity.TAG, "getPhotos: sending request to $PHOTOS_URL")

    val client = HttpClient(Android)
    val response = client.get<String>(PHOTOS_URL) {
        this.headers["Authorization"] = AUTH_KEY
    }

    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(PexelsResponse::class.java)
    val pexelsResponse = adapter.fromJson(response)

    return pexelsResponse?.wallpapers
}

@ExperimentalMaterialApi
@SuppressLint("UnrememberedMutableState")
@ExperimentalFoundationApi
@Composable
fun Interface() {

    val viewModel: MainActivityViewModel = mavericksViewModel()
    val state by mutableStateOf(viewModel.collectAsState {it.wallpapers})

    val list by rememberSaveable{ mutableStateOf(state) }
    val shouldShowLoading = list.value.isEmpty()

    Log.d(MainActivity.TAG, "Interface: fired! list size: ${list.value.size} ")

    if (shouldShowLoading) {
        Loading()
    } else {
        Wallpapers(list = list.value)
    }

}

@Composable
fun Loading() {

    Surface(color = MaterialTheme.colors.background) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator()
            Text(
                text = stringResource(id = R.string.message_loading),
                modifier = Modifier.padding(top = 24.dp)
            )

        }

    }

    val viewModel: MainActivityViewModel = mavericksViewModel()
    viewModel.loadWallpapers()

}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun Wallpapers(list: List<Wallpaper>) {

    val context = LocalContext.current

    Surface(color = MaterialTheme.colors.background) {

        LazyVerticalGrid(cells = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {

            items(list.size) { index ->

                Card(modifier = Modifier.padding(top = 16.dp, start = 16.dp), onClick = {

                    context.startActivity(Intent(context, WallpaperActivity::class.java)
                        .putExtra(EXTRA_WALLPAPER, list[index]))

                }) {

                    GlideImage(
                        imageModel = list[index].small,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                }

            }

        }

    }

}