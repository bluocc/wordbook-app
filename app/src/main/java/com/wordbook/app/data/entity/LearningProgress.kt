package com.wordbook.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_progress",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LearningProgress(
    @PrimaryKey val wordId: Long,
    val easeFactor: Float = 2.5f,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val nextReview: Long = 0,
    val lastReview: Long = 0,
    val lastQuality: Int = 0,
    val addedToPool: Long = 0
)
