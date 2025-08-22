package com.codingfun.szabolcsnagy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.codingfun.szabolcsnagy.constants.color.toColor
import com.codingfun.szabolcsnagy.ui.theme.KoinAppTheme
import com.codingfun.szabolcsnagy.presentation.AppScreen
import com.codingfun.szabolcsnagy.presentation.ArticleViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val articleViewModel: ArticleViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        articleViewModel.startAnimDuration()
        installSplashScreen().setKeepOnScreenCondition {
            !articleViewModel.isReady || !articleViewModel.isAnimDurationComplete
        }
        articleViewModel.setFilesDir(filesDir.path)
        articleViewModel.migrateArticles()
        enableEdgeToEdge()
        setContent {
            val preferencesState by articleViewModel.preferencesState.collectAsState()

            val darkTheme = when (preferencesState.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val seed = preferencesState.colorScheme.toColor()

            KoinAppTheme(
                darkTheme = darkTheme,
                seedColor = seed,
                blackTheme = preferencesState.blackTheme
            ) {
                articleViewModel.setCompositionLocals(
                    cs = colorScheme,
                    tg = typography
                )
                AppScreen(
                    viewModel = articleViewModel,
                    preferencesState = preferencesState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}