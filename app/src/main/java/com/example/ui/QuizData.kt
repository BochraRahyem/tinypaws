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
        0 to listOf(
            QuizQuestion(
                id = 1,
                questionRes = R.string.quiz_q1,
                optionsRes = listOf(R.string.quiz_q1_opt0, R.string.quiz_q1_opt1, R.string.quiz_q1_opt2, R.string.quiz_q1_opt3),
                correctIndex = 2,
                correctFeedbackRes = R.string.quiz_q1_cf,
                wrongFeedbackRes = R.string.quiz_q1_wf
            ),
            QuizQuestion(
                id = 2,
                questionRes = R.string.quiz_q2,
                optionsRes = listOf(R.string.quiz_q2_opt0, R.string.quiz_q2_opt1, R.string.quiz_q2_opt2, R.string.quiz_q2_opt3),
                correctIndex = 2,
                correctFeedbackRes = R.string.quiz_q2_cf,
                wrongFeedbackRes = R.string.quiz_q2_wf
            ),
            QuizQuestion(
                id = 3,
                questionRes = R.string.quiz_q3,
                optionsRes = listOf(R.string.quiz_q3_opt0, R.string.quiz_q3_opt1, R.string.quiz_q3_opt2, R.string.quiz_q3_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q3_cf,
                wrongFeedbackRes = R.string.quiz_q3_wf
            ),
            QuizQuestion(
                id = 4,
                questionRes = R.string.quiz_q4,
                optionsRes = listOf(R.string.quiz_q4_opt0, R.string.quiz_q4_opt1, R.string.quiz_q4_opt2, R.string.quiz_q4_opt3),
                correctIndex = 2,
                correctFeedbackRes = R.string.quiz_q4_cf,
                wrongFeedbackRes = R.string.quiz_q4_wf
            ),
            QuizQuestion(
                id = 5,
                questionRes = R.string.quiz_q5,
                optionsRes = listOf(R.string.quiz_q5_opt0, R.string.quiz_q5_opt1, R.string.quiz_q5_opt2, R.string.quiz_q5_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q5_cf,
                wrongFeedbackRes = R.string.quiz_q5_wf
            ),
            QuizQuestion(
                id = 6,
                questionRes = R.string.quiz_q6,
                optionsRes = listOf(R.string.quiz_q6_opt0, R.string.quiz_q6_opt1, R.string.quiz_q6_opt2, R.string.quiz_q6_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q6_cf,
                wrongFeedbackRes = R.string.quiz_q6_wf
            ),
            QuizQuestion(
                id = 7,
                questionRes = R.string.quiz_q7,
                optionsRes = listOf(R.string.quiz_q7_opt0, R.string.quiz_q7_opt1, R.string.quiz_q7_opt2, R.string.quiz_q7_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q7_cf,
                wrongFeedbackRes = R.string.quiz_q7_wf
            ),
            QuizQuestion(
                id = 8,
                questionRes = R.string.quiz_q8,
                optionsRes = listOf(R.string.quiz_q8_opt0, R.string.quiz_q8_opt1, R.string.quiz_q8_opt2, R.string.quiz_q8_opt3),
                correctIndex = 2,
                correctFeedbackRes = R.string.quiz_q8_cf,
                wrongFeedbackRes = R.string.quiz_q8_wf
            ),
            QuizQuestion(
                id = 9,
                questionRes = R.string.quiz_q9,
                optionsRes = listOf(R.string.quiz_q9_opt0, R.string.quiz_q9_opt1, R.string.quiz_q9_opt2, R.string.quiz_q9_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q9_cf,
                wrongFeedbackRes = R.string.quiz_q9_wf
            ),
            QuizQuestion(
                id = 10,
                questionRes = R.string.quiz_q10,
                optionsRes = listOf(R.string.quiz_q10_opt0, R.string.quiz_q10_opt1, R.string.quiz_q10_opt2, R.string.quiz_q10_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q10_cf,
                wrongFeedbackRes = R.string.quiz_q10_wf
            )
        ),
        1 to listOf(
            QuizQuestion(
                id = 11,
                questionRes = R.string.quiz_q11,
                optionsRes = listOf(R.string.quiz_q11_opt0, R.string.quiz_q11_opt1, R.string.quiz_q11_opt2, R.string.quiz_q11_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q11_cf,
                wrongFeedbackRes = R.string.quiz_q11_wf
            ),
            QuizQuestion(
                id = 12,
                questionRes = R.string.quiz_q12,
                optionsRes = listOf(R.string.quiz_q12_opt0, R.string.quiz_q12_opt1, R.string.quiz_q12_opt2, R.string.quiz_q12_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q12_cf,
                wrongFeedbackRes = R.string.quiz_q12_wf
            ),
            QuizQuestion(
                id = 13,
                questionRes = R.string.quiz_q13,
                optionsRes = listOf(R.string.quiz_q13_opt0, R.string.quiz_q13_opt1, R.string.quiz_q13_opt2, R.string.quiz_q13_opt3),
                correctIndex = 2,
                correctFeedbackRes = R.string.quiz_q13_cf,
                wrongFeedbackRes = R.string.quiz_q13_wf
            ),
            QuizQuestion(
                id = 14,
                questionRes = R.string.quiz_q14,
                optionsRes = listOf(R.string.quiz_q14_opt0, R.string.quiz_q14_opt1, R.string.quiz_q14_opt2, R.string.quiz_q14_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q14_cf,
                wrongFeedbackRes = R.string.quiz_q14_wf
            ),
            QuizQuestion(
                id = 15,
                questionRes = R.string.quiz_q15,
                optionsRes = listOf(R.string.quiz_q15_opt0, R.string.quiz_q15_opt1, R.string.quiz_q15_opt2, R.string.quiz_q15_opt3),
                correctIndex = 2,
                correctFeedbackRes = R.string.quiz_q15_cf,
                wrongFeedbackRes = R.string.quiz_q15_wf
            ),
            QuizQuestion(
                id = 16,
                questionRes = R.string.quiz_q16,
                optionsRes = listOf(R.string.quiz_q16_opt0, R.string.quiz_q16_opt1, R.string.quiz_q16_opt2, R.string.quiz_q16_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q16_cf,
                wrongFeedbackRes = R.string.quiz_q16_wf
            ),
            QuizQuestion(
                id = 17,
                questionRes = R.string.quiz_q17,
                optionsRes = listOf(R.string.quiz_q17_opt0, R.string.quiz_q17_opt1, R.string.quiz_q17_opt2, R.string.quiz_q17_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q17_cf,
                wrongFeedbackRes = R.string.quiz_q17_wf
            ),
            QuizQuestion(
                id = 18,
                questionRes = R.string.quiz_q18,
                optionsRes = listOf(R.string.quiz_q18_opt0, R.string.quiz_q18_opt1, R.string.quiz_q18_opt2, R.string.quiz_q18_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q18_cf,
                wrongFeedbackRes = R.string.quiz_q18_wf
            ),
            QuizQuestion(
                id = 19,
                questionRes = R.string.quiz_q19,
                optionsRes = listOf(R.string.quiz_q19_opt0, R.string.quiz_q19_opt1, R.string.quiz_q19_opt2, R.string.quiz_q19_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q19_cf,
                wrongFeedbackRes = R.string.quiz_q19_wf
            ),
            QuizQuestion(
                id = 20,
                questionRes = R.string.quiz_q20,
                optionsRes = listOf(R.string.quiz_q20_opt0, R.string.quiz_q20_opt1, R.string.quiz_q20_opt2, R.string.quiz_q20_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q20_cf,
                wrongFeedbackRes = R.string.quiz_q20_wf
            )
        ),
        2 to listOf(
            QuizQuestion(
                id = 21,
                questionRes = R.string.quiz_q21,
                optionsRes = listOf(R.string.quiz_q21_opt0, R.string.quiz_q21_opt1, R.string.quiz_q21_opt2, R.string.quiz_q21_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q21_cf,
                wrongFeedbackRes = R.string.quiz_q21_wf
            ),
            QuizQuestion(
                id = 22,
                questionRes = R.string.quiz_q22,
                optionsRes = listOf(R.string.quiz_q22_opt0, R.string.quiz_q22_opt1, R.string.quiz_q22_opt2, R.string.quiz_q22_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q22_cf,
                wrongFeedbackRes = R.string.quiz_q22_wf
            ),
            QuizQuestion(
                id = 23,
                questionRes = R.string.quiz_q23,
                optionsRes = listOf(R.string.quiz_q23_opt0, R.string.quiz_q23_opt1, R.string.quiz_q23_opt2, R.string.quiz_q23_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q23_cf,
                wrongFeedbackRes = R.string.quiz_q23_wf
            ),
            QuizQuestion(
                id = 24,
                questionRes = R.string.quiz_q24,
                optionsRes = listOf(R.string.quiz_q24_opt0, R.string.quiz_q24_opt1, R.string.quiz_q24_opt2, R.string.quiz_q24_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q24_cf,
                wrongFeedbackRes = R.string.quiz_q24_wf
            ),
            QuizQuestion(
                id = 25,
                questionRes = R.string.quiz_q25,
                optionsRes = listOf(R.string.quiz_q25_opt0, R.string.quiz_q25_opt1, R.string.quiz_q25_opt2, R.string.quiz_q25_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q25_cf,
                wrongFeedbackRes = R.string.quiz_q25_wf
            ),
            QuizQuestion(
                id = 26,
                questionRes = R.string.quiz_q26,
                optionsRes = listOf(R.string.quiz_q26_opt0, R.string.quiz_q26_opt1, R.string.quiz_q26_opt2, R.string.quiz_q26_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q26_cf,
                wrongFeedbackRes = R.string.quiz_q26_wf
            ),
            QuizQuestion(
                id = 27,
                questionRes = R.string.quiz_q27,
                optionsRes = listOf(R.string.quiz_q27_opt0, R.string.quiz_q27_opt1, R.string.quiz_q27_opt2, R.string.quiz_q27_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q27_cf,
                wrongFeedbackRes = R.string.quiz_q27_wf
            ),
            QuizQuestion(
                id = 28,
                questionRes = R.string.quiz_q28,
                optionsRes = listOf(R.string.quiz_q28_opt0, R.string.quiz_q28_opt1, R.string.quiz_q28_opt2, R.string.quiz_q28_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q28_cf,
                wrongFeedbackRes = R.string.quiz_q28_wf
            ),
            QuizQuestion(
                id = 29,
                questionRes = R.string.quiz_q29,
                optionsRes = listOf(R.string.quiz_q29_opt0, R.string.quiz_q29_opt1, R.string.quiz_q29_opt2, R.string.quiz_q29_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q29_cf,
                wrongFeedbackRes = R.string.quiz_q29_wf
            ),
            QuizQuestion(
                id = 30,
                questionRes = R.string.quiz_q30,
                optionsRes = listOf(R.string.quiz_q30_opt0, R.string.quiz_q30_opt1, R.string.quiz_q30_opt2, R.string.quiz_q30_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q30_cf,
                wrongFeedbackRes = R.string.quiz_q30_wf
            )
        ),
        3 to listOf(
            QuizQuestion(
                id = 31,
                questionRes = R.string.quiz_q31,
                optionsRes = listOf(R.string.quiz_q31_opt0, R.string.quiz_q31_opt1, R.string.quiz_q31_opt2, R.string.quiz_q31_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q31_cf,
                wrongFeedbackRes = R.string.quiz_q31_wf
            ),
            QuizQuestion(
                id = 32,
                questionRes = R.string.quiz_q32,
                optionsRes = listOf(R.string.quiz_q32_opt0, R.string.quiz_q32_opt1, R.string.quiz_q32_opt2, R.string.quiz_q32_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q32_cf,
                wrongFeedbackRes = R.string.quiz_q32_wf
            ),
            QuizQuestion(
                id = 33,
                questionRes = R.string.quiz_q33,
                optionsRes = listOf(R.string.quiz_q33_opt0, R.string.quiz_q33_opt1, R.string.quiz_q33_opt2, R.string.quiz_q33_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q33_cf,
                wrongFeedbackRes = R.string.quiz_q33_wf
            ),
            QuizQuestion(
                id = 34,
                questionRes = R.string.quiz_q34,
                optionsRes = listOf(R.string.quiz_q34_opt0, R.string.quiz_q34_opt1, R.string.quiz_q34_opt2, R.string.quiz_q34_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q34_cf,
                wrongFeedbackRes = R.string.quiz_q34_wf
            ),
            QuizQuestion(
                id = 35,
                questionRes = R.string.quiz_q35,
                optionsRes = listOf(R.string.quiz_q35_opt0, R.string.quiz_q35_opt1, R.string.quiz_q35_opt2, R.string.quiz_q35_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q35_cf,
                wrongFeedbackRes = R.string.quiz_q35_wf
            ),
            QuizQuestion(
                id = 36,
                questionRes = R.string.quiz_q36,
                optionsRes = listOf(R.string.quiz_q36_opt0, R.string.quiz_q36_opt1, R.string.quiz_q36_opt2, R.string.quiz_q36_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q36_cf,
                wrongFeedbackRes = R.string.quiz_q36_wf
            ),
            QuizQuestion(
                id = 37,
                questionRes = R.string.quiz_q37,
                optionsRes = listOf(R.string.quiz_q37_opt0, R.string.quiz_q37_opt1, R.string.quiz_q37_opt2, R.string.quiz_q37_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q37_cf,
                wrongFeedbackRes = R.string.quiz_q37_wf
            ),
            QuizQuestion(
                id = 38,
                questionRes = R.string.quiz_q38,
                optionsRes = listOf(R.string.quiz_q38_opt0, R.string.quiz_q38_opt1, R.string.quiz_q38_opt2, R.string.quiz_q38_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q38_cf,
                wrongFeedbackRes = R.string.quiz_q38_wf
            ),
            QuizQuestion(
                id = 39,
                questionRes = R.string.quiz_q39,
                optionsRes = listOf(R.string.quiz_q39_opt0, R.string.quiz_q39_opt1, R.string.quiz_q39_opt2, R.string.quiz_q39_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q39_cf,
                wrongFeedbackRes = R.string.quiz_q39_wf
            ),
            QuizQuestion(
                id = 40,
                questionRes = R.string.quiz_q40,
                optionsRes = listOf(R.string.quiz_q40_opt0, R.string.quiz_q40_opt1, R.string.quiz_q40_opt2, R.string.quiz_q40_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q40_cf,
                wrongFeedbackRes = R.string.quiz_q40_wf
            )
        ),
        4 to listOf(
            QuizQuestion(
                id = 41,
                questionRes = R.string.quiz_q41,
                optionsRes = listOf(R.string.quiz_q41_opt0, R.string.quiz_q41_opt1, R.string.quiz_q41_opt2, R.string.quiz_q41_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q41_cf,
                wrongFeedbackRes = R.string.quiz_q41_wf
            ),
            QuizQuestion(
                id = 42,
                questionRes = R.string.quiz_q42,
                optionsRes = listOf(R.string.quiz_q42_opt0, R.string.quiz_q42_opt1, R.string.quiz_q42_opt2, R.string.quiz_q42_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q42_cf,
                wrongFeedbackRes = R.string.quiz_q42_wf
            ),
            QuizQuestion(
                id = 43,
                questionRes = R.string.quiz_q43,
                optionsRes = listOf(R.string.quiz_q43_opt0, R.string.quiz_q43_opt1, R.string.quiz_q43_opt2, R.string.quiz_q43_opt3),
                correctIndex = 0,
                correctFeedbackRes = R.string.quiz_q43_cf,
                wrongFeedbackRes = R.string.quiz_q43_wf
            ),
            QuizQuestion(
                id = 44,
                questionRes = R.string.quiz_q44,
                optionsRes = listOf(R.string.quiz_q44_opt0, R.string.quiz_q44_opt1, R.string.quiz_q44_opt2, R.string.quiz_q44_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q44_cf,
                wrongFeedbackRes = R.string.quiz_q44_wf
            ),
            QuizQuestion(
                id = 45,
                questionRes = R.string.quiz_q45,
                optionsRes = listOf(R.string.quiz_q45_opt0, R.string.quiz_q45_opt1, R.string.quiz_q45_opt2, R.string.quiz_q45_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q45_cf,
                wrongFeedbackRes = R.string.quiz_q45_wf
            ),
            QuizQuestion(
                id = 46,
                questionRes = R.string.quiz_q46,
                optionsRes = listOf(R.string.quiz_q46_opt0, R.string.quiz_q46_opt1, R.string.quiz_q46_opt2, R.string.quiz_q46_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q46_cf,
                wrongFeedbackRes = R.string.quiz_q46_wf
            ),
            QuizQuestion(
                id = 47,
                questionRes = R.string.quiz_q47,
                optionsRes = listOf(R.string.quiz_q47_opt0, R.string.quiz_q47_opt1, R.string.quiz_q47_opt2, R.string.quiz_q47_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q47_cf,
                wrongFeedbackRes = R.string.quiz_q47_wf
            ),
            QuizQuestion(
                id = 48,
                questionRes = R.string.quiz_q48,
                optionsRes = listOf(R.string.quiz_q48_opt0, R.string.quiz_q48_opt1, R.string.quiz_q48_opt2, R.string.quiz_q48_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q48_cf,
                wrongFeedbackRes = R.string.quiz_q48_wf
            ),
            QuizQuestion(
                id = 49,
                questionRes = R.string.quiz_q49,
                optionsRes = listOf(R.string.quiz_q49_opt0, R.string.quiz_q49_opt1, R.string.quiz_q49_opt2, R.string.quiz_q49_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q49_cf,
                wrongFeedbackRes = R.string.quiz_q49_wf
            ),
            QuizQuestion(
                id = 50,
                questionRes = R.string.quiz_q50,
                optionsRes = listOf(R.string.quiz_q50_opt0, R.string.quiz_q50_opt1, R.string.quiz_q50_opt2, R.string.quiz_q50_opt3),
                correctIndex = 1,
                correctFeedbackRes = R.string.quiz_q50_cf,
                wrongFeedbackRes = R.string.quiz_q50_wf
            )
        )
    )
}
