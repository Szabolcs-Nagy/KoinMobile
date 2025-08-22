package com.codingfun.szabolcsnagy.constants.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class WikiApiSearchResults(
    val query: WikiSearchResultsQuery = WikiSearchResultsQuery()
)

@Serializable
data class WikiSearchResultsQuery(
    val pages: List<WikiSearchResult> = emptyList()
)

@Serializable
data class WikiSearchResult(
    val ns: Int = 0,
    val title: String,
   @SerializedName("titlesnippet")
    val titleSnippet: String,
   @SerializedName("pageid")
    val pageId: Int,
    val snippet: String,
    val index: Int,
   @SerializedName("redirecttitle")
    val redirectTitle: String? = null,
    val thumbnail: WikiPhoto? = null
)

@Serializable
data class WikiApiPrefixSearchResults(
    val query: WikiPrefixSearchResultsQuery = WikiPrefixSearchResultsQuery()
)

@Serializable
data class WikiPrefixSearchResultsQuery(
    val pages: List<WikiPrefixSearchResult> = emptyList()
)

@Serializable
data class WikiPrefixSearchResult(
   @SerializedName("pageid")
    val pageId: Int,
    val ns: Int = 0,
    val title: String,
    val index: Int,
    val thumbnail: WikiPhoto? = null,
    val terms: WikiPrefixSearchPageTerms? = null
)

@Serializable
data class WikiPrefixSearchPageTerms(
    val description: List<String>
)