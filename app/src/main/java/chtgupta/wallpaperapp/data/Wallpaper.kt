package chtgupta.wallpaperapp.data

import com.squareup.moshi.Json
import java.io.Serializable

data class Wallpaper(@Json(name = "src") val sources: Map<String, String>) : Serializable {

    val original = sources["medium"]
    val small = sources["small"]
    val portrait = sources["portrait"]

}
