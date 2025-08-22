package com.codingfun.szabolcsnagy.presentation.chart.model

data class DividerProperties(
    val enabled:Boolean = true,
    val xAxisProperties:LineProperties = LineProperties(),
    val yAxisProperties:LineProperties = LineProperties()
)
