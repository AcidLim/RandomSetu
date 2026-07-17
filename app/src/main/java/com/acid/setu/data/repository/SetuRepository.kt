package com.acid.setu.data.repository

import com.acid.setu.data.network.ApiService

class SetuRepository(
    private val apiService: ApiService = ApiService()
) {

    suspend fun fetchImageUrl(r18: Int, size: String = "regular"): String? {
        val result = apiService.fetchSetuImage(r18, size)
        if (result.isFailure) return null

        val response = result.getOrNull() ?: return null
        if (response.data.isEmpty()) return null

        return when (size) {
            "original" -> response.data[0].urls.original
            "regular"  -> response.data[0].urls.regular
            "small"    -> response.data[0].urls.small
            "thumb"    -> response.data[0].urls.thumb
            "mini"     -> response.data[0].urls.mini
            else       -> response.data[0].urls.regular
        }.takeIf { it.isNotEmpty() }
    }
}