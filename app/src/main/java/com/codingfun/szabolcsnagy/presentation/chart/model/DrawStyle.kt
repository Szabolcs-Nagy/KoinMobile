package com.codingfun.szabolcsnagy.presentation.chart.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class DrawStyle() {
    data class Stroke(val width: Dp = 2.dp, val strokeStyle: StrokeStyle = StrokeStyle.Normal) :
        DrawStyle()


    data object Fill : DrawStyle()
}