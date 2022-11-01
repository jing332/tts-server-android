package com.github.jing332.tts_server_android.util

/**
 * Construct the normalization utility, allow the normalization range to be specified.
 * @param dataHigh The high value for the input data.
 * @param dataLow The low value for the input data.
 * @param dataHigh The high value for the normalized data.
 * @param dataLow The low value for the normalized data.
 */
class NormUtil(
    var dataHigh: Float,
    var dataLow: Float,
    var normalizedHigh: Float,
    var normalizedLow: Float
) {

    /**
     * Normalize x.
     * @param x The value to be normalized.
     * @return The result of the normalization.
     */
    fun normalize(x: Float): Float {
        return ((x - dataLow) / (dataHigh - dataLow)) * normalizedHigh - normalizedLow + normalizedLow
    }
}