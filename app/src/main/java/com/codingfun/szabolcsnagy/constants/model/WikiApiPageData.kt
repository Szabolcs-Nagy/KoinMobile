package com.codingfun.szabolcsnagy.constants.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class WikiApiPageData(
    val query: WikiApiQuery? = null
)

@Serializable
data class WikiApiQuery(
    val pages: List<WikiApiPage>
)

@Serializable
data class WikiApiPage(
    val title: String,
    val extract: String? = null,
   @SerializedName(value = "pageid")
    val pageId: Int? = null,
   @SerializedName(value = "original")
    val photo: WikiPhoto? = null,
   @SerializedName(value = "terms")
    val photoDesc: WikiPhotoDesc? = null,
   @SerializedName(value = "langlinks")
    val langs: List<WikiLang>? = null
)

@Serializable
data class WikiPhoto(
    val source: String,
    val width: Int,
    val height: Int
)

@Serializable
data class WikiPhotoDesc(
    val label: List<String>? = null,
    val description: List<String>? = null
)

@Serializable
data class WikiLang(
    val lang: String,
    val title: String
)
