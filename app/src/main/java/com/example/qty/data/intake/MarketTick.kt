package com.example.qty.data.intake

/**
 * Authentic Market Tick representation in QtY.
 *
 * Core Data Rules:
 * - Must use authentic market data with source identity, timestamps, and chronological ordering.
 * - Never fabricate market data.
 * - Millisecond server timestamps must reflect true exchange event time.
 */
data class MarketTick(
    val timestampMs: Long,
    val price: Double,
    val volume: Double,
    val sourceIdentity: String,
    val sequenceId: Long,
    val receivedAtLocalMs: Long = System.currentTimeMillis()
) {
    init {
        require(price > 0.0) { "Authentic price must be strictly positive: $price" }
        require(volume >= 0.0) { "Authentic volume cannot be negative: $volume" }
        require(timestampMs > 0L) { "Authentic timestamp must be positive: $timestampMs" }
        require(sourceIdentity.isNotBlank()) { "Source identity must be explicitly defined" }
    }

    val latencyMs: Long
        get() = (receivedAtLocalMs - timestampMs).coerceAtLeast(0L)
}
