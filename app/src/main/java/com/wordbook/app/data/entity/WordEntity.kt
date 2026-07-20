package com.wordbook.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val pronunciation: String,
    val explanation: String,
    val sentence1: String,
    val translation1: String,
    val sentence2: String,
    val translation2: String
)
