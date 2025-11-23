package dev.mskelton.versly.api

import androidx.compose.runtime.compositionLocalOf
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

const val BASE_URL = "https://versly.mskelton.dev/"

data class TranslationInfo(val id: String, val name: String, val lastUpdated: String)

data class SearchResult(
    val book: String,
    val translationId: String,
    val chapter: String,
    val range: List<String>,
    val relevance: Float? = null,
)

data class SearchResponse(val results: List<SearchResult>)

interface VerslyService {
    @GET("api/translations") suspend fun getTranslations(): Response<List<TranslationInfo>>

    @GET("api/download/{translation}")
    @Streaming
    suspend fun downloadTranslation(
        @Path("translation") translation: String
    ): Response<ResponseBody>

    @GET("api/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("translation") translation: String = "ESV",
    ): Response<SearchResponse>
}

val LocalVerslyService = compositionLocalOf<VerslyService> { error("No VerslyService provided") }
