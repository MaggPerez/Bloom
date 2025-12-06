package com.example.bloom.datamodels

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

// =====================================================
// TIME PERIOD & FILTERING
// =====================================================

/**
 * Time period options for analytics filtering
 */
enum class TimePeriod {
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM;

    fun displayName(): String = when (this) {
        WEEKLY -> "Weekly"
        MONTHLY -> "Monthly"
        YEARLY -> "Yearly"
        CUSTOM -> "Custom Range"
    }
}

/**
 * Date range for analytics queries
 */
data class DateRange(
    val startDate: String, // yyyy-MM-dd format
    val endDate: String    // yyyy-MM-dd format
)

// =====================================================
// ANALYTICS DATA MODELS
// =====================================================

/**
 * Monthly trend data for income/expense charts
 * Shows financial performance over time
 */
data class MonthlyTrendData(
    val month: String,              // "Jan 2024" format for display
    val monthYear: Pair<Int, Int>,  // (month, year) for sorting
    val totalIncome: Double,
    val totalExpenses: Double,
    val netAmount: Double,          // income - expenses
    val transactionCount: Int
)

/**
 * Category breakdown for pie charts and spending analysis
 * Shows where money is being spent
 */
data class CategoryBreakdownData(
    val categoryId: String,
    val categoryName: String,
    val categoryColor: Color,
    val totalSpent: Double,
    val percentage: Float,          // Percentage of total spending
    val transactionCount: Int,
    val averageTransaction: Double
)

/**
 * Income vs Expense comparison data
 * For bar charts and comparisons
 */
data class IncomeVsExpenseData(
    val period: String,             // "Week 1", "January", "2024"
    val income: Double,
    val expenses: Double,
    val balance: Double             // income - expenses
)

/**
 * Budget adherence tracking
 * Shows how well user is sticking to budget
 */
data class BudgetAdherenceData(
    val month: String,              // "Jan 2024"
    val budgetAmount: Double,
    val actualSpent: Double,
    val difference: Double,         // budget - spent (positive = under budget)
    val adherencePercentage: Float, // (spent / budget) * 100
    val isOverBudget: Boolean
)

/**
 * Recurring bills analysis
 * Helps users plan for fixed monthly expenses
 */
data class RecurringBillSummary(
    val totalRecurringAmount: Double,           // Total monthly recurring costs
    val upcomingBills: List<UpcomingBill>,      // Bills sorted by due date
    val recurringByFrequency: Map<String, Double> // "Monthly" -> $500, "Weekly" -> $50
)

/**
 * Individual upcoming bill
 */
data class UpcomingBill(
    val name: String,
    val amount: Double,
    val dueDate: String,            // yyyy-MM-dd
    val frequency: String,          // "Daily", "Weekly", "Monthly", "Yearly"
    val daysUntilDue: Int
)

/**
 * Spending insights and recommendations
 * Provides actionable insights for college students/low-income users
 */
data class SpendingInsights(
    val topCategory: CategoryBreakdownData?,    // Highest spending category
    val averageDailySpending: Double,           // Helps with daily budgeting
    val largestTransaction: TransactionSummary?, // Identifies splurges
    val mostFrequentCategory: String?,          // Most common spending area
    val budgetHealthStatus: BudgetHealthStatus  // Overall budget health
)

/**
 * Transaction summary for insights
 */
data class TransactionSummary(
    val amount: Double,
    val categoryName: String,
    val date: String,               // yyyy-MM-dd
    val description: String?
)

/**
 * Budget health status indicator
 * Provides visual feedback on spending habits
 */
enum class BudgetHealthStatus {
    EXCELLENT,  // < 70% of budget spent
    GOOD,       // 70-85% spent
    WARNING,    // 85-95% spent
    CRITICAL;   // > 95% spent (overspending risk)

    fun displayName(): String = when (this) {
        EXCELLENT -> "Excellent"
        GOOD -> "Good"
        WARNING -> "Warning"
        CRITICAL -> "Critical"
    }

    fun color(): Color = when (this) {
        EXCELLENT -> Color(0xFF10B981) // Green
        GOOD -> Color(0xFF3B82F6)      // Blue
        WARNING -> Color(0xFFF59E0B)   // Yellow/Orange
        CRITICAL -> Color(0xFFEF4444)  // Red
    }

    fun description(): String = when (this) {
        EXCELLENT -> "You're well within budget. Great job!"
        GOOD -> "On track with your budget."
        WARNING -> "Approaching budget limit. Watch spending."
        CRITICAL -> "Over budget or very close. Review expenses."
    }
}

/**
 * Main analytics summary
 * Aggregates all analytics data for a given time period
 */
data class AnalyticsSummary(
    val dateRange: DateRange,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netSavings: Double,                         // income - expenses
    val monthlyTrends: List<MonthlyTrendData>,
    val categoryBreakdown: List<CategoryBreakdownData>,
    val budgetAdherence: List<BudgetAdherenceData>,
    val recurringBillSummary: RecurringBillSummary,
    val insights: SpendingInsights
)

// =====================================================
// SUPABASE QUERY RESULT MODELS
// =====================================================

/**
 * Result model for monthly_income_expense_summary view
 */
@Serializable
data class MonthlyAggregateResult(
    val month: Int,
    val year: Int,
    val total_income: Double = 0.0,
    val total_expenses: Double = 0.0,
    val transaction_count: Int = 0
)

/**
 * Result model for category_spending_analysis view
 */
@Serializable
data class CategoryAggregateResult(
    val category_id: String,
    val category_name: String,
    val color_hex: String,
    val total_amount: Double,
    val transaction_count: Int,
    val average_amount: Double = 0.0
)

/**
 * Result model for budget_performance_analysis view
 */
@Serializable
data class BudgetPerformanceResult(
    val month: Int,
    val year: Int,
    val monthly_budget: Double,
    val savings_goal: Double = 0.0,
    val current_savings: Double = 0.0,
    val actual_spent: Double = 0.0,
    val budget_remaining: Double = 0.0,
    val spent_percentage: Double = 0.0
)

/**
 * Result model for recurring_bills_summary view
 */
@Serializable
data class RecurringBillsResult(
    val recurring_frequency: String,
    val bill_count: Int,
    val total_amount: Double
)
