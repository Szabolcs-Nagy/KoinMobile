package com.codingfun.szabolcsnagy.di

import com.codingfun.szabolcsnagy.data.ArticleApiService
import com.codingfun.szabolcsnagy.data.HostInterceptor
import com.codingfun.szabolcsnagy.data.ResponseAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val BASE_URL = "https://en.wikipedia.org"

val networkModule = module {

    // HttpLoggingInterceptor instance
    single { provideLoggingInterceptor() }
    // HostInterceptor instance
    single { provideHostInterceptor() }
    // OkHttpClient instance
    single { provideOkHttpClient(get(), get()) }
    // Retrofit instance
    single { provideRetrofit(get()) }
    // ArticleApiService instance


    single { provideApiService(get()) }
}

private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}
private fun provideHostInterceptor(): HostInterceptor {
    return HostInterceptor()
}

private fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor, hostInterceptor: HostInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(hostInterceptor)
        .build()
}

private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addCallAdapterFactory(ResponseAdapterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideApiService(retrofit: Retrofit): ArticleApiService {
    return retrofit.create(ArticleApiService::class.java)
}