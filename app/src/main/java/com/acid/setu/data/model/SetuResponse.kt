package com.acid.setu.data.model

import org.json.JSONObject

data class SetuResponse(
    val error: String,
    val data: List<SetuData>
) {
    companion object {
        fun fromJson(json: String): SetuResponse? {
            return try {
                val obj = JSONObject(json)
                val dataArray = obj.optJSONArray("data")
                val dataList = mutableListOf<SetuData>()

                if (dataArray != null) {
                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        val urlsObj = item.optJSONObject("urls")
                        val urls = SetuUrls(
                            original = urlsObj?.optString("original") ?: "",
                            regular = urlsObj?.optString("regular") ?: "",
                            small = urlsObj?.optString("small") ?: "",
                            thumb = urlsObj?.optString("thumb") ?: "",
                            mini = urlsObj?.optString("mini") ?: ""
                        )
                        dataList.add(
                            SetuData(
                                pid = item.optInt("pid"),
                                title = item.optString("title"),
                                author = item.optString("author"),
                                urls = urls
                            )
                        )
                    }
                }

                SetuResponse(
                    error = obj.optString("error"),
                    data = dataList
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class SetuData(
    val pid: Int,
    val title: String,
    val author: String,
    val urls: SetuUrls
)

data class SetuUrls(
    val original: String,
    val regular: String,
    val small: String,
    val thumb: String,
    val mini: String
)