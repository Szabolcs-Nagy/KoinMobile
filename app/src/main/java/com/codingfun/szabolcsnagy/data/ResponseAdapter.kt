package com.codingfun.szabolcsnagy.data

import com.codingfun.szabolcsnagy.data.utils.NetworkResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

/**
 * [CallAdapter] interface implementation to let Retrofit know about [ResponseCall].
 */
class ResponseAdapter<S : Any, E : Any>(
    private val successType: Type,
    private val errorBodyConverter: Converter<ResponseBody, E>
) : CallAdapter<S, Call<NetworkResponse<S, E>>> {

    override fun adapt(call: Call<S>): Call<NetworkResponse<S, E>> {
        return ResponseCall(call, errorBodyConverter)
    }

    override fun responseType(): Type = successType
}