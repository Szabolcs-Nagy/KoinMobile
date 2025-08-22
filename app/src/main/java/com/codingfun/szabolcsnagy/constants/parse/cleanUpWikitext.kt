package com.codingfun.szabolcsnagy.constants.parse

/**
 * Remove parts of a Wikitext section that are not to be rendered
 *
 * @param wikitext Source Wikitext to clean up
 */
fun cleanUpWikitext(wikitext: String): String {
    var bodyText = wikitext
        .replace(
            "<ref[^/]*?>.+?</ref>|<ref.*?/>".toRegex(RegexOption.DOT_MATCHES_ALL),
            ""
        )
    bodyText = bodyText.replace("<var>|</var>".toRegex(), "''")
    return bodyText
}