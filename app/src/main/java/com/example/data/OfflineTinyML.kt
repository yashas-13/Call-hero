package com.example.data

object OfflineTinyML {
    fun analyzeSentiment(transcript: String): String {
        val text = transcript.lowercase()
        val positiveWords = setOf(
            "great", "fantastic", "excellent", "awesome", "good", "perfect", "happy", "love", 
            "resolved", "success", "incredible", "thanks", "thank", "helpful", "appreciate", 
            "beautiful", "clean", "smooth", "recommend", "solved", "pleased", "fine", "super",
            "valuable", "happy", "excited", "stellar", "nice", "indeed"
        )
        val negativeWords = setOf(
            "worried", "block", "longer", "delay", "milestone", "escalate", "problem", "issue", 
            "failing", "broken", "crash", "rejecting", "reject", "error", "bad", "poor", 
            "concern", "unhappy", "disappointed", "frustrated", "failed", "fail", "slow", "down",
            "blocking", "worry", "unfortunate", "difficult", "timeout", "rejects", "angry",
            "complaint", "issue", "blocker"
        )
        
        var score = 0
        // Simple token counting
        val tokens = text.split(Regex("[\\s,.;:!?()]+"))
        for (token in tokens) {
            if (positiveWords.contains(token)) {
                score++
            } else if (negativeWords.contains(token)) {
                score--
            }
        }
        
        return when {
            score > 0 -> "Positive"
            score < 0 -> "Negative"
            else -> "Neutral"
        }
    }
}
