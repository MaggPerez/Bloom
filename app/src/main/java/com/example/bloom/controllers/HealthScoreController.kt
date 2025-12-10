package com.example.bloom.controllers

import com.example.bloom.SupabaseClient
import com.example.bloom.datamodels.ExpenseData
import com.example.bloom.datamodels.IncomeData
import com.example.bloom.datamodels.BudgetSummaryData
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Controller for Health Score calculations
 * Provides comprehensive financial data from expenses, income, and budget tables
 */
class HealthScoreController {
    private val supabase = SupabaseClient.client

    /**
     * Get current authenticated user ID
     */
    private fun getUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // =====================================================
    // INCOME OPERATIONS
    // =====================================================

    /**
     * Get total income for current month
     */
    suspend fun getMonthlyIncome(): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val currentDate = LocalDate.now()
                val startOfMonth = currentDate.withDayOfMonth(1).toString()
                val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

                val income = supabase.from("income")
                    .select() {
                        filter {
                            eq("user_id", userId)
                            gte("income_date", startOfMonth)
                            lte("income_date", endOfMonth)
                        }
                    }
                    .decodeList<IncomeData>()

                val total = income.sumOf { it.amount }
                Result.success(total)
            } catch (e: Exception) {
                Result.success(0.0) // Return 0 if no income data
            }
        }
    }

    /**
     * Get all income for current month (detailed)
     */
    suspend fun getMonthlyIncomeDetailed(): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val currentDate = LocalDate.now()
                val startOfMonth = currentDate.withDayOfMonth(1).toString()
                val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

                val income = supabase.from("income")
                    .select() {
                        filter {
                            eq("user_id", userId)
                            gte("income_date", startOfMonth)
                            lte("income_date", endOfMonth)
                        }
                        order("income_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<IncomeData>()

                Result.success(income)
            } catch (e: Exception) {
                Result.success(emptyList())
            }
        }
    }

    /**
     * Get average monthly income over last N months
     */
    suspend fun getAverageMonthlyIncome(months: Int = 3): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val currentDate = LocalDate.now()
                val startDate = currentDate.minusMonths(months.toLong()).withDayOfMonth(1).toString()
                val endDate = currentDate.toString()

                val income = supabase.from("income")
                    .select() {
                        filter {
                            eq("user_id", userId)
                            gte("income_date", startDate)
                            lte("income_date", endDate)
                        }
                    }
                    .decodeList<IncomeData>()

                val total = income.sumOf { it.amount }
                val average = if (months > 0) total / months else 0.0

                Result.success(average)
            } catch (e: Exception) {
                Result.success(0.0)
            }
        }
    }

    // =====================================================
    // EXPENSE OPERATIONS
    // =====================================================

    /**
     * Get average monthly expenses over last N months
     */
    suspend fun getAverageMonthlyExpenses(months: Int = 3): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val currentDate = LocalDate.now()
                val startDate = currentDate.minusMonths(months.toLong()).withDayOfMonth(1).toString()
                val endDate = currentDate.toString()

                val expenses = supabase.from("expenses")
                    .select() {
                        filter {
                            eq("user_id", userId)
                            gte("due_date", startDate)
                            lte("due_date", endDate)
                        }
                    }
                    .decodeList<ExpenseData>()

                val total = expenses.sumOf { it.amount }
                val average = if (months > 0) total / months else 0.0

                Result.success(average)
            } catch (e: Exception) {
                Result.success(0.0)
            }
        }
    }

    /**
     * Get all expenses for current month (detailed)
     */
    suspend fun getMonthlyExpensesDetailed(): Result<List<ExpenseData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val currentDate = LocalDate.now()
                val startOfMonth = currentDate.withDayOfMonth(1).toString()
                val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

                val expenses = supabase.from("expenses")
                    .select() {
                        filter {
                            eq("user_id", userId)
                            gte("due_date", startOfMonth)
                            lte("due_date", endOfMonth)
                        }
                        order("due_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<ExpenseData>()

                Result.success(expenses)
            } catch (e: Exception) {
                Result.success(emptyList())
            }
        }
    }

    /**
     * Get expense breakdown by category (grouped by name)
     */
    suspend fun getExpenseBreakdown(): Result<Map<String, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val currentDate = LocalDate.now()
                val startOfMonth = currentDate.withDayOfMonth(1).toString()
                val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

                val expenses = supabase.from("expenses")
                    .select() {
                        filter {
                            eq("user_id", userId)
                            gte("due_date", startOfMonth)
                            lte("due_date", endOfMonth)
                        }
                    }
                    .decodeList<ExpenseData>()

                val breakdown = expenses
                    .groupBy { it.name }
                    .mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }

                Result.success(breakdown)
            } catch (e: Exception) {
                Result.success(emptyMap())
            }
        }
    }

    /**
     * Get recurring expenses
     */
    suspend fun getRecurringExpenses(): Result<List<ExpenseData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val expenses = supabase.from("expenses")
                    .select() {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<ExpenseData>()
                    .filter { !it.recurring_frequency.isNullOrBlank() }

                Result.success(expenses)
            } catch (e: Exception) {
                Result.success(emptyList())
            }
        }
    }

    // =====================================================
    // COMPREHENSIVE FINANCIAL SUMMARY
    // =====================================================

    /**
     * Get comprehensive financial data for AI analysis
     */
    suspend fun getComprehensiveFinancialData(): Result<FinancialHealthData> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                // Fetch all data in parallel would be better, but for simplicity we'll do sequential
                val monthlyIncome = getMonthlyIncome().getOrDefault(0.0)
                val avgMonthlyIncome = getAverageMonthlyIncome().getOrDefault(0.0)
                val avgMonthlyExpenses = getAverageMonthlyExpenses().getOrDefault(0.0)
                val monthlyIncomeDetails = getMonthlyIncomeDetailed().getOrDefault(emptyList())
                val monthlyExpenseDetails = getMonthlyExpensesDetailed().getOrDefault(emptyList())
                val expenseBreakdown = getExpenseBreakdown().getOrDefault(emptyMap())
                val recurringExpenses = getRecurringExpenses().getOrDefault(emptyList())

                // Get budget data
                val budgetController = BudgetController()
                val budget = budgetController.getCurrentMonthBudget().getOrNull()
                val totalSpent = budgetController.getTotalSpentThisMonth().getOrDefault(0.0)

                Result.success(
                    FinancialHealthData(
                        monthlyBudget = budget?.monthly_budget ?: 0.0,
                        savingsGoal = budget?.savings_goal ?: 0.0,
                        currentSavings = budget?.current_savings ?: 0.0,
                        monthlyIncome = monthlyIncome,
                        averageMonthlyIncome = avgMonthlyIncome,
                        totalSpentThisMonth = totalSpent,
                        averageMonthlyExpenses = avgMonthlyExpenses,
                        monthlyIncomeDetails = monthlyIncomeDetails,
                        monthlyExpenseDetails = monthlyExpenseDetails,
                        expenseBreakdown = expenseBreakdown,
                        recurringExpenses = recurringExpenses
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

/**
 * Comprehensive financial health data for AI analysis
 */
data class FinancialHealthData(
    val monthlyBudget: Double,
    val savingsGoal: Double,
    val currentSavings: Double,
    val monthlyIncome: Double,
    val averageMonthlyIncome: Double,
    val totalSpentThisMonth: Double,
    val averageMonthlyExpenses: Double,
    val monthlyIncomeDetails: List<IncomeData>,
    val monthlyExpenseDetails: List<ExpenseData>,
    val expenseBreakdown: Map<String, Double>,
    val recurringExpenses: List<ExpenseData>
)
