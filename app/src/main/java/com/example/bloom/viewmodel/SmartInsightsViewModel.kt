package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.BudgetController
import com.example.bloom.controllers.IncomeController
import com.example.bloom.controllers.ExpensesController
import com.example.bloom.controllers.BloomAIController
import com.example.bloom.datamodels.IncomeData
import com.example.bloom.datamodels.ExpenseData
import com.example.bloom.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class SmartInsightsViewModel : ViewModel() {

    private val budgetController = BudgetController()
    private val incomeController = IncomeController()
    private val expensesController = ExpensesController()
    private val aiController = BloomAIController()
    private val supabase = SupabaseClient.client

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

    // Income and Expense data for analysis
    var incomeList by mutableStateOf<List<IncomeData>>(emptyList())
        private set

    var expensesList by mutableStateOf<List<ExpenseData>>(emptyList())
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
     * Load income and expense data for analysis
     */
    private suspend fun loadTransactionData() {
        val currentDate = LocalDate.now()
        val firstDayOfMonth = LocalDate.of(currentDate.year, currentDate.month, 1).toString()
        val lastDayOfMonth = currentDate.toString()

        // Load current month income
        incomeController.getIncomeByDateRange(firstDayOfMonth, lastDayOfMonth).fold(
            onSuccess = { income ->
                incomeList = income
                totalIncome = income.sumOf { it.amount }
            },
            onFailure = { e ->
                throw e
            }
        )

        // Load current month expenses
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User not authenticated")

        try {
            val allExpenses = expensesController.getExpenses(userId)
            // Filter expenses by date range
            expensesList = allExpenses.filter { expense ->
                expense.due_date >= firstDayOfMonth && expense.due_date <= lastDayOfMonth
            }
            totalSpent = expensesList.sumOf { it.amount }
        } catch (e: Exception) {
            throw e
        }
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

        // 6. Largest expense
        val largestExpense = findLargestTransaction()
        if (largestExpense != null) {
            generatedInsights.add(
                Insight(
                    title = "Largest Expense",
                    description = "Your biggest expense was $${String.format("%.2f", largestExpense.amount)} for ${largestExpense.name}",
                    type = InsightType.WARNING,
                    value = "$${String.format("%.2f", largestExpense.amount)}"
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
     * Find top spending category/expense name
     */
    private fun findTopSpendingCategory(): Pair<String, Double>? {
        if (expensesList.isEmpty()) return null

        val categorySpending = expensesList
            .groupBy { it.name }
            .mapValues { it.value.sumOf { expense -> expense.amount } }

        return categorySpending.maxByOrNull { it.value }?.let { Pair(it.key, it.value) }
    }

    /**
     * Analyze spending trend compared to previous months
     */
    private fun analyzeSpendingTrend(): Insight? {
        // TODO: Calculate average from historical expense data
        // For now, skip this insight as ExpensesController doesn't have getAverageMonthlyExpenses
        // This can be implemented by fetching expenses from previous months and calculating average
        return null
    }

    /**
     * Analyze day of week spending patterns
     */
    private fun analyzeDayOfWeekPattern(): Insight? {
        if (expensesList.isEmpty()) return null

        try {
            val daySpending = expensesList
                .groupBy { LocalDate.parse(it.due_date).dayOfWeek }
                .mapValues { it.value.sumOf { expense -> expense.amount } }

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
     * Find largest expense
     */
    private fun findLargestTransaction(): ExpenseData? {
        return expensesList.maxByOrNull { it.amount }
    }

    /**
     * Analyze transaction frequency
     */
    private fun analyzeTransactionFrequency(): Insight? {
        val totalTransactions = incomeList.size + expensesList.size
        if (totalTransactions == 0) return null

        val currentDate = LocalDate.now()
        val daysInMonth = currentDate.dayOfMonth
        val avgPerDay = totalTransactions.toDouble() / daysInMonth

        return if (avgPerDay >= 3) {
            Insight(
                title = "Active Tracking",
                description = "You're recording ${String.format("%.1f", avgPerDay)} entries per day on average",
                type = InsightType.SPENDING_TREND,
                value = "$totalTransactions entries"
            )
        } else if (totalTransactions <= 5) {
            Insight(
                title = "Light Activity",
                description = "Only $totalTransactions entries this month. Consider tracking all income and expenses!",
                type = InsightType.SAVINGS_TIP,
                value = "$totalTransactions entries"
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
        val expenseBreakdown = expensesList
            .groupBy { it.name }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val incomeBreakdown = incomeList
            .groupBy { it.source }
            .mapValues { it.value.sumOf { income -> income.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        return buildString {
            appendLine("Financial Summary for ${LocalDate.now().month} ${LocalDate.now().year}:")
            appendLine("Total Income: $${String.format("%.2f", totalIncome)}")
            appendLine("Total Expenses: $${String.format("%.2f", totalSpent)}")
            appendLine("Net Savings: $${String.format("%.2f", totalIncome - totalSpent)}")

            if (incomeBreakdown.isNotEmpty()) {
                appendLine("\nTop Income Sources:")
                incomeBreakdown.forEach { (source, amount) ->
                    val percentage = if (totalIncome > 0) (amount / totalIncome * 100).toInt() else 0
                    appendLine("- $source: $${"%.2f".format(amount)} ($percentage%)")
                }
            }

            if (expenseBreakdown.isNotEmpty()) {
                appendLine("\nTop 5 Expenses:")
                expenseBreakdown.forEach { (name, amount) ->
                    val percentage = if (totalSpent > 0) (amount / totalSpent * 100).toInt() else 0
                    appendLine("- $name: $${"%.2f".format(amount)} ($percentage%)")
                }
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
