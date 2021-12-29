package chtgupta.wallpaperapp.ui.activity.home

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
import chtgupta.wallpaperapp.data.Wallpaper
import chtgupta.wallpaperapp.ui.activity.preview.PreviewActivity
import chtgupta.wallpaperapp.ui.theme.Purple700
import chtgupta.wallpaperapp.ui.theme.WallpaperAppTheme
import io.ktor.client.*
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.skydoves.landscapist.glide.GlideImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
                                Intent(context, PreviewActivity::class.java)
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
