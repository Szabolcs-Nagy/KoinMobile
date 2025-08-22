package com.codingfun.szabolcsnagy.presentation.screens.savedArticlesScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.codingfun.szabolcsnagy.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SavedArticlesTopBar(scrollBehavior: TopAppBarScrollBehavior, onBack: () -> Unit) {
    LargeTopAppBar(
        title = { Text(stringResource(R.string.savedArticles)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}