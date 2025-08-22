package com.codingfun.szabolcsnagy.presentation

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.parseAsHtml
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.network.HttpException
import com.codingfun.szabolcsnagy.constants.color.langCodeToName
import com.codingfun.szabolcsnagy.constants.color.parseSections
import com.codingfun.szabolcsnagy.constants.model.SavedStatus
import com.codingfun.szabolcsnagy.constants.model.WRStatus
import com.codingfun.szabolcsnagy.constants.model.WikiApiPageData
import com.codingfun.szabolcsnagy.constants.parse.cleanUpWikitext
import com.codingfun.szabolcsnagy.constants.parse.substringMatchingParen
import com.codingfun.szabolcsnagy.constants.parse.toWikitextAnnotatedString
import com.codingfun.szabolcsnagy.constants.preferences.AppPreferencesRepository
import com.codingfun.szabolcsnagy.data.HostInterceptor
import com.codingfun.szabolcsnagy.data.utils.ResponseResult
import com.codingfun.szabolcsnagy.domain.ArticleResolver
import com.codingfun.szabolcsnagy.presentation.screens.savedArticlesScreen.LanguageFilterOption
import com.codingfun.szabolcsnagy.presentation.state.AppSearchBarState
import com.codingfun.szabolcsnagy.presentation.state.FeedSection
import com.codingfun.szabolcsnagy.presentation.state.FeedState
import com.codingfun.szabolcsnagy.presentation.state.HomeScreenState
import com.codingfun.szabolcsnagy.presentation.state.PreferencesState
import com.codingfun.szabolcsnagy.presentation.state.SavedArticlesState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.listDirectoryEntries
import kotlin.math.min

class ArticleViewModel(
    private val interceptor: HostInterceptor,
    private val articleResolver: ArticleResolver,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {
    private val _appSearchBarState = MutableStateFlow(AppSearchBarState())
    val appSearchBarState: StateFlow<AppSearchBarState> = _appSearchBarState.asStateFlow()

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState.asStateFlow()

    private val _feedState = MutableStateFlow(FeedState())
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    private val _savedArticlesState = MutableStateFlow(SavedArticlesState())
    val savedArticlesState: StateFlow<SavedArticlesState> = _savedArticlesState.asStateFlow()

    private val _articleListState = MutableStateFlow(LazyListState(0, 0))
    val articleListState: StateFlow<LazyListState> = _articleListState.asStateFlow()
    private val _searchListState = MutableStateFlow(LazyListState(0, 0))
    val searchListState: StateFlow<LazyListState> = _searchListState.asStateFlow()

    private val _preferencesState = MutableStateFlow(PreferencesState())
    val preferencesState: StateFlow<PreferencesState> = _preferencesState.asStateFlow()

    private val _languageSearchStr = MutableStateFlow("")
    val languageSearchStr: StateFlow<String> = _languageSearchStr.asStateFlow()

    val textFieldState: TextFieldState = TextFieldState()

    @OptIn(FlowPreview::class)
    val languageSearchQuery = languageSearchStr.debounce(500L)

    internal val backStack = ArrayDeque<Pair<String?, Pair<Int, Int>>>()
    var lastQuery: Pair<String, String>? = null
    private var filesDir: String = ""
    private var loaderJob = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }
    private var searchDebounceJob: Job? = null
    private var colorScheme: ColorScheme = lightColorScheme()
    private var typography: Typography = Typography()
    private var fromLink: Boolean = false

    var isReady = false
    var isAnimDurationComplete = false

    private var sections = 0
    private var currentSection = 0

    init {
        viewModelScope.launch { // Run blocking to delay app startup until theme is determined
            val colorScheme = appPreferencesRepository.readStringPreference("color-scheme")
                ?: appPreferencesRepository.saveStringPreference(
                    "color-scheme",
                    Color.Companion.White.toString()
                )

            val fontStyle = appPreferencesRepository.readStringPreference("font-style")
                ?: appPreferencesRepository.saveStringPreference("font-style", "sans")

            val lang = appPreferencesRepository.readStringPreference("lang")
                ?: appPreferencesRepository.saveStringPreference("lang", "en")

            val theme = appPreferencesRepository.readStringPreference("theme")
                ?: appPreferencesRepository.saveStringPreference("theme", "auto")

            val fontSize = appPreferencesRepository.readIntPreference("font-size")
                ?: appPreferencesRepository.saveIntPreference("font-size", 16)

            val blackTheme = appPreferencesRepository.readBooleanPreference("black-theme")
                ?: appPreferencesRepository.saveBooleanPreference("black-theme", false)

            val dataSaver = appPreferencesRepository.readBooleanPreference("data-saver")
                ?: appPreferencesRepository.saveBooleanPreference("data-saver", false)

            val expandedSections =
                appPreferencesRepository.readBooleanPreference("expanded-sections")
                    ?: appPreferencesRepository.saveBooleanPreference("expanded-sections", false)

            val imageBackground = appPreferencesRepository.readBooleanPreference("image-background")
                ?: appPreferencesRepository.saveBooleanPreference("image-background", false)

            val immersiveMode = appPreferencesRepository.readBooleanPreference("immersive-mode")
                ?: appPreferencesRepository.saveBooleanPreference("immersive-mode", false)

            val renderMath = appPreferencesRepository.readBooleanPreference("render-math")
                ?: appPreferencesRepository.saveBooleanPreference("render-math", true)

            val searchHistory = appPreferencesRepository.readBooleanPreference("search-history")
                ?: appPreferencesRepository.saveBooleanPreference("search-history", true)

            _preferencesState.update { currentState ->
                currentState.copy(
                    blackTheme = blackTheme,
                    colorScheme = colorScheme,
                    dataSaver = dataSaver,
                    expandedSections = expandedSections,
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    imageBackground = imageBackground,
                    immersiveMode = immersiveMode,
                    lang = lang,
                    renderMath = renderMath,
                    searchHistory = searchHistory,
                    theme = theme
                )
            }

            _appSearchBarState.update { currentState ->
                currentState.copy(history = appPreferencesRepository.readHistory() ?: emptySet())
            }

            updateArticlesList()

            interceptor.setHost("$lang.wikipedia.org")
            isReady = true
            loadFeed()
        }
    }

    fun startAnimDuration() {
        viewModelScope.launch {
            delay(600)
            isAnimDurationComplete = true
        }
    }

    fun setFilesDir(path: String) {
        filesDir = path
    }

    fun setCompositionLocals(cs: ColorScheme, tg: Typography) {
        colorScheme = cs
        typography = tg
    }

    private fun pushBackstack(
        title: String?,
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int
    ) {
        backStack.addLast(Pair(title, Pair(firstVisibleItemIndex, firstVisibleItemScrollOffset)))
        if (backStack.size > 16) backStack.removeFirst()
    }

    fun stopAll() {
        loaderJob.cancel()
        fromLink = true
    }

    fun loadPreviousPage() {
        val popped = backStack.removeLastOrNull()
        if (popped?.first != null) {
            loadPage(title = popped.first, listStatePair = popped.second)
        } else {
            backStack.clear()
            loadFeed(true)
            _homeScreenState.update { currentState ->
                currentState.copy(backStackSize = 0)
            }
        }
    }

    private suspend fun loadSearchResults(query: String) {
        val q = query.trim()
        if (q.isNotEmpty()) {
            try {
                val prefixSearchResults = articleResolver.getPrefixSearchResults(q)
                when (prefixSearchResults) {
                    is ResponseResult.Success -> {
                        _appSearchBarState.update { currentState ->
                            currentState.copy(
                                prefixSearchResults = prefixSearchResults.value.query.pages.sortedBy { it.index }
                            )
                        }
                        Log.d("ArticleViewModel", "Received Prefix Search Result")
                    }

                    is ResponseResult.Error -> {
                        Log.e("ArticleViewModel", "${prefixSearchResults.message}")
                    }
                }

                val searchResults = articleResolver.getSearchResults(q)
                when (searchResults) {
                    is ResponseResult.Success -> {
                        val results = searchResults.value.query.pages.sortedBy { it.index }
                        val resultsParsed = results.map {
                            it.copy(
                                snippet = it.snippet.replace("<span.+?(?<!/)>".toRegex(), "<b>")
                                    .replace("</span>", "</b>"),
                                titleSnippet = it.titleSnippet.replace(
                                    "<span.+?(?<!/)>".toRegex(),
                                    "<b>"
                                )
                                    .replace("</span>", "</b>")
                            )
                        }
                        _appSearchBarState.update { currentState ->
                            currentState.copy(
                                searchResults = resultsParsed
                            )
                        }
                        Log.d("ArticleViewModel", "Received Search Result")
                    }

                    is ResponseResult.Error -> {
                        Log.e("ArticleViewModel", " ${searchResults.message}")
                    }
                }
            } catch (_: Exception) {
                _appSearchBarState.update { currentState ->
                    currentState.copy(
                        prefixSearchResults = null,
                        searchResults = null
                    )
                }
            }
        } else clearSearchResults()
    }

    private fun clearSearchResults() {
        _appSearchBarState.update { currentState ->
            currentState.copy(prefixSearchResults = emptyList(), searchResults = emptyList())
        }
    }

    fun loadSearchResultsDebounced(query: String) {
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(500)
            loadSearchResults(query)
        }
    }
    @SuppressWarnings("ConstantConditions")
    fun loadSearch(
        query: String?,
        lang: String? = null,
        random: Boolean = false,
        listStatePair: Pair<Int, Int>? = null
    ) {
        val q = query?.trim() ?: " "
        val history = appSearchBarState.value.history.toMutableSet()
        if (q != "") {
            viewModelScope.launch {
                var setLang = preferencesState.value.lang
                try {
                    if (lang != null) {
                        interceptor.setHost("$lang.wikipedia.org")
                        setLang = lang
                    }
                    _homeScreenState.update { currentState ->
                        currentState.copy(isLoading = true, loadingProgress = null)
                    }
                    if (!random && listStatePair == null && preferencesState.value.searchHistory) {
                        history.remove(q)
                        history.add(q)
                        if (history.size > 50) history.remove(history.first())
                        appPreferencesRepository.saveHistory(history)
                    }
                    if (!random) {
                        loadSearchResults(q)
                        if (appSearchBarState.value.prefixSearchResults != null && appSearchBarState.value.searchResults != null)
                            loadPage(
                                title = if (appSearchBarState.value.prefixSearchResults!!.isEmpty())
                                    appSearchBarState.value.searchResults!![0].title
                                else appSearchBarState.value.prefixSearchResults!![0].title,
                                lang = setLang,
                                listStatePair = listStatePair
                            )
                        else throw Exception()
                    } else
                        loadPage(title = null, random = true)
                } catch (e: Exception) {
                    if (e.cause != null) _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf(
                                "An error occurred :(\n" +
                                        "Please check your internet connection"
                            ).map { parseWikitext(it) },
                            photo = null,
                            photoDesc = null,
                            langs = null,
                            currentLang = setLang,
                            status = WRStatus.NO_SEARCH_RESULT,
                            pageId = null,
                            isLoading = false,
                            backStackSize = backStack.size,
                            savedStatus = SavedStatus.NOT_SAVED
                        )
                    } else _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf("No search results found for $q").map {
                                parseWikitext(it)
                            },
                            photo = null,
                            photoDesc = null,
                            langs = null,
                            currentLang = setLang,
                            status = WRStatus.NO_SEARCH_RESULT,
                            pageId = null,
                            isLoading = false,
                            backStackSize = backStack.size,
                            savedStatus = SavedStatus.NOT_SAVED
                        )
                    }
                }
            }

            if (!random && listStatePair == null)
                _appSearchBarState.update { currentState ->
                    currentState.copy(
                        history = history
                    )
                }
        }
    }

    /**
     * Loads page with the given title
     *
     * @param title Page title
     */
    fun loadPage(
        title: String?,
        lang: String? = null,
        random: Boolean = false,
        listStatePair: Pair<Int, Int>? = null
    ) {
        loaderJob.cancel()
        viewModelScope.launch(loaderJob) {
            var setLang = preferencesState.value.lang
            if (title != null || random) {
                try {
                    if (lang != null) {
                        interceptor.setHost("$lang.wikipedia.org")
                        setLang = lang
                    }
                    if (title != null) {
                        lastQuery = Pair(title, setLang)
                    }
                    _homeScreenState.update { currentState ->
                        currentState.copy(isLoading = true, loadingProgress = null)
                    }

                    val wikiApiPageData = when (random) {
                        false -> articleResolver.getPageData(title!!)
                        else -> articleResolver.getRandomResult()
                    }

                    val apiResponse = when (wikiApiPageData) {
                        is ResponseResult.Success -> {
                            wikiApiPageData.value.query?.pages?.get(0)
                        }

                        is ResponseResult.Error -> {
                            Log.e(
                                "ArticleViewModel",
                                "Error in fetching results: ${wikiApiPageData.message}"
                            )
                            null
                        }
                    }

                    if (apiResponse?.title != null)
                        lastQuery = Pair(apiResponse.title, setLang)


                    val extractText = if (apiResponse != null) {
                        val extractTextResult =
                            articleResolver.getPageContent(apiResponse.title)
                        when (extractTextResult) {
                            is ResponseResult.Success -> {
                                extractTextResult.value
                            }

                            is ResponseResult.Error -> {
                                Log.e(
                                    "ArticleViewModel",
                                    "Error in fetching Page Content: ${extractTextResult.message}"
                                )
                                ""
                            }
                        }
                    } else {
                        ""
                    }
                    var saved = SavedStatus.NOT_SAVED
                    val extract: List<String> =
                        parseSections(extractText)
                    val status: WRStatus = WRStatus.SUCCESS

                    try {
                        val articlesDir = File(filesDir, "savedArticles")

                        val apiFile = File(
                            articlesDir,
                            "${apiResponse!!.title}.${apiResponse.pageId}-api.${setLang}"
                        )
                        if (apiFile.exists()) saved = SavedStatus.SAVED
                    } catch (_: Exception) {
                    }

                    if (
                        listStatePair == null &&
                        homeScreenState.value.status != WRStatus.FEED_LOADED &&
                        homeScreenState.value.status != WRStatus.FEED_NETWORK_ERROR &&
                        homeScreenState.value.status != WRStatus.UNINITIALIZED
                    ) {
                        val articleState = articleListState.value
                        pushBackstack(
                            title = homeScreenState.value.title,
                            firstVisibleItemIndex = articleState.firstVisibleItemIndex,
                            firstVisibleItemScrollOffset = articleState.firstVisibleItemScrollOffset
                        )
                    }

                    _articleListState.update {
                        LazyListState(0, 0)
                    }

                    sections = extract.size
                    var sectionIndex = 3
                    val articleSections = mutableListOf<Pair<Int, String>>()
                    val parsedExtract = mutableListOf<List<AnnotatedString>>()

                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = apiResponse?.title ?: "Error",
                            photo = apiResponse?.photo,
                            photoDesc = apiResponse?.photoDesc,
                            langs = apiResponse?.langs,
                            currentLang = setLang,
                            status = status,
                            pageId = apiResponse?.pageId,
                            backStackSize = backStack.size,
                            savedStatus = saved
                        )
                    }

                    extract.forEachIndexed { index, it ->
                        currentSection = index + 1
                        val parsed = parseWikitext(it)
                        if (index % 2 == 1) {
                            articleSections.add(
                                Pair(
                                    sectionIndex,
                                    parsed.joinToString(separator = "").parseAsHtml()
                                        .toString()
                                )
                            )
                            sectionIndex += 2
                        }
                        parsedExtract.add(parsed)
                        _homeScreenState.update { currentState ->
                            currentState.copy(
                                loadingProgress = currentSection.toFloat() / sections,
                                extract = parsedExtract,
                                sections = articleSections
                            )
                        }
                    }

                    _homeScreenState.update { currentState ->
                        currentState.copy(isLoading = false)
                    }

                    if (listStatePair != null)
                        _articleListState.update {
                            LazyListState(listStatePair.first, listStatePair.second)
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = if (e.message?.contains("404") == true) {
                                listOf("No article with title $title")
                                    .map { parseWikitext(it) }
                            } else if (e is HttpException) {
                                listOf(
                                    "An error occurred :(\n" +
                                            "Please check your internet connection"
                                ).map { parseWikitext(it) }
                            } else {
                                listOf("An unknown error occurred :(\n${e.message} caused by ${e.cause}")
                                    .map { parseWikitext(it) }
                            },
                            langs = null,
                            currentLang = null,
                            photo = null,
                            photoDesc = null,
                            status = WRStatus.NETWORK_ERROR,
                            pageId = null,
                            isLoading = false,
                            savedStatus = SavedStatus.NOT_SAVED
                        )
                    }
                }

                if (lang != null)
                    _preferencesState.update { currentState ->
                        currentState.copy(lang = lang)
                    }
            } else {
                _homeScreenState.update { currentState ->
                    currentState.copy(
                        title = "Error",
                        extract = listOf("Null search query").map { parseWikitext(it) },
                        photo = null,
                        photoDesc = null,
                        langs = null,
                        currentLang = setLang,
                        status = WRStatus.OTHER,
                        pageId = null,
                        isLoading = false,
                        backStackSize = backStack.size,
                        savedStatus = SavedStatus.NOT_SAVED
                    )
                }
            }
        }
    }

    fun reloadPage(
        persistLang: Boolean = false
    ) {
        if (lastQuery != null) {
            if (persistLang)
                loadPage(
                    lastQuery?.first,
                    lang = lastQuery?.second,
                    random = false
                )
            else
                loadPage(
                    lastQuery?.first,
                    random = false
                )
        }
    }

    /**
     * Loads feed, updates the [FeedState] and sets the app status to [WRStatus.FEED_LOADED]
     *
     * If an error is encountered, app status is set to [WRStatus.FEED_NETWORK_ERROR] and home screen
     * text is updated to the error
     */
    fun loadFeed(fromBack: Boolean = false) {
        viewModelScope.launch(loaderJob) {
            if (!preferencesState.value.dataSaver) {
                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true, loadingProgress = null)
                }

                try {
                    val feed = articleResolver.getFeed()
                    when (feed) {
                        is ResponseResult.Success -> {
                            val sections = mutableListOf<Pair<Int, FeedSection>>()
                            var currentSection = 0
                            _feedState.update { currentState ->
                                currentState.copy(
                                    tfa = feed.value.tfa,
                                    mostReadArticles = feed.value.mostRead?.articles?.sortedBy { it.rank },
                                    image = feed.value.image,
                                    news = feed.value.news,
                                    onThisDay = feed.value.onThisDay
                                )
                            }

                            if (feedState.value.tfa != null) {
                                sections.add(Pair(currentSection, FeedSection.TFA))
                                currentSection++
                            }
                            if (feedState.value.mostReadArticles != null) {
                                sections.add(Pair(currentSection, FeedSection.MOST_READ))
                                currentSection++
                            }

                            if (feedState.value.image != null) {
                                sections.add(Pair(currentSection, FeedSection.IMAGE))
                                currentSection++
                            }
                            if (feedState.value.news != null) {
                                sections.add(Pair(currentSection, FeedSection.NEWS))
                                currentSection++
                            }
                            if (feedState.value.onThisDay != null) {
                                sections.add(Pair(currentSection, FeedSection.ON_THIS_DAY))
                            }

                            _feedState.update { currentState ->
                                currentState.copy(sections = sections)
                            }

                            if (!listOf(
                                    WRStatus.SUCCESS,
                                    WRStatus.NO_SEARCH_RESULT,
                                    WRStatus.NETWORK_ERROR
                                ).contains(homeScreenState.value.status) || fromBack
                            ) _homeScreenState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    status = WRStatus.FEED_LOADED
                                )
                            }
                            else _homeScreenState.update { currentState ->
                                currentState.copy(isLoading = false)
                            }
                        }

                        is ResponseResult.Error -> {
                            Log.e("Repository", "${feed.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ArticleViewModel", "Error in loading feed: ${e.message}")
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf(
                                listOf(
                                    buildAnnotatedString {
                                        append(
                                            "An error occurred, feed could not be loaded :(\nPlease " +
                                                    "check your internet connection"
                                        )
                                    }
                                )
                            ),
                            langs = null,
                            currentLang = null,
                            photo = null,
                            photoDesc = null,
                            status = WRStatus.FEED_NETWORK_ERROR,
                            pageId = null,
                            isLoading = false,
                            savedStatus = SavedStatus.NOT_SAVED
                        )
                    }
                }
            } else {
                _homeScreenState.update { currentState ->
                    currentState.copy(status = WRStatus.UNINITIALIZED)
                }
            }
        }
    }

    /**
     * Saves the current article to the given directory
     *
     * @return A [WRStatus] enum value indicating the status of the save operation
     */
    suspend fun saveArticle(
        title: String? = null,
        lang: String? = null
    ): WRStatus {
        if (homeScreenState.value.status == WRStatus.UNINITIALIZED && title == null) {
            Log.e("ArticleViewModel", "Cannot save article, HomeScreenState not initialized")
            return WRStatus.OTHER
        }

        val currentLang = lang ?: preferencesState.value.lang
        interceptor.setHost("${currentLang}.wikipedia.org")

        try {
            _homeScreenState.update { currentState ->
                currentState.copy(savedStatus = SavedStatus.SAVING)
            }
            val pageTitle = title ?: homeScreenState.value.title
            val apiResponse = articleResolver
                .getPageData(pageTitle)

            val apiResponseQuery = when (apiResponse) {
                is ResponseResult.Success -> {
                    Log.d("ArticleViewModel", "Received Page Data")
                    apiResponse.value.query?.pages?.get(0)

                }

                is ResponseResult.Error -> {
                    Log.e("ArticleViewModel", " ${apiResponse.message}")
                    null
                }
            }

            if (apiResponseQuery == null) {
                Log.e("ArticleViewModel", "Cannot save article, apiResponse is null")
                interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
                return WRStatus.NO_SEARCH_RESULT
            }

            val pageContentResult = articleResolver.getPageContent(apiResponseQuery.title)

            val pageContent = when (pageContentResult) {
                is ResponseResult.Success -> {
                    Log.d("ArticleViewModel", "Received Page Content")
                    pageContentResult.value

                }

                is ResponseResult.Error -> {
                    Log.e(
                        "ArticleViewModel",
                        "Error While fetching Page Content Result: ${pageContentResult.message}"
                    )
                    null
                }
            }

            try {
                val apiFileName =
                    "${apiResponseQuery.title}.${apiResponseQuery.pageId}-api.${currentLang}"
                val contentFileName =
                    "${apiResponseQuery.title}.${apiResponseQuery.pageId}-content.${currentLang}"

                val articlesDir = File(filesDir, "savedArticles")
                articlesDir.mkdirs()

                val apiFile = File(
                    articlesDir,
                    apiFileName
                )
                val contentFile = File(
                    articlesDir,
                    contentFileName
                )

                FileOutputStream(apiFile).use {
//                    it.write(gson.toJson(apiResponse).toByteArray())
                    it.write(Json.Default.encodeToString(apiResponse).toByteArray())
                }
                FileOutputStream(contentFile).use {
                    it.write(pageContent?.toByteArray())
                }

                if (title == null)
                    _homeScreenState.update { currentState ->
                        currentState.copy(savedStatus = SavedStatus.SAVED)
                    }
                _savedArticlesState.update { currentState ->
                    currentState.copy(
                        savedArticles = currentState.savedArticles + apiFileName,
                        articlesSize = totalArticlesSize(),
                    )
                }
                updateLanguageFilters()

                return WRStatus.SUCCESS
            } catch (e: Exception) {
                Log.e(
                    "ArticleViewModel",
                    "Cannot save article, file IO error"
                )
                e.printStackTrace()
                _homeScreenState.update { currentState ->
                    currentState.copy(savedStatus = SavedStatus.NOT_SAVED)
                }
                interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
                return WRStatus.IO_ERROR
            }
        } catch (e: Exception) {
            Log.e("ArticleViewModel", "Cannot save article, network error")
            e.printStackTrace()
            _homeScreenState.update { currentState ->
                currentState.copy(savedStatus = SavedStatus.NOT_SAVED)
            }
            interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
            return WRStatus.NETWORK_ERROR
        }

    }

    /**
     * Migrates articles downloaded in the pre-2.0 format (single file) to the new format (split api
     * and content files). The old file corresponding to an article is only deleted when the new files
     * have been successfully written, so accidental data loss is not a concern.
     *
     * It is recommended to run this function on every startup to ensure any pending migrations are
     * completed
     */
    fun migrateArticles() {
        try {
            val articleList =
                listArticles(filter = false).filterNot { it.contains("-api.") || it.contains("-content.") }
            val articlesDir = File(filesDir, "savedArticles")
            viewModelScope.launch {
                articleList.forEach { // Sequentially reload and save articles, then delete old files
                    val oldFile = File(articlesDir, it)
                    val saved =
                        saveArticle(
                            title = it.substringBefore('.'),
                            lang = it.substringAfterLast('.')
                        )
                    if (saved == WRStatus.SUCCESS) {
                        oldFile.delete()
                    }
                }
                interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
            }
        } catch (e: Exception) {
            Log.e("ArticleViewModel", "Failed to load articles list: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Deletes the current article
     *
     * @return A [WRStatus] enum value indicating the status of the delete operation
     */
    fun deleteArticle(apiFileName: String = "${homeScreenState.value.title}.${homeScreenState.value.pageId}-api.${homeScreenState.value.currentLang}"): WRStatus {
        if (homeScreenState.value.status == WRStatus.UNINITIALIZED) {
            Log.e("ArticleViewModel", "Cannot delete article, HomeScreenState is uninitialized")
            return WRStatus.OTHER
        }

        try {
            val articlesDir = File(filesDir, "savedArticles")

            val apiFile = File(articlesDir, apiFileName)
            val contentFile = File(articlesDir, apiFileName.replace("-api.", "-content."))

            val deleted = apiFile.delete() && contentFile.delete()
            if (deleted) {
                _homeScreenState.update { currentState ->
                    currentState.copy(savedStatus = SavedStatus.NOT_SAVED)
                }
                _savedArticlesState.update { currentState ->
                    currentState.copy(
                        savedArticles = currentState.savedArticles - apiFileName,
                        articlesSize = totalArticlesSize()
                    )
                }
                return WRStatus.SUCCESS
            } else return WRStatus.IO_ERROR
        } catch (e: Exception) {
            Log.e("ArticleViewModel", "Cannot delete article")
            e.printStackTrace()
            return WRStatus.IO_ERROR
        }
    }

    fun deleteAllArticles(): WRStatus {
        try {
            val articlesDir = File(filesDir, "savedArticles")
            val deleted = articlesDir.deleteRecursively()
            if (deleted) {
                _savedArticlesState.update { currentState ->
                    currentState.copy(savedArticles = emptyList(), articlesSize = 0)
                }
                updateLanguageFilters()
                return WRStatus.SUCCESS
            } else return WRStatus.IO_ERROR
        } catch (e: Exception) {
            Log.e("ArticleViewModel", "Cannot delete all articles")
            e.printStackTrace()
            return WRStatus.IO_ERROR
        }
    }

    fun listArticles(filter: Boolean = true): List<String> {
        val articlesDir = File(filesDir, "savedArticles")
        val directoryEntries = articlesDir.toPath().listDirectoryEntries()
        val out = mutableListOf<String>()
        directoryEntries.forEach {
            out.add(it.fileName.toString())
        }
        out.sort()
        return if (filter) out.filter { it.contains("-api.") } else out
    }

    fun updateArticlesList() =
        viewModelScope.launch(Dispatchers.IO) {
            _savedArticlesState.update { currentState ->
                currentState.copy(isLoading = true)
            }
            try {
                val out = listArticles()
                _savedArticlesState.update { currentState ->
                    currentState.copy(
                        savedArticles = out.filter { it.contains("-api.") },
                        articlesSize = totalArticlesSize()
                    )
                }
                updateLanguageFilters()
            } catch (e: Exception) {
                Log.e("ArticleViewModel", "Cannot load list of downloaded articles, IO error")
                e.printStackTrace()
                _savedArticlesState.update { currentState ->
                    currentState.copy(savedArticles = emptyList(), articlesSize = 0)
                }
            }
            _savedArticlesState.update { currentState ->
                currentState.copy(isLoading = false)
            }
        }

    private fun totalArticlesSize(): Long {
        try {
            val size = File(filesDir, "savedArticles")
                .walkTopDown()
                .map { it.length() }
                .sum()
            return size
        } catch (e: Exception) {
            Log.e("ArticleViewModel", "Cannot load total article size, IO error")
            e.printStackTrace()
            return 0
        }
    }

    suspend fun loadSavedArticle(apiFileName: String): WRStatus =
        withContext(Dispatchers.IO) {
            try {
                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true, loadingProgress = null)
                }
                val articlesDir = File(filesDir, "savedArticles")
                val apiFile = File(articlesDir, apiFileName)
                val contentFile = File(articlesDir, apiFileName.replace("-api.", "-content."))
                val apiResponse =
                    Json.Default.decodeFromString<WikiApiPageData>(apiFile.readText()).query?.pages?.get(
                        0
                    )

                val extract: List<String> =
                    parseSections(contentFile.readText())

                sections = extract.size
                var sectionIndex = 3
                val articleSections = mutableListOf<Pair<Int, String>>()
                val parsedExtract = mutableListOf<List<AnnotatedString>>()

                val articleState = articleListState.value
                pushBackstack(
                    title = homeScreenState.value.title,
                    firstVisibleItemIndex = articleState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = articleState.firstVisibleItemScrollOffset
                )
                _articleListState.update {
                    LazyListState(0, 0)
                }

                _preferencesState.update { currentState ->
                    currentState.copy(
                        lang = apiFileName.substringAfterLast('.')
                    )
                }

                _homeScreenState.update { currentState ->
                    currentState.copy(
                        title = apiResponse?.title ?: "Error",
                        photo = apiResponse?.photo,
                        photoDesc = apiResponse?.photoDesc,
                        langs = apiResponse?.langs,
                        currentLang = preferencesState.value.lang,
                        pageId = apiResponse?.pageId,
                        backStackSize = backStack.size,
                        status = WRStatus.SUCCESS,
                        savedStatus = SavedStatus.SAVED
                    )
                }

                extract.forEachIndexed { index, it ->
                    currentSection = index + 1
                    val parsed = parseWikitext(it)
                    if (index % 2 == 1) {
                        articleSections.add(
                            Pair(
                                sectionIndex,
                                parsed.joinToString(separator = "").parseAsHtml().toString()
                            )
                        )
                        sectionIndex += 2
                    }
                    parsedExtract.add(parsed)
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            loadingProgress = currentSection.toFloat() / sections,
                            extract = parsedExtract,
                            sections = articleSections
                        )
                    }
                }

                _homeScreenState.update { currentState ->
                    currentState.copy(
                        isLoading = false
                    )
                }

                return@withContext WRStatus.SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext WRStatus.IO_ERROR
            }
        }

    fun updateLanguageFilters() {
        val languageFilters = mutableSetOf<String>()
        savedArticlesState.value.savedArticles.forEach {
            languageFilters.add(it.substringAfterLast('.'))
        }
        _savedArticlesState.update { currentState ->
            currentState.copy(
                languageFilters =
                    languageFilters
                        .toList()
                        .map {
                            LanguageFilterOption(
                                langCodeToName(it), it
                            )
                        }
                        .sortedBy { it.option }
            )
        }
    }

    suspend fun parseWikitext(wikitext: String): List<AnnotatedString> =
        withContext(Dispatchers.IO) {
            val parsed = cleanUpWikitext(wikitext)
            var curr = ""
            var i = 0
            var stack = 0
            val out = mutableListOf<AnnotatedString>()

            while (i < parsed.length) {
                if (parsed[i] == '{')
                    stack++
                else if (parsed[i] == '}')
                    stack--

                if (parsed[i] == '<') {
                    var currSubstring = parsed.substring(i, min(i + 16, parsed.length))
                    if (currSubstring.startsWith("<math display")) {
                        currSubstring = parsed.substring(i).substringBefore("</math>")
                        out.add(
                            curr.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                loadPage = ::loadPage,
                                fontSize = preferencesState.value.fontSize
                            )
                        )
                        out.add(androidx.compose.ui.text.AnnotatedString(currSubstring))
                        i += currSubstring.length + 7
                        curr = ""
                    } else if (currSubstring.startsWith("<gallery")) {
                        currSubstring = parsed.substring(i).substringBefore("</gallery>")
                        out.add(
                            curr.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                loadPage = ::loadPage,
                                fontSize = preferencesState.value.fontSize
                            )
                        )
                        out.add(androidx.compose.ui.text.AnnotatedString(currSubstring))
                        i += currSubstring.length + 10
                        curr = ""
                    } else curr += parsed[i]
                } else if (stack == 0 && parsed[i] == '[' && parsed.getOrNull(i + 1) == '[') {
                    val currSubstring = parsed.substringMatchingParen('[', ']', i)
                    if (currSubstring.contains(':')) {
                        if (currSubstring
                                .matches(
                                    ".*\\.jpg.*|.*\\.jpeg.*|.*\\.png.*|.*\\.svg.*|.*\\.gif.*"
                                        .toRegex(RegexOption.IGNORE_CASE)
                                )
                        ) {
                            out.add(
                                curr.toWikitextAnnotatedString(
                                    colorScheme = colorScheme,
                                    typography = typography,
                                    loadPage = ::loadPage,
                                    fontSize = preferencesState.value.fontSize
                                )
                            )
                            out.add(
                                buildAnnotatedString {
                                    append("[[File:")
                                    append(currSubstring.substringAfter(':').substringBefore('|'))
                                    append('|')
                                    append(
                                        currSubstring.substringAfter('|').substringBeforeLast("]]")
                                            .split('|')
                                            .filterNot { it.matches("thumb|thumbnail|frame|frameless|border|baseline|class=.*|center|left|right|upright.*|.+px|alt=.*".toRegex()) }
                                            .joinToString("|")
                                    )
                                    if (currSubstring.contains("class=skin-invert-image")) {
                                        append("|invert")
                                    }
                                }
                            )
                            curr = ""
                            i += currSubstring.length - 1
                        } else
                            curr += parsed[i]
                    } else {
                        curr += parsed[i]
                    }
                } else if (parsed[i] == '{' && parsed.getOrNull(i + 1) == '|') {
                    val currSubstring = parsed.substringMatchingParen('{', '}', i)
                    if (!currSubstring.substring(min(i + 2, currSubstring.lastIndex))
                            .contains("{|")
                    ) {
                        out.add(
                            curr.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                loadPage = ::loadPage,
                                fontSize = preferencesState.value.fontSize
                            )
                        )
                        out.add(androidx.compose.ui.text.AnnotatedString(currSubstring))
                        curr = ""
                        i += currSubstring.length
                    } else {
                        val currSubstringNestedTable =
                            parsed.substringMatchingParen('{', '}', parsed.indexOf("{|", i + 2))
                        out.add(
                            curr.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                loadPage = ::loadPage,
                                fontSize = preferencesState.value.fontSize
                            )
                        )
                        out.add(androidx.compose.ui.text.AnnotatedString(currSubstringNestedTable))
                        curr = ""
                        i += currSubstring.length
                    }
                } else curr += parsed[i]
                i++
            }
            out.add(
                curr.toWikitextAnnotatedString(
                    colorScheme = colorScheme,
                    typography = typography,
                    loadPage = { loadPage(it) },
                    fontSize = preferencesState.value.fontSize
                )
            )
            out.toList()
        }

    fun focusSearchBar() {
        appSearchBarState.value.focusRequester.requestFocus()
    }

    fun removeHistoryItem(item: String) {
        viewModelScope.launch {
            val history = appSearchBarState.value.history.toMutableSet()
            history.remove(item)
            _appSearchBarState.update { currentState ->
                currentState.copy(history = history)
            }
            appPreferencesRepository.saveHistory(history)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _appSearchBarState.update { currentState ->
                currentState.copy(history = emptySet())
            }
            appPreferencesRepository.saveHistory(emptySet())
        }
    }

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    theme = appPreferencesRepository
                        .saveStringPreference("theme", theme)
                )
            }
        }
    }

    fun saveFontStyle(fontStyle: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    fontStyle = appPreferencesRepository
                        .saveStringPreference("font-style", fontStyle)
                )
            }
        }
    }

    fun saveLang(lang: String) {
        interceptor.setHost("$lang.wikipedia.org")
        _preferencesState.update { currentState ->
            currentState.copy(
                lang = lang
            )
        }
        viewModelScope.launch {
            appPreferencesRepository.saveStringPreference("lang", lang)
        }
    }

    fun saveColorScheme(colorScheme: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    colorScheme = appPreferencesRepository
                        .saveStringPreference("color-scheme", colorScheme)
                )
            }
        }
    }

    fun saveBlackTheme(blackTheme: Boolean) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    blackTheme = appPreferencesRepository
                        .saveBooleanPreference("black-theme", blackTheme)
                )
            }
        }
    }

    fun saveFontSize(fontSize: Int) {
        viewModelScope.launch {
            appPreferencesRepository.saveIntPreference("font-size", fontSize)
            _preferencesState.update { currentState ->
                currentState.copy(fontSize = fontSize)
            }
        }
    }

    fun saveExpandedSections(expandedSections: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("expanded-sections", expandedSections)
            _preferencesState.update { currentState ->
                currentState.copy(expandedSections = expandedSections)
            }
        }
    }

    fun saveRenderMath(renderMath: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("render-math", renderMath)
            _preferencesState.update { currentState ->
                currentState.copy(renderMath = renderMath)
            }
        }
    }

    fun saveSearchHistory(searchHistory: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("search-history", searchHistory)
            _preferencesState.update { currentState ->
                currentState.copy(searchHistory = searchHistory)
            }
        }
    }

    fun saveDataSaver(dataSaver: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("data-saver", dataSaver)
            _preferencesState.update { currentState ->
                currentState.copy(dataSaver = dataSaver)
            }
        }
    }

    fun saveImageBackground(imageBackground: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("image-background", imageBackground)
            _preferencesState.update { currentState ->
                currentState.copy(imageBackground = imageBackground)
            }
        }
    }

    fun saveImmersiveMode(immersiveMode: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("immersive-mode", immersiveMode)
            _preferencesState.update { currentState ->
                currentState.copy(immersiveMode = immersiveMode)
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            try {
                appPreferencesRepository.resetSettings()
                _preferencesState.update {
                    PreferencesState()
                }
            } catch (e: Exception) {
                Log.e("ArticleViewModel", "Error in restoring settings: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateLanguageSearchStr(str: String) {
        _languageSearchStr.update {
            str
        }
    }
}