package abdullah.bari.asif.crawler

import it.skrape.core.htmlDocument
import it.skrape.selects.Doc
import it.skrape.selects.DocElement

expect fun generateArticleId(url: String): String

expect fun parseXmlSafe(xmlContent: String): Doc?

internal fun resolveUrl(baseUrl: String, relativeUrl: String): String {
    val trimmedRelative = relativeUrl.trim()
    if (trimmedRelative.startsWith("http://") || trimmedRelative.startsWith("https://")) {
        return trimmedRelative
    }
    val cleanBase = baseUrl.trimEnd('/')
    return if (trimmedRelative.startsWith("/")) {
        "$cleanBase$trimmedRelative"
    } else {
        "$cleanBase/$trimmedRelative"
    }
}

internal fun String.cleanCdataAndEntities(): String {
    var result = this.trim()
    if (result.startsWith("<![CDATA[") && result.endsWith("]]>")) {
        result = result.substring(9, result.length - 3)
    }
    return result
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .trim()
}

internal fun parseHtmlSafe(htmlContent: String): Doc? {
    return try {
        htmlDocument(htmlContent)
    } catch (e: Exception) {
        null
    }
}

internal fun Doc.findAllSafe(cssSelector: String): List<DocElement> {
    return try {
        findAll(cssSelector)
    } catch (e: Exception) {
        emptyList()
    }
}

internal fun DocElement.findAllSafe(cssSelector: String): List<DocElement> {
    return try {
        findAll(cssSelector)
    } catch (e: Exception) {
        emptyList()
    }
}

internal fun DocElement.findFirstSafe(cssSelector: String): DocElement? {
    return try {
        findFirst(cssSelector)
    } catch (e: Exception) {
        null
    }
}

internal fun DocElement.getAttr(key: String): String? {
    return attributes[key]?.trim()?.ifBlank { null }
}
