package com.codingfun.szabolcsnagy.presentation.chart.model

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

data class LabelHelperProperties(
    val enabled:Boolean = true,
    val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp)
)
