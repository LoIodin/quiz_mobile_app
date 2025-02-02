package com.example.interviewhelper

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Checkbox
//import androidx.compose.material3.Switch
import com.example.interviewhelper.data.Question
import com.example.interviewhelper.data.QuestionCategory
import com.example.interviewhelper.data.QuestionRepository
//import com.example.interviewhelper.data.QuestionsData
import com.example.interviewhelper.ui.theme.InterviewHelperTheme

class MainActivity : ComponentActivity() {
    private lateinit var questionRepository: QuestionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionRepository = QuestionRepository(this)

        setContent {
            InterviewHelperTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val questionsData = remember { questionRepository.loadQuestions() }
                    InterviewApp(questionsData.categories)
                }
            }
        }
    }
}

@Composable
fun InterviewApp(categories: List<QuestionCategory>) {
    var selectedCategories by remember { mutableStateOf<List<QuestionCategory>>(emptyList()) }

    if (selectedCategories.isEmpty()) {
        CategorySelection(
            categories = categories,
            onCategoriesSelected = { categories ->
                selectedCategories = categories
            }
        )
    } else {
        // Здесь вы можете реализовать логику для отображения вопросов из выбранных категорий
        // Например, можно объединить вопросы из всех выбранных категорий
        val allQuestions = selectedCategories.flatMap { it.questions }
        QuizScreen(questions = allQuestions, onBackToCategories = { selectedCategories = emptyList() })
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CategorySelection(
    categories: List<QuestionCategory>,
    onCategoriesSelected: (List<QuestionCategory>) -> Unit
) {
    val selectedCategories by remember { mutableStateOf(mutableSetOf<QuestionCategory>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Выберите категории",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedCategories.contains(category)) {
                                selectedCategories.remove(category)
                            } else {
                                selectedCategories.add(category)
                            }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedCategories.contains(category),
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedCategories.add(category)
                            } else {
                                selectedCategories.remove(category)
                            }
                        }
                    )
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Button(
            onClick = { onCategoriesSelected(selectedCategories.toList()) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Начать тест")
        }
    }
}

@Composable
fun QuizScreen(
    questions: List<Question>,
    onBackToCategories: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    if (currentQuestionIndex < questions.size) {
        QuestionCard(
            question = questions[currentQuestionIndex],
            onAnswerSelected = { selectedAnswer ->
                if (selectedAnswer == questions[currentQuestionIndex].correctAnswer) {
                    score++
                }
                currentQuestionIndex++
            }
        )
    } else {
        FinalScore(
            score = score,
            totalQuestions = questions.size,
            onRestartQuiz = {
                currentQuestionIndex = 0
                score = 0
            },
            onBackToCategories = onBackToCategories // Возврат в меню
        )
    }
}

@Composable
fun QuestionCard(
    question: Question,
    onAnswerSelected: (String) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }

    val correctGreen = Color(0xFF4CAF50)
    val incorrectRed = Color(0xFFE57373)

    // Перемешиваем варианты ответов
    val shuffledOptions = question.options.shuffled()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                shuffledOptions.forEach { option ->
                    Button(
                        onClick = {
                            if (!isAnswerSubmitted) {
                                selectedAnswer = option
                                isAnswerSubmitted = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isAnswerSubmitted && selectedAnswer == option && option == question.correctAnswer -> correctGreen
                                isAnswerSubmitted && selectedAnswer == option -> incorrectRed
                                else -> MaterialTheme.colorScheme.primary
                            },
                            disabledContainerColor = when {
                                isAnswerSubmitted && selectedAnswer == option && option == question.correctAnswer -> correctGreen
                                isAnswerSubmitted && selectedAnswer == option -> incorrectRed
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        enabled = !isAnswerSubmitted
                    ) {
                        Text(text = option)
                    }
                }

                if (isAnswerSubmitted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Правильный ответ: ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            if (isAnswerSubmitted) {
                Button(
                    onClick = {
                        onAnswerSelected(selectedAnswer ?: "")
                        selectedAnswer = null
                        isAnswerSubmitted = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Следующий вопрос")
                }
            }
        }
    }
}

@Composable
fun FinalScore(
    score: Int,
    totalQuestions: Int,
    onRestartQuiz: () -> Unit,
    onBackToCategories: () -> Unit // Добавляем новый параметр
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Тест завершен!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Ваш результат: $score из $totalQuestions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 24.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRestartQuiz,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Пройти тест заново",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = onBackToCategories, // Обработчик для возврата в меню
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "Вернуться в главное меню",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)
