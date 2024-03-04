package com.example.arithmeticgame

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    var player1Score = 0
    var player2Score = 0
    var jackpotAmount = 5
    var currentPlayer = "P1"
    var doublePoints = false
    var correctAnswer = 0
    var currentProblemType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val rollDieButton: Button = findViewById(R.id.rollDieButton)
        rollDieButton.setOnClickListener {
            rollDie()
        }
        updateUI()
        setupGuessButtonLogic()
    }

    private fun updateUI() {
        val player1TotalTextView: TextView = findViewById(R.id.player1Total)
        val player2TotalTextView: TextView = findViewById(R.id.player2Total)
        val jackpotTextView: TextView = findViewById(R.id.currentJackpot)
        val currentPlayerTextView: TextView = findViewById(R.id.currentPlayer)

        player1TotalTextView.text = "Player 1 total: $player1Score"
        player2TotalTextView.text = "Player 2 total: $player2Score"
        jackpotTextView.text = "Current Jackpot: $jackpotAmount"
        currentPlayerTextView.text = "Current Player: $currentPlayer"
    }

    fun rollDie() {
        val dieResult = (1..6).random() // Generate a random number between 1 and 6
        updateDieImage(dieResult) // A method to update the die image based on the roll
        handleDieRoll(dieResult) // Handle the logic based on the die result
    }

    fun updateDieImage(dieResult: Int) {
        val dieImage: ImageView = findViewById(R.id.dieImage)
        val resourceId = when(dieResult) {
            1 -> R.drawable.die_1
            2 -> R.drawable.die_2
            3 -> R.drawable.die_3
            4 -> R.drawable.die_4
            5 -> R.drawable.die_5
            else -> R.drawable.die_6
        }
        dieImage.setImageResource(resourceId)
    }

    fun handleDieRoll(dieResult: Int) {
        val problemTextView: TextView = findViewById(R.id.problemTextView)
        when (dieResult) {
            1 -> generateProblem("addition")
            2 -> generateProblem("subtraction")
            3 -> generateProblem("multiplication")
            4 -> {
                doublePoints = true
                problemTextView.text = "Roll again for double points!"
            }
            5 -> {
                currentProblemType = "lose a turn"
                problemTextView.text = "Lose a turn!"
                switchPlayer()
            }
            6 -> {
                currentProblemType = "jackpot"
                problemTextView.text = "Try for jackpot!"
                generateProblem(listOf("addition", "subtraction", "multiplication").random())
            }
        }
    }

    fun generateProblem(operation: String) {
        val number1: Int
        val number2: Int
        val problemTextView: TextView = findViewById(R.id.problemTextView)

        when (operation) {
            "addition" -> {
                number1 = (0..99).random()
                number2 = (0..99).random()
                correctAnswer = number1 + number2
                problemTextView.text = "$number1 + $number2 = ?"
                currentProblemType = "addition"
            }
            "subtraction" -> {
                number1 = (0..99).random()
                number2 = (0..99).random()
                correctAnswer = number1 - number2
                problemTextView.text = "$number1 - $number2 = ?"
                currentProblemType = "subtraction"
            }
            "multiplication" -> {
                number1 = (0..20).random()
                number2 = (0..20).random()
                correctAnswer = number1 * number2
                problemTextView.text = "$number1 * $number2 = ?"
                currentProblemType = "multiplication"
            }
        }
    }

    private fun setupGuessButtonLogic() {
        val guessButton: Button = findViewById(R.id.guessButton)
        guessButton.setOnClickListener {
            val answerInput: EditText = findViewById(R.id.answerInput)
            try {
                val userAnswer = answerInput.text.toString().toInt()
                var pointsAwarded = 0
                val problemTextView: TextView = findViewById(R.id.problemTextView)

                if (userAnswer == correctAnswer) {
                    when (currentProblemType) {
                        "addition" -> pointsAwarded = 1
                        "subtraction" -> pointsAwarded = 2
                        "multiplication" -> pointsAwarded = 3
                        "jackpot" -> pointsAwarded = jackpotAmount
                    }

                    if (doublePoints && currentProblemType != "jackpot") {
                        pointsAwarded *= 2 // Double points for a correct answer under double points condition
                        doublePoints = false
                    }

                    updateScore(pointsAwarded)
                    if (currentProblemType == "jackpot") {
                        problemTextView.text = "Jackpot won! Points earned: $pointsAwarded"
                        jackpotAmount = 5 // Reset the jackpot
                    } else {
                        Toast.makeText(this, "Correct! Points earned: $pointsAwarded", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Incorrect. The correct answer was $correctAnswer", Toast.LENGTH_SHORT).show()
                    if (currentProblemType != "jackpot") {
                        jackpotAmount += pointsAwarded // Add to the jackpot the points that would have been earned
                    }
                }

                if (currentProblemType != "double") {
                    switchPlayer() // Switch player unless it's a double points condition
                }
                updateUI()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
            answerInput.text.clear()
        }
    }

    fun updateScore(points: Int) {
        if (currentPlayer == "P1") {
            player1Score += points
        } else {
            player2Score += points
        }
        checkWinCondition()
        updateUI()
    }

    fun switchPlayer() {
        currentPlayer = if (currentPlayer == "P1") "P2" else "P1"
        updateUI() // Make sure this updates the currentPlayer TextView among others
    }

    fun checkWinCondition() {
        if (player1Score >= 20) {
            Toast.makeText(this, "Player 1 Wins!", Toast.LENGTH_LONG).show()
            resetGame()
        } else if (player2Score >= 20) {
            Toast.makeText(this, "Player 2 Wins!", Toast.LENGTH_LONG).show()
            resetGame()
        }
    }

    fun resetGame() {
        player1Score = 0
        player2Score = 0
        jackpotAmount = 5 // Reset to the starting jackpot amount
        currentPlayer = "P1"
        doublePoints = false
        updateUI() // Make sure to update the UI with the reset values
    }

}