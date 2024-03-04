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
    var attemptingJackpot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val guessButton: Button = findViewById(R.id.guessButton)
        guessButton.isEnabled = false

        val rollDieButton: Button = findViewById(R.id.rollDieButton)
        rollDieButton.isEnabled = true

        rollDieButton.setOnClickListener {
            rollDie()
        }
        setupGuessButtonLogic()
        updateUI()
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
        val dieResult = (1..6).random()
        updateDieImage(dieResult)

        val guessButton: Button = findViewById(R.id.guessButton)
        val rollDieButton: Button = findViewById(R.id.rollDieButton)

        // Enabling guess button for problems requiring a guess
        if (dieResult in 1..3 || dieResult == 6) {
            guessButton.isEnabled = true
            rollDieButton.isEnabled = false // Ensure user cannot roll again before guessing
        } else if (dieResult == 5) { // Lose a turn, directly switch players
            switchPlayer()
        } else if (dieResult == 4) { // Double points, but still needs to roll again
            guessButton.isEnabled = false // No guessing required for double points directly
        }

        handleDieRoll(dieResult)
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
                attemptingJackpot = true // Indicate that a jackpot attempt is in progress
                problemTextView.text = "Try for jackpot!"
                generateProblem(listOf("addition", "subtraction", "multiplication").random())
            }
        }
    }

    fun generateProblem(operation: String) {
        var number1: Int
        var number2: Int
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

                // Ensure number1 is always greater than or equal to number2
                if (number1 < number2) {
                    // Swap numbers if number2 is greater than number1
                    val temp = number1
                    number1 = number2
                    number2 = temp
                }

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
                    }

                    if (attemptingJackpot) {
                        pointsAwarded = jackpotAmount
                        Toast.makeText(this, "Jackpot won! Points earned: $pointsAwarded", Toast.LENGTH_LONG).show()
                        jackpotAmount = 5 // Reset the jackpot
                        attemptingJackpot = false // Reset the flag indicating a jackpot attempt
                    }else {
                        if (doublePoints) {
                            pointsAwarded *= 2 // Double points for a correct answer under double points condition
                            doublePoints = false
                        }
                        Toast.makeText(this, "Correct! Points earned: $pointsAwarded", Toast.LENGTH_SHORT).show()
                    }
                    updateScore(pointsAwarded)
                } else {
                    Toast.makeText(this, "Incorrect. The correct answer was $correctAnswer", Toast.LENGTH_SHORT).show()
                    val missedPoints = when (currentProblemType) {
                        "addition" -> 1
                        "subtraction" -> 2
                        "multiplication" -> 3
                        else -> 0 // No points missed for incorrect jackpot attempt
                    }
                    if (attemptingJackpot) {
                        attemptingJackpot = false
                        jackpotAmount += missedPoints // Correctly increment the jackpot based on the missed question
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

        val rollDieButton: Button = findViewById(R.id.rollDieButton)
        rollDieButton.isEnabled = true // Re-enable for the next player's turn

        val guessButton: Button = findViewById(R.id.guessButton)
        guessButton.isEnabled = false // Disable guessing until the die is rolled again

        updateUI()
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

