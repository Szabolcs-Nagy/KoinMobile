package com.codingfun.szabolcsnagy.presentation.chart.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BarProperties(
    val thickness: Dp = 15.dp,
    val spacing: Dp = 6.dp,
    val cornerRadius: Bars.Data.Radius = Bars.Data.Radius.None,
    val style: DrawStyle = DrawStyle.Fill
)
