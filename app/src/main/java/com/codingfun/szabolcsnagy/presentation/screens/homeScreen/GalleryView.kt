package com.codingfun.szabolcsnagy.presentation.screens.homeScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import com.codingfun.szabolcsnagy.constants.parse.toWikitextAnnotatedString
import com.codingfun.szabolcsnagy.R
import com.codingfun.szabolcsnagy.presentation.image.FeedImage
import kotlinx.coroutines.launch

@Composable
fun Gallery(
    text: String,
    fontSize: Int,
    background: Boolean,
    imageLoader: ImageLoader,
    onLinkClick: (String) -> Unit,
    onClick: (String, String) -> Unit
) {
    val content = remember(text) { text.substringAfter('>').trim(' ', '\n').lines() }
    val pagerState = rememberPagerState { content.size }
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        val uriLow = remember(text) {
            "https://commons.wikimedia.org/wiki/Special:FilePath/${
                content[it].substringBefore('|')
            }?width=720"
        }
        val uriHigh = remember(text) {
            "https://commons.wikimedia.org/wiki/Special:FilePath/${
                content[it].substringBefore('|')
            }"
        }
        val description = remember(text) { content[it].substringAfter('|') }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            FeedImage(
                source = uriLow,
                description = description,
                imageLoader = imageLoader,
                loadingIndicator = false,
                background = background,
                modifier = Modifier
                    .clip(shapes.large)
                    .clickable(onClick = { onClick(uriHigh, description) })
            )
            Text(
                description.toWikitextAnnotatedString(
                    colorScheme = colorScheme,
                    fontSize = fontSize - 2,
                    loadPage = onLinkClick,
                    typography = typography
                ),
                fontSize = (fontSize - 2).sp,
                lineHeight = (24 * ((fontSize - 2) / 16.0)).toInt().sp,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage - 1
                        )
                    }
                },
                enabled = pagerState.currentPage != 0
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.scrollLeft)
                )
            }
            Text(
                stringResource(
                    R.string.imageCounter,
                    pagerState.currentPage + 1,
                    pagerState.pageCount
                )
            )
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage + 1
                        )
                    }
                },
                enabled = pagerState.currentPage != pagerState.pageCount - 1
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.scrollRight)
                )
            }
        }
    }
}