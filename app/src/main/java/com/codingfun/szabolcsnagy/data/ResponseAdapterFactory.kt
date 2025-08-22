package com.codingfun.szabolcsnagy.data

import com.codingfun.szabolcsnagy.data.utils.NetworkResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * [CallAdapter.Factory] implementation that returns [ResponseAdapter] for the interface
 * methods which it can handle or null otherwise.
 */
class ResponseAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        // Suspend functions wrap the response type in `Call`
        if (Call::class.java != getRawType(returnType)) {
            return null
        }

        // Check first that the return type is `ParameterizedType`
        check(returnType is ParameterizedType) {
            "return type must be parameterized as Call<NetworkResponse<Any>> or Call<NetworkResponse<out Any>>"
        }

        // Get the response type inside the `Call` type
        val responseType = getParameterUpperBound(0, returnType)
        // if the response type is not NetworkResponse then we can't handle it, so we return null
        if (getRawType(responseType) != NetworkResponse::class.java) {
            return null
        }

        // The response type is NetworkResponse and should be parameterized
        check(responseType is ParameterizedType) {
            "response type must be parameterized as NetworkResponse<Any> or NetworkResponse<out Any>"
        }

        // Extract the success and error types from the NetworkResponse parameterized type
        val successBodyType = getParameterUpperBound(0, responseType)
        val errorBodyType = getParameterUpperBound(1, responseType)

        // Get the error converter
        val errorBodyConverter = retrofit.nextResponseBodyConverter<Any>(null, errorBodyType, annotations)

        return ResponseAdapter<Any, Any>(successBodyType, errorBodyConverter)
    }
}