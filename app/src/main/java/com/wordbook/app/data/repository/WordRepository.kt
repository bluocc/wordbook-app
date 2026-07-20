package com.wordbook.app.data.repository

import com.wordbook.app.data.JsonLoader
import com.wordbook.app.data.dao.WordDao
import com.wordbook.app.data.entity.LearningProgress
import com.wordbook.app.data.entity.WordEntity
import kotlinx.coroutines.flow.Flow

class WordRepository(private val dao: WordDao) {

    suspend fun loadWordData(context: android.content.Context) {
        JsonLoader.loadIfNeeded(context, dao)
    }

    suspend fun getWordCount(): Int = dao.getWordCount()

    suspend fun getUnlearnedWords(count: Int): List<WordEntity> =
        dao.getUnlearnedWords(count)

    suspend fun getDueWords(now: Long, count: Int): List<WordEntity> =
        dao.getDueWords(now, count)

    suspend fun getWordById(id: Long): WordEntity? = dao.getWordById(id)

    suspend fun getProgress(wordId: Long): LearningProgress? = dao.getProgress(wordId)

    suspend fun upsertProgress(progress: LearningProgress) = dao.upsertProgress(progress)

    suspend fun getAllPoolWords(): List<WordEntity> = dao.getAllPoolWords()

    suspend fun getAllProgress(): List<LearningProgress> = dao.getAllProgress()

    fun observeProgress(wordId: Long): Flow<LearningProgress?> = dao.observeProgress(wordId)

    suspend fun getAddedDates(): List<Long> = dao.getAllAddedDates()
}
