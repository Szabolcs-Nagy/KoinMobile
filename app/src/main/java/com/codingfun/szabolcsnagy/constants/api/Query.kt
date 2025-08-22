package com.codingfun.szabolcsnagy.constants.api

internal const val API_QUERY =
    "w/api.php?format=json&action=query&prop=pageimages|pageterms|langlinks&piprop=original&pilicense=any&lllimit=max&redirects=1&formatversion=2&maxage=900&smaxage=900"

internal const val CONTENT_QUERY =
    "wiki/{title}?action=raw&maxage=900&smaxage=900"

internal const val FEED_QUERY =
    "api/rest_v1/feed/featured/{date}"

internal const val PREFIX_SEARCH_QUERY =
    "w/api.php?action=query&generator=prefixsearch&prop=pageimages|pageterms&piprop=thumbnail&pithumbsize=128&wbptterms=description&gpslimit=20&format=json&formatversion=2&maxage=900&smaxage=900"

internal const val RANDOM_QUERY =
    "w/api.php?format=json&action=query&prop=pageimages|pageterms|langlinks&piprop=original&pilicense=any&lllimit=max&generator=random&redirects=1&formatversion=2&grnnamespace=0"

internal const val SEARCH_QUERY =
    "w/api.php?action=query&generator=search&prop=pageimages&piprop=thumbnail&pithumbsize=128&gsrnamespace=0&gsrlimit=20&gsrprop=snippet|titlesnippet|redirecttitle|extensiondata&format=json&formatversion=2&maxage=900&smaxage=900"