package com.codingfun.szabolcsnagy.presentation.screens.homeScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import com.codingfun.szabolcsnagy.ui.theme.KoinAppTheme
import com.codingfun.szabolcsnagy.R

@Composable
fun ExpandableSection(
    title: List<AnnotatedString>,
    body: List<AnnotatedString>,
    fontSize: Int,
    fontFamily: FontFamily,
    imageLoader: ImageLoader,
    expanded: Boolean,
    renderMath: Boolean,
    darkTheme: Boolean,
    dataSaver: Boolean,
    imageBackground: Boolean,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
    onGalleryImageClick: (String, String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(expanded) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { expanded = !expanded })
        ) {
            AnimatedContent(expanded) { expanded ->
                when (expanded) {
                    true -> Icon(
                        Icons.Outlined.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.collapse_section),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    else -> Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.expand_section),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Text(
                text = remember {
                    var out = ""
                    title.forEach {
                        out += it
                    }
                    out.replace("<.+>".toRegex(), "")
                },
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                fontSize = (28 * (fontSize / 16.0)).toInt().sp,
                lineHeight = (36 * (fontSize / 16.0)).toInt().sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
            )
        }

        AnimatedVisibility(
            expanded,
            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut()
        ) {
            ParsedBodyText(
                body = body,
                fontSize = fontSize,
                fontFamily = fontFamily,
                renderMath = renderMath,
                imageLoader = imageLoader,
                darkTheme = darkTheme,
                dataSaver = dataSaver,
                background = imageBackground,
                onLinkClick = onLinkClick,
                onGalleryImageClick = onGalleryImageClick
            )
        }
    }
}

@Preview
@Composable
fun ExpandableSectionPreview() {
    KoinAppTheme {
        ExpandableSection(
            title = listOf(buildAnnotatedString { append("Title") }),
            body = listOf(buildAnnotatedString { append("Lorem\nIpsum\nBig\nHonking\nBody\nText") }),
            fontSize = 16,
            fontFamily = FontFamily.SansSerif,
            imageLoader = ImageLoader(context = LocalContext.current),
            expanded = false,
            renderMath = true,
            darkTheme = false,
            false,
            false,
            onLinkClick = {}
        ) { a, b -> }
    }
}
