package com.codingfun.szabolcsnagy.data.utils

sealed class ResponseResult<out T : Any> {

    class Success<out T : Any>(val value: T) : ResponseResult<T>()

    class Error(val message: String?) : ResponseResult<Nothing>()
}