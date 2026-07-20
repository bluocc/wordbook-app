package com.wordbook.app.data.dao

import androidx.room.*
import com.wordbook.app.data.entity.LearningProgress
import com.wordbook.app.data.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT * FROM words WHERE id NOT IN (SELECT wordId FROM learning_progress) ORDER BY RANDOM() LIMIT :count")
    suspend fun getUnlearnedWords(count: Int): List<WordEntity>

    @Query("""
        SELECT w.* FROM words w 
        INNER JOIN learning_progress lp ON w.id = lp.wordId 
        WHERE lp.nextReview <= :now 
        ORDER BY lp.nextReview ASC 
        LIMIT :count
    """)
    suspend fun getDueWords(now: Long, count: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LearningProgress)

    @Query("SELECT * FROM learning_progress WHERE wordId = :wordId")
    suspend fun getProgress(wordId: Long): LearningProgress?

    @Query("""
        SELECT w.* FROM words w 
        INNER JOIN learning_progress lp ON w.id = lp.wordId 
        ORDER BY lp.addedToPool DESC
    """)
    suspend fun getAllPoolWords(): List<WordEntity>

    @Query("SELECT * FROM learning_progress WHERE wordId = :wordId")
    fun observeProgress(wordId: Long): Flow<LearningProgress?>

    @Query("""
        SELECT lp.* FROM learning_progress lp
        WHERE lp.addedToPool > 0
        ORDER BY lp.addedToPool DESC
    """)
    suspend fun getAllProgress(): List<LearningProgress>

    @Query("SELECT COUNT(*) FROM learning_progress WHERE lastReview > :startOfDay AND lastReview < :endOfDay")
    suspend fun getReviewedCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM learning_progress WHERE addedToPool > :startOfDay AND addedToPool < :endOfDay")
    suspend fun getAddedCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT addedToPool FROM learning_progress WHERE addedToPool > 0")
    suspend fun getAllAddedDates(): List<Long>
}
