package com.wordbook.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wordbook.app.data.dao.WordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object JsonLoader {

    private var isLoaded = false

    suspend fun loadIfNeeded(context: Context, dao: WordDao) {
        if (isLoaded) return
        val count = dao.getWordCount()
        if (count > 0) {
            isLoaded = true
            return
        }
        withContext(Dispatchers.IO) {
            val json = context.assets.open("wordbook.json").bufferedReader().readText()
            val type = object : TypeToken<List<RawWord>>() {}.type
            val rawWords: List<RawWord> = Gson().fromJson(json, type)
            val entities = rawWords.map { raw ->
                com.wordbook.app.data.entity.WordEntity(
                    word = raw.W ?: "",
                    pronunciation = raw.P ?: "",
                    explanation = raw.E ?: "",
                    sentence1 = raw.S1 ?: "",
                    translation1 = raw.T1 ?: "",
                    sentence2 = raw.S2 ?: "",
                    translation2 = raw.T2 ?: ""
                )
            }
            dao.insertWords(entities)
            isLoaded = true
        }
    }

    private data class RawWord(
        val W: String?,
        val P: String?,
        val E: String?,
        val S1: String?,
        val T1: String?,
        val S2: String?,
        val T2: String?
    )
}
