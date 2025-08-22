package com.codingfun.szabolcsnagy.data

import com.codingfun.szabolcsnagy.constants.api.API_QUERY
import com.codingfun.szabolcsnagy.constants.api.CONTENT_QUERY
import com.codingfun.szabolcsnagy.constants.api.FEED_QUERY
import com.codingfun.szabolcsnagy.constants.api.PREFIX_SEARCH_QUERY
import com.codingfun.szabolcsnagy.constants.api.RANDOM_QUERY
import com.codingfun.szabolcsnagy.constants.api.SEARCH_QUERY
import com.codingfun.szabolcsnagy.data.model.ResponseError
import com.codingfun.szabolcsnagy.data.utils.NetworkResponse
import com.codingfun.szabolcsnagy.constants.model.FeedApiResponse
import com.codingfun.szabolcsnagy.constants.model.WikiApiPageData
import com.codingfun.szabolcsnagy.constants.model.WikiApiPrefixSearchResults
import com.codingfun.szabolcsnagy.constants.model.WikiApiSearchResults
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArticleApiService {

    @GET(PREFIX_SEARCH_QUERY)
    suspend fun getPrefixSearchResults(@Query("gpssearch") query: String): NetworkResponse<WikiApiPrefixSearchResults, ResponseError>

    @GET(SEARCH_QUERY)
    suspend fun getFlickrSearchResults(@Query("gsrsearch") query: String): NetworkResponse<WikiApiSearchResults, ResponseError>

    @GET(API_QUERY)
    suspend fun getPageData(@Query("titles") query: String): NetworkResponse<WikiApiPageData, ResponseError>

    @GET(CONTENT_QUERY)
    suspend fun getPageContent(@Path("title", encoded = true) title: String): NetworkResponse<String, ResponseError>

    @GET(RANDOM_QUERY)
    suspend fun getRandomResult(): NetworkResponse<WikiApiPageData, ResponseError>

    @GET(FEED_QUERY)
    suspend fun getFeed(@Path("date", encoded = true) date: String): NetworkResponse<FeedApiResponse, ResponseError>
}