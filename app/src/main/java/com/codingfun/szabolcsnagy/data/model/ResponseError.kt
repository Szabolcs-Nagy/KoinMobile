package com.codingfun.szabolcsnagy.data.model

import com.google.gson.annotations.SerializedName

data class ResponseError(
    @SerializedName("code") val statusCode: String = "",
    @SerializedName("message") val statusMessage: String = ""
)