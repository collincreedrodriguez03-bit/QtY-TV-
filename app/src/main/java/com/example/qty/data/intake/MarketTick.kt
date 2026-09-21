package com.example.qty.data.intake

/**
 * Authentic Market Tick representation in QtY.
 *
 * Core Data Rules:
 * - Must use authentic market data with source identity, timestamps, and chronological ordering.
 * - Never fabricate market data.
 * - Distinguishes between exchange event timestamp, local receipt timestamp, server synchronization timestamp,
 *   and explicitly reports whether volume was supplied by the exchange source or unavailable (failing closed rather than fabricating).
 */
data class MarketTick(
    val exchangeTimestampMs: Long?, // Authentic exchange trade event timestamp if supplied by source
    val localReceiptTimestampMs: Long = System.currentTimeMillis(), // Local system receipt timestamp
    val serverSyncTimestampMs: Long?, // Synchronized server time (e.g. Binance /api/v3/time)
    val price: Double,
    val volume: Double?, // Authentic trade volume if supplied; null if ticker feed does not provide trade volume
    val sourceIdentity: String,
    val sequenceId: Long
) {
    init {
        require(price > 0.0) { "Authentic price must be strictly positive: $price" }
        require(volume == null || volume >= 0.0) { "Authentic volume cannot be negative: $volume" }
        require(sourceIdentity.isNotBlank()) { "Source identity must be explicitly defined" }
    }

    /**
     * The primary authoritative timestamp for chronological processing and regression analysis.
     * Prefers exchange event timestamp, falling back to server synchronization timestamp.
     * Fails closed (returns null) if no authentic timestamp is available.
     */
    val timestampMs: Long
        get() = exchangeTimestampMs ?: serverSyncTimestampMs ?: throw IllegalStateException("No authentic timestamp source available for tick $sequenceId")

    val hasAuthenticExchangeTimestamp: Boolean
        get() = exchangeTimestampMs != null

    val hasAuthenticVolume: Boolean
        get() = volume != null

    val latencyMs: Long
        get() = (localReceiptTimestampMs - timestampMs).coerceAtLeast(0L)
}
