package com.example.bloom.datamodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Data models for UI

class FinancialDataModels {
    data class BudgetSummary(
        val monthlyBudget: Double,
        val totalSpent: Double,
        val savingsGoal: Double,
        val currentSavings: Double
    ) {
        val remaining: Double get() = monthlyBudget - totalSpent
        val remainingPercentage: Float get() = ((remaining / monthlyBudget) * 100).toFloat().coerceIn(0f, 100f)
        val spentPercentage: Float get() = ((totalSpent / monthlyBudget) * 100).toFloat()
        val savingsPercentage: Float get() = ((currentSavings / savingsGoal) * 100).toFloat().coerceIn(0f, 100f)
    }

    data class CategorySpending(
        val category: String,
        val amount: Double,
        val color: Color,
        val icon: ImageVector
    )

    data class QuickAction(
        val label: String,
        val icon: ImageVector,
        val onClick: () -> Unit
    )
}

// Serializable data models for Supabase integration
@Serializable
data class BudgetSummaryData(
    val id: String? = null,
    val user_id: String,
    val month: Int,
    val year: Int,
    val monthly_budget: Double,
    val savings_goal: Double = 0.0,
    val current_savings: Double = 0.0,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class CategoryData(
    val id: String? = null,
    val user_id: String,
    val name: String,
    val color_hex: String,
    val icon_name: String? = null,
    val category_type: String = "expense", // 'expense' or 'income'
    val budget_allocation: Double = 0.0, // Budget allocated to this category
    val is_default: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)

// UI model for category with budget info
data class CategoryWithBudget(
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String?,
    val budgetAllocation: Double,
    val spent: Double = 0.0,
    val categoryType: String = "expense"
) {
    val remaining: Double get() = budgetAllocation - spent
    val spentPercentage: Float
        get() = if (budgetAllocation > 0) ((spent / budgetAllocation) * 100).toFloat()
            .coerceIn(0f, 100f) else 0f
    val color: Color get() = Color(android.graphics.Color.parseColor(colorHex))
}