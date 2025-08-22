package com.codingfun.szabolcsnagy.presentation.screens.aboutScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codingfun.szabolcsnagy.BuildConfig
import com.codingfun.szabolcsnagy.R
import com.codingfun.szabolcsnagy.ui.theme.KoinAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isAlpha = remember { BuildConfig.VERSION_NAME.contains('a') }

    Scaffold(
        topBar = { AboutTopAppBar(scrollBehavior = scrollBehavior, onBack = onBack) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        LazyColumn(
            contentPadding = insets
        ) {
            item {
                OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_launcher_foreground), null)
                            Text(
                                if (!isAlpha) AnnotatedString(stringResource(R.string.app_name))
                                else buildAnnotatedString {
                                    append(stringResource(R.string.app_name))
                                    withStyle(
                                        SpanStyle(
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                            fontStyle = FontStyle.Italic,
                                            baselineShift = BaselineShift.Superscript
                                        )
                                    ) {
                                        append(" αlpha")
                                    }
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            stringResource(R.string.appTagline),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Outlined.Info, null)
                            },
                            headlineContent = { Text(stringResource(R.string.version)) },
                            supportingContent = { Text(BuildConfig.VERSION_NAME) }
                        )
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.code), null)
                            },
                            headlineContent = { Text(stringResource(R.string.sourceCode)) },
                            supportingContent = { Text("GitHub") },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/Szabolcs-Nagy/KoinMobile/")
                            })
                        )
//                        ListItem(
//                            leadingContent = {
//                                Icon(painterResource(R.drawable.gavel), null)
//                            },
//                            headlineContent = { Text(stringResource(R.string.license)) },
//                            supportingContent = { Text("GPL v3.0") },
//                            modifier = Modifier.clickable(onClick = {
//                                uriHandler.openUri("https://github.com/Szabolcs-Nagy/KoinMobile/blob/main/LICENSE")
//                            })
//                        )
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.update), null)
                            },
                            headlineContent = { Text(stringResource(R.string.releases)) },
                            supportingContent = { Text(stringResource(R.string.releasesDesc)) },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/Szabolcs-Nagy/KoinMobile/releases")
                            })
                        )
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.translate), null)
                            },
                            headlineContent = { Text(stringResource(R.string.translate)) },
                            supportingContent = { Text(stringResource(R.string.translateDesc)) },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://hosted.weblate.org/engage/wikireader/")
                            })
                        )
                    }
                }
            }
            item {
                OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.wikipedia),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Start)
                        )
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.wikipedia_s_w), null
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.wikipedia)) },
                            supportingContent = { Text(stringResource(R.string.wikipediaWebsiteDesc)) },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://wikipedia.org")
                            })
                        )
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.wikimedia_logo_black), null
                                )
                            },
                            headlineContent = { Text(stringResource(R.string.supportWikipedia)) },
                            supportingContent = { Text(stringResource(R.string.donateToWikipedia)) },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://wikimediafoundation.org/support/")
                            })
                        )
                    }
                }
            }
            item {
                OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.author),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Start)
                        )
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(R.drawable.github), null
                                )
                            },
                            headlineContent = { Text("Szabolcs Nagy") },
                            supportingContent = { Text(stringResource(R.string.otherProjectsDesc)) },
                            modifier = Modifier.clickable(onClick = {
                                uriHandler.openUri("https://github.com/Szabolcs-Nagy/")
                            })
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AboutScreenPreview() {
    KoinAppTheme(darkTheme = true) {
        AboutScreen {}
    }
}
