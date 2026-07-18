package com.example.ui

import com.example.R

data class QuizQuestion(
    val id: Int,
    val questionRes: Int,
    val optionsRes: List<Int>,
    val correctIndex: Int,
    val correctFeedbackRes: Int,
    val wrongFeedbackRes: Int
)

object QuizData {
    val levelResIds = listOf(
        R.string.quiz_level_0,
        R.string.quiz_level_1,
        R.string.quiz_level_2,
        R.string.quiz_level_3,
        R.string.quiz_level_4
    )

    val questionsByLevel = mapOf<Int, List<QuizQuestion>>(
        0 to (1..10).map { i ->
            val questionResId = getResId("quiz_q${i}_text")
            val options = (0..3).map { j -> getResId("quiz_q${i}_opt$j") }
            val correctResId = getResId("quiz_q${i}_correct")
            val wrongResId = getResId("quiz_q${i}_wrong")
            QuizQuestion(i, questionResId, options, 1, correctResId, wrongResId)
        },
        1 to (11..20).map { i ->
            val questionResId = getResId("quiz_q${i}_text")
            val options = (0..3).map { j -> getResId("quiz_q${i}_opt$j") }
            val correctResId = getResId("quiz_q${i}_correct")
            val wrongResId = getResId("quiz_q${i}_wrong")
            QuizQuestion(i, questionResId, options, 1, correctResId, wrongResId)
        },
        2 to (21..30).map { i ->
            val questionResId = getResId("quiz_q${i}_text")
            val options = (0..3).map { j -> getResId("quiz_q${i}_opt$j") }
            val correctResId = getResId("quiz_q${i}_correct")
            val wrongResId = getResId("quiz_q${i}_wrong")
            QuizQuestion(i, questionResId, options, 1, correctResId, wrongResId)
        },
        3 to (31..40).map { i ->
            val questionResId = getResId("quiz_q${i}_text")
            val options = (0..3).map { j -> getResId("quiz_q${i}_opt$j") }
            val correctResId = getResId("quiz_q${i}_correct")
            val wrongResId = getResId("quiz_q${i}_wrong")
            QuizQuestion(i, questionResId, options, 1, correctResId, wrongResId)
        },
        4 to (41..50).map { i ->
            val questionResId = getResId("quiz_q${i}_text")
            val options = (0..3).map { j -> getResId("quiz_q${i}_opt$j") }
            val correctResId = getResId("quiz_q${i}_correct")
            val wrongResId = getResId("quiz_q${i}_wrong")
            QuizQuestion(i, questionResId, options, 1, correctResId, wrongResId)
        }
    )

    private fun getResId(name: String): Int {
        return try {
            val field = R.string::class.java.getField(name)
            field.getInt(null)
        } catch (e: Exception) {
            R.string.app_name // Fallback
        }
    }
}
