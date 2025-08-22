package com.codingfun.szabolcsnagy.presentation.screens.homeScreen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import com.codingfun.szabolcsnagy.presentation.chart.LineChart
import com.codingfun.szabolcsnagy.presentation.chart.model.DividerProperties
import com.codingfun.szabolcsnagy.presentation.chart.model.DotProperties
import com.codingfun.szabolcsnagy.presentation.chart.model.GridProperties
import com.codingfun.szabolcsnagy.presentation.chart.model.HorizontalIndicatorProperties
import com.codingfun.szabolcsnagy.presentation.chart.model.LabelHelperProperties
import com.codingfun.szabolcsnagy.presentation.chart.model.LabelProperties
import com.codingfun.szabolcsnagy.presentation.chart.model.Line
import com.codingfun.szabolcsnagy.presentation.chart.model.PopUpProperties

@Composable
fun ArticleViewsGraph(viewCounts: List<Int>, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    LineChart(
        modifier = modifier,
        data = remember {
            listOf(
                Line(
                    label = "",
                    values = viewCounts.map { it.toDouble() },
                    color = SolidColor(colorScheme.primary),
                    strokeAnimationSpec = tween(1500, easing = FastOutSlowInEasing),
                    curvedEdges = true
                )
            )
        },
        labelHelperProperties = LabelHelperProperties(enabled = false),
        gridProperties = GridProperties(enabled = false),
        popupProperties = PopUpProperties(enabled = false),
        labelProperties = LabelProperties(enabled = false),
        indicatorProperties = HorizontalIndicatorProperties(enabled = false),
        dotsProperties = DotProperties(enabled = false),
        dividerProperties = DividerProperties(enabled = false)
    )
}