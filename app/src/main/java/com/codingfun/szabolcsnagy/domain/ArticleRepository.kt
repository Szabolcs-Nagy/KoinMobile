package com.codingfun.szabolcsnagy.domain

import com.codingfun.szabolcsnagy.constants.model.FeedApiResponse
import com.codingfun.szabolcsnagy.constants.model.WikiApiPageData
import com.codingfun.szabolcsnagy.constants.model.WikiApiPrefixSearchResults
import com.codingfun.szabolcsnagy.constants.model.WikiApiSearchResults
import com.codingfun.szabolcsnagy.data.utils.ResponseResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface ArticleRepository {

    suspend fun getPrefixSearchResults(query: String): ResponseResult<WikiApiPrefixSearchResults>
    suspend fun getSearchResults(query: String): ResponseResult<WikiApiSearchResults>
    suspend fun getPageData(query: String): ResponseResult<WikiApiPageData>
    suspend fun getPageContent(title: String): ResponseResult<String>
    suspend fun getRandomResult(): ResponseResult<WikiApiPageData>
    suspend fun getFeed(date: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    ):  ResponseResult<FeedApiResponse>
}