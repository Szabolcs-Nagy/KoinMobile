package com.codingfun.szabolcsnagy.presentation.image

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.ImageLoader
import com.codingfun.szabolcsnagy.constants.model.WikiPhoto
import com.codingfun.szabolcsnagy.constants.model.WikiPhotoDesc
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FullScreenImage(
    photo: WikiPhoto?,
    photoDesc: WikiPhotoDesc?,
    title: String,
    background: Boolean,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    link: String? = null,
    onBack: () -> Unit,
) {
    var currentLightStatusBars = true
    val view = LocalView.current
    val window = remember { (view.context as Activity).window }
    val insetsController = remember { WindowCompat.getInsetsController(window, view) }
    DisposableEffect(null) {
        if (!view.isInEditMode) {
            currentLightStatusBars = insetsController.isAppearanceLightStatusBars
            insetsController.isAppearanceLightStatusBars = false
        }

        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
            insetsController.isAppearanceLightStatusBars = currentLightStatusBars
        }
    }

    var showTopBar by remember { mutableStateOf(true) }

    LaunchedEffect(showTopBar) {
        if (showTopBar) {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        } else {
            insetsController.apply {
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                showTopBar,
                enter = slideInVertically(motionScheme.defaultSpatialSpec()) { -it },
                exit = slideOutVertically(motionScheme.defaultSpatialSpec()) { -it }
            ) {
                FullScreenImageTopBar(
                    photoDesc = photoDesc,
                    title = title,
                    link = link,
                    onBack = onBack
                )
            }
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { _ ->
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        // Zoom/Offset logic. Also prevents image from going out of bounds
        val state = rememberTransformableState { scaleChange, offsetChange, _ ->
            val maxX = (size.width * (scale - 1) / 2f)
            val maxY = (size.height * (scale - 1) / 2f)

            scale = (scale * scaleChange).coerceIn(1f..8f)

            offset += offsetChange.times(scale)
            offset = Offset(
                offset.x.coerceIn(-maxX, maxX),
                offset.y.coerceIn(-maxY, maxY)
            )
        }

        val coroutineScope = rememberCoroutineScope()

        if (photo != null && photoDesc != null)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                PageImage(
                    photo = photo,
                    photoDesc = photoDesc,
                    imageLoader = imageLoader,
                    contentScale = ContentScale.Inside,
                    background = background,
                    modifier = Modifier
                        .onSizeChanged { size = it }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showTopBar = !showTopBar
                                },
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        if (scale == 1f) // Zoom in only if the image is zoomed out
                                            state.animateZoomBy(4f)
                                        else
                                            state.animateZoomBy(0.25f)
                                    }
                                }
                            )
                        }
                )
            }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FullScreenImage(
    uri: String,
    description: String,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    link: String? = null,
    background: Boolean,
    onBack: () -> Unit,
) {
    var currentLightStatusBars = true
    val view = LocalView.current
    val window = remember { (view.context as Activity).window }
    val insetsController = remember { WindowCompat.getInsetsController(window, view) }
    DisposableEffect(null) {
        if (!view.isInEditMode) {
            currentLightStatusBars = insetsController.isAppearanceLightStatusBars
            insetsController.isAppearanceLightStatusBars = false
        }

        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
            insetsController.isAppearanceLightStatusBars = currentLightStatusBars
        }
    }

    var showTopBar by remember { mutableStateOf(true) }

    LaunchedEffect(showTopBar) {
        if (showTopBar) {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        } else {
            insetsController.apply {
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                showTopBar,
                enter = slideInVertically(motionScheme.defaultSpatialSpec()) { -it },
                exit = slideOutVertically(motionScheme.defaultSpatialSpec()) { -it }
            ) {
                FullScreenImageTopBar(
                    description = description,
                    link = link,
                    onBack = onBack
                )
            }
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { _ ->
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        // Zoom/Offset logic. Also prevents image from going out of bounds
        val state = rememberTransformableState { scaleChange, offsetChange, _ ->
            val maxX = (size.width * (scale - 1) / 2f)
            val maxY = (size.height * (scale - 1) / 2f)

            scale = (scale * scaleChange).coerceIn(1f..8f)

            offset += offsetChange.times(scale)
            offset = Offset(
                offset.x.coerceIn(-maxX, maxX),
                offset.y.coerceIn(-maxY, maxY)
            )
        }

        val coroutineScope = rememberCoroutineScope()

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            PageImage(
                uri = uri,
                description = description,
                imageLoader = imageLoader,
                contentScale = ContentScale.Inside,
                background = background,
                modifier = Modifier
                    .onSizeChanged { size = it }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = state)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showTopBar = !showTopBar
                            },
                            onDoubleTap = {
                                coroutineScope.launch {
                                    if (scale == 1f) // Zoom in only if the image is zoomed out
                                        state.animateZoomBy(4f)
                                    else
                                        state.animateZoomBy(0.25f)
                                }
                            }
                        )
                    }
            )
        }
    }
}