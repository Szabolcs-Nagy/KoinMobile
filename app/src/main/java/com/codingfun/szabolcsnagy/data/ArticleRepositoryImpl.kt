package com.codingfun.szabolcsnagy.data

import com.codingfun.szabolcsnagy.domain.ArticleRepository
import com.codingfun.szabolcsnagy.domain.ArticleResolver

class ArticleRepositoryImpl(private val articleResolver: ArticleResolver): ArticleRepository {
    
    override suspend fun getPrefixSearchResults(query: String) = articleResolver.getPrefixSearchResults(query)
    override suspend fun getSearchResults(query: String) = articleResolver.getSearchResults(query)
    override suspend fun getPageData(query: String) = articleResolver.getPageData(query)
    override suspend fun getPageContent(title: String) = articleResolver.getPageContent(title)
    override suspend fun getRandomResult() = articleResolver.getRandomResult()
    override suspend fun getFeed(date: String) = articleResolver.getFeed(date)
    
}