package com.arsvechkarev.strictbrowser.extensions

fun String.toDuckDuckGoSearchUrl(): String {
    return "https://duckduckgo.com/?q=$this"
}
