package com.example.bloom.datamodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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