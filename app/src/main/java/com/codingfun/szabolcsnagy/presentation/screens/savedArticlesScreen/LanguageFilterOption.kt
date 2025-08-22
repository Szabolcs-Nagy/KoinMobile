package com.codingfun.szabolcsnagy.presentation.screens.savedArticlesScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LanguageFilterOption(
    val option: String,
    val langCode: String,
    initialSelection: Boolean = false
) {
    var selected by mutableStateOf(initialSelection)
}