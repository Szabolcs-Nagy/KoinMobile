package com.codingfun.szabolcsnagy.di

import android.content.Context
import com.google.gson.Gson
import com.codingfun.szabolcsnagy.data.ArticleResolverImpl
import com.codingfun.szabolcsnagy.data.ArticleApiService
import com.codingfun.szabolcsnagy.data.HostInterceptor
import com.codingfun.szabolcsnagy.constants.preferences.AppPreferencesRepository
import com.codingfun.szabolcsnagy.domain.ArticleResolver
import com.codingfun.szabolcsnagy.presentation.ArticleViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel { provideMainViewModel(get(), get(), get()) }

    single { appPreferencesRepository(androidContext(), get()) }

    single { provideRepository(get(), get()) }

    single { provideCoroutinesDispatcher() }

    single { provideGson() }
}

private fun provideCoroutinesDispatcher(): CoroutineDispatcher {
    return Dispatchers.IO
}

private fun provideRepository(articleApiService: ArticleApiService, ioDispatcher: CoroutineDispatcher): ArticleResolver {
    return ArticleResolverImpl(articleApiService, ioDispatcher)
}

private fun appPreferencesRepository(context: Context, ioDispatcher: CoroutineDispatcher): AppPreferencesRepository {
    return AppPreferencesRepository(context, ioDispatcher)
}

private fun provideGson(): Gson {
    return Gson()
}

private fun provideMainViewModel(repository: ArticleResolver, hostInterceptor: HostInterceptor, appPreferencesRepository: AppPreferencesRepository): ArticleViewModel {
    return ArticleViewModel(
        articleResolver = repository,
        interceptor = hostInterceptor,
        appPreferencesRepository = appPreferencesRepository,
    )
}