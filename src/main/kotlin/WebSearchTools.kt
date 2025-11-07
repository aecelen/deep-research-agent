package org.example

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Web search and scraping tools using BrightData API.
 *
 * @property brightDataKey The BrightData API key for authentication
 */
class WebSearchTools(
    private val brightDataKey: String,
) : ToolSet {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        explicitNulls = false
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    private val httpClient = HttpClient {
        defaultRequest {
            url("https://api.brightdata.com/request")
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $brightDataKey")
        }

        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Search for a query on Google using BrightData SERP API.
     *
     * @param query The search query string
     * @return Web search results containing organic search results
     */
    @Tool
    @LLMDescription("Search for a query on Google.")
    @Suppress("unused")
    suspend fun search(
        @LLMDescription("The query to search")
        query: String,
    ): WebSearchResult {
        println("[DEBUG] Searching for: $query")

        val url = URLBuilder("https://www.google.com/search")
            .apply {
                parameters.append("brd_json", "1")
                parameters.append("q", query)
            }.buildString()


        val request = BrightDataRequest(
            zone = "serp_api1",
            url = url,
            format = "raw",
        )

        val response = httpClient.post {
            setBody(request)
        }

        println("[DEBUG] Response status: ${response.status}")

        val result = response.body<WebSearchResult>()

        return result
    }

    /**
     * Scrape a web page and convert it to markdown format.
     *
     * @param url The URL of the web page to scrape
     * @return The scraped content in markdown format
     */
    @Tool
    @LLMDescription("Scrape a web page for content")
    suspend fun scrape(
        @LLMDescription("The URL to scrape")
        url: String,
    ): WebPageScrapingResult {

        val request = BrightDataRequest(
            zone = "web_unlocker1",
            url = url,
            format = "json",
            dataFormat = "markdown",
        )

        val response = httpClient.post {
            setBody(request)
        }

        println("[DEBUG] Scrape status: ${response.status}")

        val result = response.body<WebPageScrapingResult>()

        return result
    }
}
