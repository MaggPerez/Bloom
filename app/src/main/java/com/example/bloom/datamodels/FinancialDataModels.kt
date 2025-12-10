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

// Payment method enum
enum class PaymentMethod {
    CASH,
    DEBIT_CARD,
    CREDIT_CARD,
    BANK_TRANSFER,
    DIGITAL_WALLET,
    OTHER;

    fun displayName(): String = when (this) {
        CASH -> "Cash"
        DEBIT_CARD -> "Debit Card"
        CREDIT_CARD -> "Credit Card"
        BANK_TRANSFER -> "Bank Transfer"
        DIGITAL_WALLET -> "Digital Wallet"
        OTHER -> "Other"
    }
}

// Serializable transaction data model for Supabase
@Serializable
data class TransactionData(
    val id: String? = null,
    val user_id: String,
    val category_id: String? = null,
    val transaction_name: String? = null, // User-defined name for the transaction
    val amount: Double,
    val transaction_date: String, // DATE format: "yyyy-MM-dd"
    val transaction_type: String, // "expense" or "income"
    val description: String? = null,
    val notes: String? = null,
    val payment_method: String? = null, // PaymentMethod enum value as string
    val location: String? = null,
    val receipt_url: String? = null,
    val tags: String? = null, // Stored as comma-separated string for compatibility
    val is_recurring: Boolean? = false,
    val recurring_frequency: String? = null, // "daily", "weekly", "monthly", "yearly"
    val created_at: String? = null,
    val updated_at: String? = null
)

// UI model combining transaction with category information
data class TransactionWithCategory(
    val id: String,
    val userId: String,
    val categoryId: String?,
    val transactionName: String?, // User-defined name for the transaction
    val categoryName: String?,
    val categoryColorHex: String?,
    val categoryIconName: String?,
    val amount: Double,
    val transactionDate: String,
    val transactionType: String,
    val description: String?,
    val paymentMethod: PaymentMethod?,
    val tags: List<String>,
    val receiptUrl: String?
) {
    val categoryColor: Color get() = categoryColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.Gray
    val tagsList: String get() = tags.joinToString(", ")
    val isExpense: Boolean get() = transactionType == "expense"
    val isIncome: Boolean get() = transactionType == "income"
    // Display transaction name if available, otherwise fall back to category name
    val displayName: String get() = transactionName ?: categoryName ?: "Unknown"
}

// Filter state for transactions
data class TransactionFilter(
    val startDate: String? = null,
    val endDate: String? = null,
    val categoryIds: List<String> = emptyList(),
    val transactionTypes: List<String> = emptyList(), // "expense", "income"
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val searchQuery: String? = null
) {
    fun isActive(): Boolean {
        return startDate != null || endDate != null || categoryIds.isNotEmpty() ||
                transactionTypes.isNotEmpty() || minAmount != null || maxAmount != null ||
                !searchQuery.isNullOrBlank()
    }
}

@Serializable
data class ExpenseData(
    val id: String? = null,
    val user_id: String,
    val name: String,
    val amount: Double,
    val due_date: String, // Format: yyyy-MM-dd
    val image_url: String? = null,
    val icon_name: String? = null, // Name of the default Android icon
    val color_hex: String? = null, // Hex code for the icon color
    val tags: String? = null, // Comma-separated tags
    val recurring_frequency: String? = null, // "Daily", "Weekly", "Monthly", "Yearly"
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class IncomeData(
    val id: String? = null,
    val user_id: String,
    val source: String, // "Salary", "Freelance", "Investment", "Gift", etc.
    val amount: Double,
    val income_date: String, // Format: yyyy-MM-dd
    val description: String? = null,
    val image_url: String? = null,
    val icon_name: String? = null, // Name of the default Android icon
    val color_hex: String? = null, // Hex code for the icon color
    val tags: String? = null, // Comma-separated tags
    val is_recurring: Boolean = false,
    val recurring_frequency: String? = null, // "Daily", "Weekly", "Monthly", "Yearly"
    val created_at: String? = null,
    val updated_at: String? = null
)