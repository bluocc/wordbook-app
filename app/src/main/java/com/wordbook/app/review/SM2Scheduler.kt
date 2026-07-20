package com.wordbook.app.review

import com.wordbook.app.data.entity.LearningProgress

object SM2Scheduler {

    fun calculate(quality: Int, now: Long, wordId: Long, prev: LearningProgress?): LearningProgress {
        val ef = prev?.easeFactor ?: 2.5f
        val reps = prev?.repetitions ?: 0
        val currentInterval = prev?.interval ?: 0

        return if (quality >= 3) {
            val newInterval = when {
                reps == 0 -> 1
                reps == 1 -> 6
                else -> (currentInterval * ef).toInt().coerceAtLeast(1)
            }
            val efDelta = 0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)
            val newEase = (ef + efDelta).coerceAtLeast(1.3f)

            LearningProgress(
                wordId = wordId,
                easeFactor = newEase,
                interval = newInterval,
                repetitions = reps + 1,
                nextReview = now + newInterval * 24 * 60 * 60 * 1000L,
                lastReview = now,
                lastQuality = quality,
                addedToPool = prev?.addedToPool ?: now
            )
        } else {
            LearningProgress(
                wordId = wordId,
                easeFactor = ef,
                interval = 1,
                repetitions = 0,
                nextReview = now + 24 * 60 * 60 * 1000L,
                lastReview = now,
                lastQuality = quality,
                addedToPool = prev?.addedToPool ?: now
            )
        }
    }
}
