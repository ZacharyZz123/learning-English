package com.xuexi.learningenglish.data.model

enum class PracticeQuestionType {
    COMPLETE_MISSING_LETTERS,
    SPELL_FROM_MEANING
}

data class PracticeQuestion(
    val word: Word,
    val type: PracticeQuestionType,
    val prompt: String,
    val clue: String,
    val expectedAnswer: String
) {
    fun isCorrect(answer: String): Boolean {
        return answer.trim().equals(expectedAnswer.trim(), ignoreCase = true)
    }
}
