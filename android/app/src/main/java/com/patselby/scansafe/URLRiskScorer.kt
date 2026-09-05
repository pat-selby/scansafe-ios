package com.patselby.scansafe

import java.net.URI

enum class Verdict {
    GREEN, YELLOW, RED
}

data class RiskResult(
    val score: Int,
    val verdict: Verdict,
    val reason: String
)

object URLRiskScorer {

    private val IP_PATTERN = Regex("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")
    private val BRAND_SPOOFS = listOf("paypa1", "amaz0n", "g00gle", "faceb00k", "micr0s0ft", "app1e")

    fun evaluate(urlStr: String): RiskResult {
        var score = 0
        val reasons = mutableListOf<String>()

        // Ensure URL has a scheme for parsing purposes
        val parseableUrl = if (!urlStr.startsWith("http://", ignoreCase = true) && 
                               !urlStr.startsWith("https://", ignoreCase = true)) {
            "http://$urlStr"
        } else {
            urlStr
        }

        val uri = try {
            URI(parseableUrl)
        } catch (e: Exception) {
            return RiskResult(6, Verdict.RED, "Invalid URL format")
        }

        val host = uri.host ?: ""
        val path = uri.path ?: ""
        val hostLower = host.lowercase()

        // Rule 1: URL is an IP address (not domain) (+3)
        if (IP_PATTERN.matches(host)) {
            score += 3
            reasons.add("Legitimate services use domain names (IP address detected)")
        }

        // Rule 2: Brand name + character substitution (+3)
        val hasBrandSpoof = BRAND_SPOOFS.any { hostLower.contains(it) }
        if (hasBrandSpoof) {
            score += 3
            reasons.add("Classic phishing pattern (brand name substitution)")
        }

        // Rule 3: HTTP instead of HTTPS (+2)
        if (urlStr.startsWith("http://", ignoreCase = true) || 
            (!urlStr.startsWith("https://", ignoreCase = true) && parseableUrl.startsWith("http://", ignoreCase = true))) {
            score += 2
            reasons.add("No transport encryption (HTTP used)")
        }

        // Rule 4: Subdomain count > 3 (+2)
        // A simple heuristic: count dots in the host. 
        // Example: a.b.c.example.com has 4 dots -> 3 subdomains + domain + tld
        val dotCount = host.count { it == '.' }
        if (dotCount > 3 && !IP_PATTERN.matches(host)) {
            score += 2
            reasons.add("Common in phishing infrastructure (too many subdomains)")
        }

        // Rule 5: Path length > 50 characters (+1)
        if (path.length > 50) {
            score += 1
            reasons.add("Obfuscation indicator (long path)")
        }

        // Rule 6: Random-looking domain (high consonant ratio) (+1)
        val lettersOnly = host.filter { it.isLetter() }
        if (lettersOnly.isNotEmpty()) {
            val consonants = lettersOnly.lowercase().count { it !in listOf('a', 'e', 'i', 'o', 'u', 'y') }
            val ratio = consonants.toDouble() / lettersOnly.length
            if (ratio > 0.75) {
                score += 1
                reasons.add("Generated domain pattern (high consonant ratio)")
            }
        }

        // Score map
        val verdict = when {
            score >= 6 -> Verdict.RED
            score >= 3 -> Verdict.YELLOW
            else -> Verdict.GREEN
        }

        val finalReason = if (score == 0) "Safe URL" else reasons.joinToString(" • ")

        return RiskResult(score, verdict, finalReason)
    }
}
