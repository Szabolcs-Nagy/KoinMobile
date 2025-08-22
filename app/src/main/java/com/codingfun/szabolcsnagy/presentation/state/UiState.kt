package com.codingfun.szabolcsnagy.presentation.state

import androidx.compose.runtime.Immutable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.codingfun.szabolcsnagy.constants.model.FeedApiImage
import com.codingfun.szabolcsnagy.constants.model.FeedApiNews
import com.codingfun.szabolcsnagy.constants.model.FeedApiOTD
import com.codingfun.szabolcsnagy.constants.model.FeedApiTFA
import com.codingfun.szabolcsnagy.constants.model.MostReadArticle
import com.codingfun.szabolcsnagy.constants.model.SavedStatus
import com.codingfun.szabolcsnagy.constants.model.WRStatus
import com.codingfun.szabolcsnagy.constants.model.WikiLang
import com.codingfun.szabolcsnagy.constants.model.WikiPhoto
import com.codingfun.szabolcsnagy.constants.model.WikiPhotoDesc
import com.codingfun.szabolcsnagy.constants.model.WikiPrefixSearchResult
import com.codingfun.szabolcsnagy.constants.model.WikiSearchResult
import com.codingfun.szabolcsnagy.presentation.screens.savedArticlesScreen.LanguageFilterOption

@Immutable
data class AppSearchBarState(
    val prefixSearchResults: List<WikiPrefixSearchResult>? = emptyList(),
    val searchResults: List<WikiSearchResult>? = emptyList(),
    val history: Set<String> = setOf(),
    val focusRequester: FocusRequester = FocusRequester()
)

@Immutable
data class HomeScreenState(
    val title: String = "",
    val extract: List<List<AnnotatedString>> = emptyList(),
    val sections: List<Pair<Int, String>> = emptyList(),
    val photo: WikiPhoto? = null,
    val photoDesc: WikiPhotoDesc? = null,
    val langs: List<WikiLang>? = null,
    val currentLang: String? = null,
    val pageId: Int? = null,
    val status: WRStatus = WRStatus.UNINITIALIZED,
    val savedStatus: SavedStatus = SavedStatus.NOT_SAVED,
    val isLoading: Boolean = false,
    val loadingProgress: Float? = null,
    val backStackSize: Int = 0
)

@Immutable
data class PreferencesState(
    val theme: String = "auto",
    val lang: String = "en",
    val fontStyle: String = "sans",
    val colorScheme: String = Color.White.toString(),
    val fontSize: Int = 16,
    val blackTheme: Boolean = false,
    val dataSaver: Boolean = false,
    val expandedSections: Boolean = false,
    val imageBackground: Boolean = false,
    val immersiveMode: Boolean = false,
    val renderMath: Boolean = true,
    val searchHistory: Boolean = true
)

@Immutable
data class FeedState(
    val tfa: FeedApiTFA? = null,
    val mostReadArticles: List<MostReadArticle>? = null,
    val image: FeedApiImage? = null,
    val news: List<FeedApiNews>? = null,
    val onThisDay: List<FeedApiOTD>? = null,
    val sections: List<Pair<Int, FeedSection>> = emptyList()
)

@Immutable
data class SavedArticlesState(
    val isLoading: Boolean = false,
    val savedArticles: List<String> = emptyList(),
    val languageFilters: List<LanguageFilterOption> = emptyList(),
    val articlesSize: Long = 0L
)

enum class FeedSection {
    TFA, MOST_READ, IMAGE, NEWS, ON_THIS_DAY
}