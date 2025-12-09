package com.example.bloom.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.BudgetController
import com.example.bloom.controllers.TransactionController
import com.example.bloom.controllers.BloomAIController
import kotlinx.coroutines.launch
import kotlin.math.min

class HealthScoreViewModel : ViewModel() {

    private val budgetController = BudgetController()
    private val transactionController = TransactionController()
    private val aiController = BloomAIController()

    // =====================================================
    // STATE VARIABLES
    // =====================================================

    // Overall score (0-100)
    var healthScore by mutableStateOf(0)
        private set

    // AI-generated health score
    var aiHealthScore by mutableStateOf<Int?>(null)
        private set

    // Score breakdown
    var budgetAdherenceScore by mutableStateOf(0)
        private set

    var savingsRateScore by mutableStateOf(0)
        private set

    var spendingConsistencyScore by mutableStateOf(0)
        private set

    var emergencyFundScore by mutableStateOf(0)
        private set

    // Financial data
    var monthlyBudget by mutableStateOf(0.0)
        private set

    var totalSpent by mutableStateOf(0.0)
        private set

    var savingsGoal by mutableStateOf(0.0)
        private set

    var currentSavings by mutableStateOf(0.0)
        private set

    var monthlyIncome by mutableStateOf(0.0)
        private set

    var averageMonthlyExpense by mutableStateOf(0.0)
        private set

    // Recommendations
    var recommendations by mutableStateOf<List<String>>(emptyList())
        private set

    // AI-powered recommendations
    var aiRecommendations by mutableStateOf<String?>(null)
        private set

    var isLoadingAI by mutableStateOf(false)
        private set

    // Loading and error states
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Computed properties
    val scoreRating: String
        get() = when (healthScore) {
            in 90..100 -> "Excellent"
            in 75..89 -> "Good"
            in 50..74 -> "Fair"
            in 25..49 -> "Poor"
            else -> "Very Poor"
        }

    val scoreColor: Long
        get() = when (healthScore) {
            in 90..100 -> 0xFF10B981 // Green
            in 75..89 -> 0xFF3B82F6 // Blue
            in 50..74 -> 0xFFFBBF24 // Yellow
            in 25..49 -> 0xFFF97316 // Orange
            else -> 0xFFEF4444 // Red
        }



    // =====================================================
    // HEALTH SCORE CALCULATION
    // =====================================================

    /**
     * Main function to calculate overall health score
     */
    fun calculateHealthScore() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Load all necessary data
                loadFinancialData()

                // Calculate individual scores
                calculateBudgetAdherence()
                calculateSavingsRate()
                calculateSpendingConsistency()
                calculateEmergencyFund()

                // Calculate total score
                healthScore = budgetAdherenceScore + savingsRateScore +
                        spendingConsistencyScore + emergencyFundScore

                // Generate recommendations
                generateRecommendations()

            } catch (e: Exception) {
                errorMessage = "Failed to calculate health score: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Load all financial data needed for score calculation
     */
    private suspend fun loadFinancialData() {
        // Load budget summary
        budgetController.getCurrentMonthBudget().fold(
            onSuccess = { budget ->
                if (budget != null) {
                    monthlyBudget = budget.monthly_budget
                    savingsGoal = budget.savings_goal
                    currentSavings = budget.current_savings
                }
            },
            onFailure = { e ->
                throw e
            }
        )

        // Load total spent this month
        budgetController.getTotalSpentThisMonth().fold(
            onSuccess = { spent ->
                totalSpent = spent
            },
            onFailure = { e ->
                throw e
            }
        )

        // Load monthly income (from income transactions)
        transactionController.getMonthlyIncome().fold(
            onSuccess = { income ->
                monthlyIncome = income
            },
            onFailure = {
                monthlyIncome = 0.0
            }
        )

        // Load average monthly expenses
        transactionController.getAverageMonthlyExpenses().fold(
            onSuccess = { avgExpense ->
                averageMonthlyExpense = avgExpense
            },
            onFailure = {
                averageMonthlyExpense = totalSpent
            }
        )
    }

    /**
     * Calculate budget adherence score (40 points max)
     * - Under budget: Full points
     * - Slightly over budget (< 10%): Partial points
     * - Way over budget: Few or no points
     */
    private fun calculateBudgetAdherence() {
        if (monthlyBudget <= 0) {
            budgetAdherenceScore = 0
            return
        }

        val spentPercentage = (totalSpent / monthlyBudget) * 100

        budgetAdherenceScore = when {
            spentPercentage <= 80 -> 40 // Well under budget
            spentPercentage <= 95 -> 35 // Good control
            spentPercentage <= 100 -> 30 // On budget
            spentPercentage <= 110 -> 20 // Slightly over
            spentPercentage <= 125 -> 10 // Over budget
            else -> 0 // Way over budget
        }
    }

    /**
     * Calculate savings rate score (30 points max)
     * - Savings as percentage of income
     * - Progress toward savings goal
     */
    private fun calculateSavingsRate() {
        var score = 0

        // Check if meeting savings goal (15 points)
        if (savingsGoal > 0) {
            val savingsProgress = (currentSavings / savingsGoal) * 100
            score += when {
                savingsProgress >= 100 -> 15
                savingsProgress >= 75 -> 12
                savingsProgress >= 50 -> 9
                savingsProgress >= 25 -> 6
                else -> 3
            }
        }

        // Check savings rate as percentage of income (15 points)
        if (monthlyIncome > 0) {
            val monthlySavings = monthlyIncome - totalSpent
            val savingsRate = (monthlySavings / monthlyIncome) * 100

            score += when {
                savingsRate >= 20 -> 15 // Excellent savings rate
                savingsRate >= 15 -> 12
                savingsRate >= 10 -> 9
                savingsRate >= 5 -> 6
                savingsRate > 0 -> 3
                else -> 0 // Not saving
            }
        }

        savingsRateScore = score
    }

    /**
     * Calculate spending consistency score (20 points max)
     * - Reward consistent, predictable spending
     * - Penalize erratic spending patterns
     */
    private fun calculateSpendingConsistency() {
        if (monthlyBudget <= 0 || averageMonthlyExpense <= 0) {
            spendingConsistencyScore = 10 // Neutral score if no data
            return
        }

        // Compare current spending to average
        val variance = kotlin.math.abs(totalSpent - averageMonthlyExpense)
        val variancePercentage = (variance / averageMonthlyExpense) * 100

        spendingConsistencyScore = when {
            variancePercentage <= 10 -> 20 // Very consistent
            variancePercentage <= 20 -> 15 // Fairly consistent
            variancePercentage <= 30 -> 10 // Somewhat consistent
            variancePercentage <= 50 -> 5  // Inconsistent
            else -> 0 // Very inconsistent
        }
    }

    /**
     * Calculate emergency fund score (10 points max)
     * - Emergency fund should be 3-6 months of expenses
     */
    private fun calculateEmergencyFund() {
        if (averageMonthlyExpense <= 0) {
            emergencyFundScore = 0
            return
        }

        val monthsOfExpenses = currentSavings / averageMonthlyExpense

        emergencyFundScore = when {
            monthsOfExpenses >= 6 -> 10 // Fully funded
            monthsOfExpenses >= 3 -> 7  // Adequately funded
            monthsOfExpenses >= 1 -> 5  // Some buffer
            monthsOfExpenses >= 0.5 -> 3 // Minimal buffer
            else -> 0 // No emergency fund
        }
    }

    /**
     * Generate personalized recommendations based on scores
     */
    private fun generateRecommendations() {
        val recs = mutableListOf<String>()

        // Budget adherence recommendations
        if (budgetAdherenceScore < 30) {
            if (totalSpent > monthlyBudget) {
                recs.add("You're over budget this month. Review your spending and identify areas to cut back.")
            } else if (monthlyBudget == 0.0) {
                recs.add("Set a monthly budget to better track your spending and financial goals.")
            }
        }

        // Savings rate recommendations
        if (savingsRateScore < 20) {
            if (savingsGoal > 0 && currentSavings < savingsGoal) {
                val remaining = savingsGoal - currentSavings
                recs.add("You're $${String.format("%.2f", remaining)} away from your savings goal. Consider setting aside more each month.")
            }

            if (monthlyIncome > 0) {
                val currentSavingsRate = ((monthlyIncome - totalSpent) / monthlyIncome) * 100
                if (currentSavingsRate < 10) {
                    recs.add("Try to save at least 10-20% of your income each month for better financial health.")
                }
            }
        }

        // Spending consistency recommendations
        if (spendingConsistencyScore < 15) {
            recs.add("Your spending varies significantly month-to-month. Create a consistent budget to stabilize finances.")
        }

        // Emergency fund recommendations
        if (emergencyFundScore < 7) {
            val targetEmergencyFund = averageMonthlyExpense * 3
            val needed = targetEmergencyFund - currentSavings

            if (needed > 0) {
                recs.add("Build an emergency fund of $${String.format("%.2f", targetEmergencyFund)} (3 months expenses). You need $${String.format("%.2f", needed)} more.")
            }
        }

        // Positive reinforcement
        if (healthScore >= 90) {
            recs.add("Excellent work! You're managing your finances very well. Keep it up!")
        } else if (healthScore >= 75) {
            recs.add("Great job! You're on the right track. A few improvements will get you to excellent.")
        }

        // If no specific recommendations, add general advice
        if (recs.isEmpty()) {
            recs.add("Track your expenses regularly to maintain good financial health.")
            recs.add("Review your budget monthly and adjust as needed.")
        }

        recommendations = recs
    }

    /**
     * Refresh health score calculation
     */
    fun refresh() {
        calculateHealthScore()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Generate AI-powered health score and recommendations based on raw financial data
     */
    fun generateAIHealthScore() {
        viewModelScope.launch {
            isLoadingAI = true
            errorMessage = null

            try {
                // Ensure we have the latest summary data
                loadFinancialData()

                // Fetch recent transactions for context
                val transactionsResult = transactionController.fetchTransactions(page = 0, pageSize = 20)
                val transactions = transactionsResult.getOrNull() ?: emptyList()

                // Build comprehensive raw data summary for AI
                val data = buildRawFinancialDataSummary(transactions)
                Log.d("HealthScoreViewModel", "Sending raw data to AI: $data")

                // Call AI endpoint
                aiController.generateHealthScore(data).fold(
                    onSuccess = { response ->
                        Log.d("HealthScoreViewModel", "AI Response received - Score: ${response.score}")

                        // Update AI health score
                        aiHealthScore = response.score

                        // Update main health score with AI's calculation
                        healthScore = response.score

                        // Update breakdown scores from AI response
                        budgetAdherenceScore = response.budgetAdherenceScore
                        savingsRateScore = response.savingsRateScore
                        spendingConsistencyScore = response.spendingConsistencyScore
                        emergencyFundScore = response.emergencyFundScore

                        // Update recommendations
                        if (response.recommendations.isNotEmpty()) {
                            // Split recommendations string into a list if it's bulleted, or just wrap it
                            // For now, we'll store the raw text for the AI card, and parse for the list card if needed
                            aiRecommendations = response.recommendations
                            
                            // Optionally parse the AI recommendations into the main list
                            recommendations = parseRecommendationsToList(response.recommendations)
                            
                            Log.d("HealthScoreViewModel", "AI health score and breakdown updated successfully")
                        } else {
                            errorMessage = "AI returned empty recommendations"
                            Log.e("HealthScoreViewModel", "AI returned empty recommendations")
                        }
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to generate AI health score: ${e.message}"
                        Log.e("HealthScoreViewModel", "AI call failed", e)
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to generate AI health score: ${e.message}"
                Log.e("HealthScoreViewModel", "Exception in generateAIHealthScore", e)
            } finally {
                isLoadingAI = false
            }
        }
    }

    /**
     * Build raw financial data summary for AI analysis
     */
    private fun buildRawFinancialDataSummary(transactions: List<com.example.bloom.datamodels.TransactionWithCategory>): String {
        return buildString {
            appendLine("Raw Financial Data for Health Score Calculation:")
            appendLine("Budget & Goals:")
            appendLine("- Monthly Budget Limit: $${"%.2f".format(monthlyBudget)}")
            appendLine("- Savings Goal: $${"%.2f".format(savingsGoal)}")
            appendLine("- Current Savings: $${"%.2f".format(currentSavings)}")
            appendLine()
            appendLine("Income & Spending:")
            appendLine("- Monthly Income: $${"%.2f".format(monthlyIncome)}")
            appendLine("- Total Spent This Month: $${"%.2f".format(totalSpent)}")
            appendLine("- Average Monthly Expense (3-month avg): $${"%.2f".format(averageMonthlyExpense)}")
            appendLine()
            appendLine("Recent Transactions (Last 20):")
            if (transactions.isEmpty()) {
                appendLine("- No recent transactions found.")
            } else {
                transactions.forEach { txn ->
                    appendLine("- ${txn.transactionDate}: ${txn.description} (${txn.categoryName}) - $${"%.2f".format(txn.amount)} [${txn.transactionType}]")
                }
            }
            appendLine()
            appendLine("Please analyze this raw data to calculate the financial health score and provide recommendations.")
        }
    }

    private fun parseRecommendationsToList(text: String): List<String> {
        return text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && (it.first().isDigit() || it.startsWith("-") || it.startsWith("•")) }
            .map { it.replaceFirst(Regex("^\\d+\\.\\s*|[-•]\\s*"), "") }
    }
}
