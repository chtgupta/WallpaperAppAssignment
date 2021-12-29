package chtgupta.wallpaperapp.ui.activity.home

import chtgupta.wallpaperapp.constant.*
import chtgupta.wallpaperapp.data.PexelsResponse
import chtgupta.wallpaperapp.data.Wallpaper
import com.airbnb.mvrx.MavericksViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivityViewModel(initialState: MainActivityState) :
    MavericksViewModel<MainActivityState>(initialState) {

    fun loadWallpapers(client: HttpClient) {

        viewModelScope.launch(Dispatchers.IO) {

            val list = getPhotos(client) ?: return@launch
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