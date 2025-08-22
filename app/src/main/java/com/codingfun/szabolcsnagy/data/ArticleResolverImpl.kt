package com.codingfun.szabolcsnagy.data

import com.codingfun.szabolcsnagy.data.utils.NetworkResponse
import com.codingfun.szabolcsnagy.constants.model.FeedApiResponse
import com.codingfun.szabolcsnagy.constants.model.WikiApiPageData
import com.codingfun.szabolcsnagy.constants.model.WikiApiPrefixSearchResults
import com.codingfun.szabolcsnagy.constants.model.WikiApiSearchResults
import com.codingfun.szabolcsnagy.domain.ArticleResolver
import com.codingfun.szabolcsnagy.data.model.ResponseError
import com.codingfun.szabolcsnagy.data.utils.ResponseResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ArticleResolverImpl(
    private val wikipediaArticleApiService: ArticleApiService,
    private val ioDispatcher: CoroutineDispatcher
) : ArticleResolver {
    override suspend fun getPrefixSearchResults(query: String): ResponseResult<WikiApiPrefixSearchResults> =
        withContext(ioDispatcher) {
            val response = wikipediaArticleApiService.getPrefixSearchResults(query)
            return@withContext getResponseResult(response)
        }

    override suspend fun getSearchResults(query: String): ResponseResult<WikiApiSearchResults> =
        withContext(ioDispatcher) {
            val response = wikipediaArticleApiService.getFlickrSearchResults(query)
            return@withContext getResponseResult(response)
        }

    override suspend fun getPageData(query: String): ResponseResult<WikiApiPageData> =
        withContext(ioDispatcher) {
            val response = wikipediaArticleApiService.getPageData(query)
            return@withContext getResponseResult(response)
        }

    override suspend fun getPageContent(title: String): ResponseResult<String> =
        withContext(ioDispatcher) {
            val response = wikipediaArticleApiService.getPageContent(title)
            return@withContext getResponseResult(response)
        }

    override suspend fun getRandomResult(): ResponseResult<WikiApiPageData> =
        withContext(ioDispatcher) {
            val response = wikipediaArticleApiService.getRandomResult()
            return@withContext getResponseResult(response)
        }

    override suspend fun getFeed(date: String): ResponseResult<FeedApiResponse> =
        withContext(ioDispatcher) {
            val response = wikipediaArticleApiService.getFeed(date)
            return@withContext getResponseResult(response)
        }

    private fun <T : Any> getResponseResult(response: NetworkResponse<T, ResponseError>) =
        when (response) {
            is NetworkResponse.Success -> {
                ResponseResult.Success(response.body)
            }

            is NetworkResponse.ApiError -> {
                val message = response.body.statusMessage
                ResponseResult.Error(message)
            }

            is NetworkResponse.NetworkError -> {
                val message = response.error.localizedMessage
                ResponseResult.Error(message)
            }

            is NetworkResponse.UnknownError -> {
                val message = response.error?.localizedMessage
                ResponseResult.Error(message)
            }
        }
}