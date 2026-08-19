package abdullah.bari.asif.crawler

import abdullah.bari.asif.model.ElementSelectors
import abdullah.bari.asif.model.NewsSource
import abdullah.bari.asif.model.SourceLogic
import abdullah.bari.asif.model.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CustomHtmlFetcherTest {

    private val fetcher = CustomHtmlFetcher()

    private val nihSource = NewsSource(
        id = "nih_news_custom",
        name = "NIH Research Matters",
        baseUrl = "https://www.nih.gov",
        imageUrl = "https://www.nih.gov/favicon.ico",
        updatedAt = "2025.02.18.12.00",
        fetchUrl = "https://www.nih.gov/news-events/news-releases",
        logic = SourceLogic(
            type = SourceType.CUSTOM_HTML,
            selectors = ElementSelectors(
                containerSelector = "li.news-item",
                titleSelector = "h3.title a",
                linkSelector = "h3.title a[href]",
                imageSelector = "img.thumbnail",
                dateSelector = "span.date"
            )
        )
    )

    @Test
    fun testParseCustomHtmlWebpage() {
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <body>
                <ul class="news-list">
                    <li class="news-item">
                        <img class="thumbnail" src="/sites/default/files/images/gene_therapy.jpg" alt="Gene Therapy"/>
                        <h3 class="title">
                            <a href="/news-events/news-releases/gene-therapy-advancements-2025">Gene Therapy Shows Promise in Clinical Trials</a>
                        </h3>
                        <span class="date">February 18, 2025</span>
                        <p class="summary">New findings published by NIH researchers.</p>
                    </li>
                    <li class="news-item">
                        <img class="thumbnail" src="https://www.nih.gov/sites/default/files/images/brain.jpg" alt="Brain Study"/>
                        <h3 class="title">
                            <a href="https://www.nih.gov/news-events/news-releases/mapping-brain-connections">Mapping Neural Connections in Human Brain</a>
                        </h3>
                        <span class="date">February 16, 2025</span>
                    </li>
                </ul>
            </body>
            </html>
        """.trimIndent()

        val articles = fetcher.parseCustomHtml(htmlContent, nihSource)

        assertEquals(2, articles.size)

        val first = articles[0]
        assertEquals("Gene Therapy Shows Promise in Clinical Trials", first.title)
        assertEquals("https://www.nih.gov/news-events/news-releases/gene-therapy-advancements-2025", first.articleUrl)
        assertEquals("https://www.nih.gov/sites/default/files/images/gene_therapy.jpg", first.imageUrl)
        assertEquals("February 18, 2025", first.publishedAt)
        assertEquals("nih_news_custom", first.sourceId)
        assertNotNull(first.id)

        val second = articles[1]
        assertEquals("Mapping Neural Connections in Human Brain", second.title)
        assertEquals("https://www.nih.gov/news-events/news-releases/mapping-brain-connections", second.articleUrl)
        assertEquals("https://www.nih.gov/sites/default/files/images/brain.jpg", second.imageUrl)
        assertEquals("February 16, 2025", second.publishedAt)
    }

    @Test
    fun testParseCustomHtmlFallbackWhenOptionalSelectorsMissing() {
        val htmlContent = """
            <div>
                <article class="post">
                    <h3><a href="/post-1">First Post Title</a></h3>
                </article>
            </div>
        """.trimIndent()

        val minimalSource = NewsSource(
            id = "custom_minimal",
            name = "Minimal Custom Source",
            baseUrl = "https://example.com",
            imageUrl = "https://example.com/icon.png",
            updatedAt = "2025.02.18.12.00",
            fetchUrl = "https://example.com/news",
            logic = SourceLogic(
                type = SourceType.CUSTOM_HTML,
                selectors = ElementSelectors(
                    containerSelector = "article.post",
                    titleSelector = "h3 a",
                    linkSelector = "a"
                )
            )
        )

        val articles = fetcher.parseCustomHtml(htmlContent, minimalSource)

        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals("First Post Title", article.title)
        assertEquals("https://example.com/post-1", article.articleUrl)
        assertTrue(article.publishedAt.isNotBlank())
    }
}
