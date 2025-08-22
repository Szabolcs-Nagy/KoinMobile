package com.codingfun.szabolcsnagy.presentation.screens.homeScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.codingfun.szabolcsnagy.R
import com.codingfun.szabolcsnagy.constants.color.langCodeToName
import com.codingfun.szabolcsnagy.constants.model.WikiLang
import com.codingfun.szabolcsnagy.presentation.screens.settingsScreen.LanguageSearchBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleLanguageBottomSheet(
    langs: List<WikiLang>,
    searchStr: String,
    searchQuery: String,
    setShowSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    loadPage: (String) -> Unit,
    setSearchStr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            setShowSheet(false)
            setSearchStr("")
        },
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.chooseWikipediaLanguage),
                style = MaterialTheme.typography.labelLarge
            )
            LanguageSearchBar(
                searchStr = searchStr,
                setSearchStr = setSearchStr,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            HorizontalDivider()
            LazyColumn(state = listState) {
                items(langs, key = { it.lang }) {
                    val langName: String? = try {
                        langCodeToName(it.lang)
                    } catch (_: Exception) {
                        Log.e("Language", "Language not found: ${it.lang}")
                        null
                    }
                    if (langName != null && langName.contains(searchQuery, ignoreCase = true))
                        ListItem(
                            headlineContent = { Text(langName) },
                            supportingContent = { Text(it.title) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier
                                .clickable(onClick = {
                                    setLang(it.lang)
                                    loadPage(it.title)
                                    scope
                                        .launch { bottomSheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!bottomSheetState.isVisible) {
                                                setShowSheet(false)
                                                setSearchStr("")
                                            }
                                        }
                                })
                        )
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
    LaunchedEffect(searchQuery) {
        listState.scrollToItem(0)
    }
}
