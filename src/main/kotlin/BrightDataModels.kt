package org.example

import kotlinx.serialization.Serializable

/**
 * Data classes for BrightData API
 */

@Serializable
data class BrightDataRequest(
    val zone: String,
    val url: String,
    val format: String,
    val dataFormat: String? = null,
)

@Serializable
data class WebSearchResult(
    val organic: List<OrganicResult>,
) {
    @Serializable
    data class OrganicResult(
        val link: String,
        val title: String,
        val description: String,
        val rank: Int,
        val globalRank: Int,
    )
}

@Serializable
data class WebPageScrapingResult(
    val body: String,
)
