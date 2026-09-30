package dev.nico.overview.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

fun interface HttpClient {
    suspend fun get(url: String): String
}

private const val CALL_TIMEOUT_S = 15L

class OkHttpClientAdapter(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(CALL_TIMEOUT_S, TimeUnit.SECONDS)
        .build(),
) : HttpClient {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            response.body.string()
        }
    }
}
