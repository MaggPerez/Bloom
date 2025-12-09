package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.BudgetController
import com.example.bloom.controllers.TransactionController
import com.example.bloom.controllers.BloomAIController
import com.example.bloom.datamodels.TransactionWithCategory
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class SmartInsightsViewModel : ViewModel() {

    private val budgetController = BudgetController()
    private val transactionController = TransactionController()
    private val aiController = BloomAIController()

    // =====================================================
    // STATE VARIABLES
    // =====================================================

    // Insights
    var insights by mutableStateOf<List<Insight>>(emptyList())
        private set

    // AI-powered insights
    var aiInsights by mutableStateOf<String?>(null)
        private set

    var isLoadingAI by mutableStateOf(false)
        private set

    // Transaction data for analysis
    var transactions by mutableStateOf<List<TransactionWithCategory>>(emptyList())
        private set

    var totalSpent by mutableStateOf(0.0)
        private set

    var totalIncome by mutableStateOf(0.0)
        private set

    // Loading and error states
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // =====================================================
    // DATA MODELS
    // =====================================================

    data class Insight(
        val title: String,
        val description: String,
        val type: InsightType,
        val value: String? = null,
        val change: Double? = null // Percentage change
    )

    enum class InsightType {
        SPENDING_TREND,
        TOP_CATEGORY,
        DAY_PATTERN,
        BUDGET_ALERT,
        SAVINGS_TIP,
        POSITIVE,
        WARNING
    }

    // =====================================================
    // INITIALIZATION
    // =====================================================

    init {
        loadInsights()
    }

    // =====================================================
    // INSIGHTS GENERATION
    // =====================================================

    /**
     * Main function to load and generate insights
     */
    fun loadInsights() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Load transaction data for current month
                loadTransactionData()

                // Generate insights
                generateInsights()

            } catch (e: Exception) {
                errorMessage = "Failed to load insights: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Load transaction data for analysis
     */
    private suspend fun loadTransactionData() {
        val currentDate = LocalDate.now()
        val firstDayOfMonth = LocalDate.of(currentDate.year, currentDate.month, 1).toString()
        val lastDayOfMonth = currentDate.toString()

        // Load current month transactions
        transactionController.fetchTransactions(
            page = 0,
            pageSize = 500,
            filter = com.example.bloom.datamodels.TransactionFilter(
                startDate = firstDayOfMonth,
                endDate = lastDayOfMonth
            )
        ).fold(
            onSuccess = { txns ->
                transactions = txns

                // Calculate totals
                totalSpent = txns.filter { it.transactionType == "expense" }.sumOf { it.amount }
                totalIncome = txns.filter { it.transactionType == "income" }.sumOf { it.amount }
            },
            onFailure = { e ->
                throw e
            }
        )
    }

    /**
     * Generate smart insights from transaction data
     */
    private fun generateInsights() {
        val generatedInsights = mutableListOf<Insight>()

        // 1. Top spending category
        val topCategory = findTopSpendingCategory()
        if (topCategory != null) {
            val percentage = (topCategory.second / totalSpent * 100).toInt()
            generatedInsights.add(
                Insight(
                    title = "Top Spending Category",
                    description = "${topCategory.first} is your biggest expense this month at $percentage% of total spending",
                    type = InsightType.TOP_CATEGORY,
                    value = "$${String.format("%.2f", topCategory.second)}"
                )
            )
        }

        // 2. Spending trend compared to average
        val spendingTrend = analyzeSpendingTrend()
        if (spendingTrend != null) {
            generatedInsights.add(spendingTrend)
        }

        // 3. Day of week spending pattern
        val dayPattern = analyzeDayOfWeekPattern()
        if (dayPattern != null) {
            generatedInsights.add(dayPattern)
        }

        // 4. Budget status
        val budgetInsight = analyzeBudgetStatus()
        if (budgetInsight != null) {
            generatedInsights.add(budgetInsight)
        }

        // 5. Income vs Expenses
        val incomeVsExpense = analyzeIncomeVsExpenses()
        if (incomeVsExpense != null) {
            generatedInsights.add(incomeVsExpense)
        }

        // 6. Largest transaction
        val largestTransaction = findLargestTransaction()
        if (largestTransaction != null) {
            generatedInsights.add(
                Insight(
                    title = "Largest Transaction",
                    description = "Your biggest expense was $${String.format("%.2f", largestTransaction.amount)} in ${largestTransaction.categoryName}",
                    type = InsightType.WARNING,
                    value = "$${String.format("%.2f", largestTransaction.amount)}"
                )
            )
        }

        // 7. Transaction frequency
        val frequency = analyzeTransactionFrequency()
        if (frequency != null) {
            generatedInsights.add(frequency)
        }

        insights = generatedInsights
    }

    /**
     * Find top spending category
     */
    private fun findTopSpendingCategory(): Pair<String, Double>? {
        val expenseTransactions = transactions.filter { it.transactionType == "expense" }
        if (expenseTransactions.isEmpty()) return null

        val categorySpending = expenseTransactions
            .groupBy { it.categoryName }
            .mapValues { it.value.sumOf { txn -> txn.amount } }

        return categorySpending.maxByOrNull { it.value }?.toPair()
    }

    /**
     * Analyze spending trend compared to previous months
     */
    private fun analyzeSpendingTrend(): Insight? {
        viewModelScope.launch {
            transactionController.getAverageMonthlyExpenses().fold(
                onSuccess = { avgExpense ->
                    if (avgExpense > 0 && totalSpent > 0) {
                        val change = ((totalSpent - avgExpense) / avgExpense) * 100

                        val insight = if (change > 10) {
                            Insight(
                                title = "Spending Increased",
                                description = "You're spending ${String.format("%.1f", change)}% more than your 3-month average",
                                type = InsightType.WARNING,
                                change = change
                            )
                        } else if (change < -10) {
                            Insight(
                                title = "Great Job!",
                                description = "You're spending ${String.format("%.1f", kotlin.math.abs(change))}% less than your 3-month average",
                                type = InsightType.POSITIVE,
                                change = change
                            )
                        } else {
                            Insight(
                                title = "Consistent Spending",
                                description = "Your spending this month is similar to your 3-month average",
                                type = InsightType.SPENDING_TREND
                            )
                        }

                        insights = insights + insight
                    }
                },
                onFailure = {}
            )
        }

        return null // Will be added async
    }

    /**
     * Analyze day of week spending patterns
     */
    private fun analyzeDayOfWeekPattern(): Insight? {
        val expenseTransactions = transactions.filter { it.transactionType == "expense" }
        if (expenseTransactions.isEmpty()) return null

        try {
            val daySpending = expenseTransactions
                .groupBy { LocalDate.parse(it.transactionDate).dayOfWeek }
                .mapValues { it.value.sumOf { txn -> txn.amount } }

            val topDay = daySpending.maxByOrNull { it.value }?.key
            if (topDay != null) {
                val dayName = topDay.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val amount = daySpending[topDay] ?: 0.0

                return Insight(
                    title = "Peak Spending Day",
                    description = "You spend the most on ${dayName}s - $${String.format("%.2f", amount)} this month",
                    type = InsightType.DAY_PATTERN,
                    value = dayName
                )
            }
        } catch (e: Exception) {
            // If date parsing fails, skip this insight
        }

        return null
    }

    /**
     * Analyze budget status
     */
    private fun analyzeBudgetStatus(): Insight? {
        viewModelScope.launch {
            budgetController.getCurrentMonthBudget().fold(
                onSuccess = { budget ->
                    if (budget != null && budget.monthly_budget > 0) {
                        val spent = totalSpent
                        val monthlyBudget = budget.monthly_budget
                        val percentage = (spent / monthlyBudget) * 100

                        val insight = when {
                            percentage >= 100 -> Insight(
                                title = "Budget Exceeded",
                                description = "You've spent ${String.format("%.1f", percentage)}% of your monthly budget",
                                type = InsightType.BUDGET_ALERT,
                                value = "${String.format("%.1f", percentage)}%"
                            )
                            percentage >= 80 -> Insight(
                                title = "Approaching Budget Limit",
                                description = "You've used ${String.format("%.1f", percentage)}% of your budget. Watch your spending!",
                                type = InsightType.WARNING,
                                value = "${String.format("%.1f", percentage)}%"
                            )
                            percentage >= 50 -> Insight(
                                title = "On Track",
                                description = "You've used ${String.format("%.1f", percentage)}% of your budget. Keep it up!",
                                type = InsightType.POSITIVE,
                                value = "${String.format("%.1f", percentage)}%"
                            )
                            else -> Insight(
                                title = "Great Start",
                                description = "Only ${String.format("%.1f", percentage)}% of budget used. Excellent control!",
                                type = InsightType.POSITIVE,
                                value = "${String.format("%.1f", percentage)}%"
                            )
                        }

                        insights = insights + insight
                    }
                },
                onFailure = {}
            )
        }

        return null // Will be added async
    }

    /**
     * Analyze income vs expenses
     */
    private fun analyzeIncomeVsExpenses(): Insight? {
        if (totalIncome <= 0) return null

        val netSavings = totalIncome - totalSpent
        val savingsRate = (netSavings / totalIncome) * 100

        return if (netSavings > 0) {
            Insight(
                title = "Positive Cash Flow",
                description = "You're saving ${String.format("%.1f", savingsRate)}% of your income this month",
                type = InsightType.POSITIVE,
                value = "$${String.format("%.2f", netSavings)}"
            )
        } else {
            Insight(
                title = "Spending More Than Earning",
                description = "Your expenses exceed your income by $${String.format("%.2f", kotlin.math.abs(netSavings))}",
                type = InsightType.WARNING,
                value = "$${String.format("%.2f", kotlin.math.abs(netSavings))}"
            )
        }
    }

    /**
     * Find largest transaction
     */
    private fun findLargestTransaction(): TransactionWithCategory? {
        return transactions
            .filter { it.transactionType == "expense" }
            .maxByOrNull { it.amount }
    }

    /**
     * Analyze transaction frequency
     */
    private fun analyzeTransactionFrequency(): Insight? {
        if (transactions.isEmpty()) return null

        val currentDate = LocalDate.now()
        val daysInMonth = currentDate.dayOfMonth
        val avgPerDay = transactions.size.toDouble() / daysInMonth

        return if (avgPerDay >= 3) {
            Insight(
                title = "Active Spending",
                description = "You're making ${String.format("%.1f", avgPerDay)} transactions per day on average",
                type = InsightType.SPENDING_TREND,
                value = "${transactions.size} transactions"
            )
        } else if (transactions.size <= 5) {
            Insight(
                title = "Light Activity",
                description = "Only ${transactions.size} transactions this month. Consider tracking all expenses!",
                type = InsightType.SAVINGS_TIP,
                value = "${transactions.size} transactions"
            )
        } else {
            null
        }
    }

    /**
     * Generate AI-powered insights using backend
     */
    fun generateAIInsights() {
        viewModelScope.launch {
            isLoadingAI = true
            errorMessage = null

            try {
                // Prepare transaction summary for AI
                val summary = buildTransactionSummary()

                // Call AI endpoint
                aiController.generateInsights(summary).fold(
                    onSuccess = { response ->
                        aiInsights = response
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to generate AI insights: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to generate AI insights: ${e.message}"
            } finally {
                isLoadingAI = false
            }
        }
    }

    /**
     * Build transaction summary for AI analysis
     */
    private fun buildTransactionSummary(): String {
        val categoryBreakdown = transactions
            .filter { it.transactionType == "expense" }
            .groupBy { it.categoryName }
            .mapValues { it.value.sumOf { txn -> txn.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        return buildString {
            appendLine("Financial Summary for ${LocalDate.now().month} ${LocalDate.now().year}:")
            appendLine("Total Income: $${String.format("%.2f", totalIncome)}")
            appendLine("Total Expenses: $${String.format("%.2f", totalSpent)}")
            appendLine("Net Savings: $${String.format("%.2f", totalIncome - totalSpent)}")
            appendLine("\nTop 5 Spending Categories:")
            categoryBreakdown.forEach { (category, amount) ->
                val percentage = (amount / totalSpent * 100).toInt()
                appendLine("- $category: $${"%.2f".format(amount)} ($percentage%)")
            }
            appendLine("\nProvide 3-4 personalized financial insights and actionable recommendations.")
        }
    }

    /**
     * Refresh insights
     */
    fun refresh() {
        loadInsights()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage = null
    }
}
