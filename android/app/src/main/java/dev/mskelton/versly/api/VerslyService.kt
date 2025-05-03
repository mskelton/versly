package dev.mskelton.versly.api

import androidx.compose.runtime.compositionLocalOf
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface VerslyService {
    @GET("download/{translation}")
    @Streaming
    suspend fun downloadTranslation(@Path("translation") translation: String): Response<ResponseBody>
}

val LocalVerslyService = compositionLocalOf<VerslyService> {
    error("No VerslyService provided")
}
