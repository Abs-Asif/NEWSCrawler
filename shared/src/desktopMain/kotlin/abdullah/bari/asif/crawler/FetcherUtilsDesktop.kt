package abdullah.bari.asif.crawler

import it.skrape.core.htmlDocument
import it.skrape.selects.Doc
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.security.MessageDigest

actual fun generateArticleId(url: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(url.trim().toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

actual fun parseXmlSafe(xmlContent: String): Doc? {
    return try {
        val jsoupDoc = Jsoup.parse(xmlContent, "", Parser.xmlParser())
        Doc(jsoupDoc)
    } catch (e: Exception) {
        try {
            htmlDocument(xmlContent)
        } catch (ex: Exception) {
            null
        }
    }
}
