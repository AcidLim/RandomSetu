package com.acid.setu.data.network


import com.acid.setu.data.model.SetuResponse
import okhttp3.OkHttpClient
import okhttp3.Request

class ApiService(
    private val client: OkHttpClient = ApiClient.getClient()
) {

    suspend fun fetchSetuImage(
        r18: Int,
        size: String = "regular"
    ): Result<SetuResponse> {
        return try {
            val url = "https://api.lolicon.app/setu/v2?r18=$r18&size=$size"
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}"))
            }

            val json = response.body?.string()
                ?: return Result.failure(Exception("Empty response"))

            val parsed = SetuResponse.fromJson(json)
                ?: return Result.failure(Exception("Parse error"))

            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}