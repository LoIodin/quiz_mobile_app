package com.example.interviewhelper.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class QuestionCategory(
    val id: String,
    val name: String,
    val description: String,
    val questions: List<Question>
)

data class QuestionsData(
    val categories: List<QuestionCategory>
)

class QuestionRepository(private val context: Context) {
    private val gson = Gson()

    fun loadQuestions(): QuestionsData {
        try {
            val jsonString = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            return gson.fromJson(jsonString, QuestionsData::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            return QuestionsData(emptyList()) // Возвращаем пустой список в случае ошибки
        }
    }
} 