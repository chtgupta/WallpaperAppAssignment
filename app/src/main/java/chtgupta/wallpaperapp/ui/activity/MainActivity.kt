package chtgupta.wallpaperapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import chtgupta.wallpaperapp.R
import chtgupta.wallpaperapp.constant.*
import chtgupta.wallpaperapp.data.PexelsResponse
import chtgupta.wallpaperapp.data.Wallpaper
import chtgupta.wallpaperapp.ui.theme.Purple700
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
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

data class MainActivityState(val wallpapers: MutableList<Wallpaper> = mutableListOf(), val span: Int = SPAN_GRID) : MavericksState

class MainActivityViewModel(initialState: MainActivityState) : MavericksViewModel<MainActivityState>(initialState) {

    fun loadWallpapers(client: HttpClient) {

        viewModelScope.launch(Dispatchers.IO) {

            val list = getPhotos(client) ?: return@launch
            /*Log.d(MainActivity.TAG, "loadWallpapers: received ${list.size} wallpapers")*/
            setState { copy(wallpapers = list) }

        }
    }

    fun setGrid() {
        setState { copy(span = SPAN_GRID) }
    }

    fun setList() {
        setState { copy(span = SPAN_LIST) }
    }

    private suspend fun getPhotos(client: HttpClient): MutableList<Wallpaper>? {

        /*Log.d(MainActivity.TAG, "getPhotos: sending request to $PHOTOS_URL")*/

        /*val client = HttpClient(Android)*/

        val response = try {
            client.get<String>(PHOTOS_URL) {
                this.headers["Authorization"] = AUTH_KEY
            }
        } catch (e: Exception) {

            // Pexels API was giving random 500 status codes this morning so I added this fallback JSON
            FALLBACK_JSON
        }

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(PexelsResponse::class.java)
        val pexelsResponse = adapter.fromJson(response)

        return pexelsResponse?.wallpapers
    }

}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivityAPI"
    }

    @Inject
    lateinit var client: HttpClient

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WallpaperAppTheme {
                MainInterface()
            }
        }
    }


    @ExperimentalMaterialApi
    @SuppressLint("UnrememberedMutableState")
    @ExperimentalFoundationApi
    @Composable
    fun MainInterface() {

        val viewModel: MainActivityViewModel = mavericksViewModel()
        val listState by mutableStateOf(viewModel.collectAsState { it.wallpapers })

        val list by rememberSaveable { mutableStateOf(listState) }
        val shouldShowLoading = list.value.isEmpty()

        /*Log.d(MainActivity.TAG, "Interface: fired! list size: ${listState.value.size} ")*/

        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(color = Purple700),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = stringResource(R.string.title_home),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

            }

            if (shouldShowLoading) {
                LoadingInterface()
            } else {
                WallpapersInterface(list = listState.value)
            }

        }

    }

    @Composable
    fun LoadingInterface() {

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
        viewModel.loadWallpapers(client)

    }

    @SuppressLint("UnrememberedMutableState")
    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @Composable
    fun WallpapersInterface(list: List<Wallpaper>) {

        val context = LocalContext.current
        val viewModel: MainActivityViewModel = mavericksViewModel()
        val spanState by mutableStateOf(viewModel.collectAsState { it.span })

        Surface(color = MaterialTheme.colors.background) {

            Box(modifier = Modifier.fillMaxSize()) {

                LazyVerticalGrid(
                    cells = GridCells.Fixed(spanState.value), modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp)
                ) {

                    items(list.size) { index ->

                        Card(modifier = Modifier.padding(top = 16.dp, start = 16.dp), onClick = {

                            context.startActivity(
                                Intent(context, WallpaperActivity::class.java)
                                    .putExtra(EXTRA_WALLPAPER, list[index])
                            )

                        }) {

                            GlideImage(
                                imageModel = list[index].portrait,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                        }

                    }

                }

                FloatingActionButton(
                    onClick = { if (spanState.value == SPAN_GRID) viewModel.setList() else viewModel.setGrid() },
                    modifier = Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .padding(24.dp)
                ) {
                    Icon(Icons.Filled.List, null)
                }

            }

        }

    }

}