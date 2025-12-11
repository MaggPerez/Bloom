package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.ExpensesController
import com.example.bloom.datamodels.ExpenseData
import com.example.bloom.user
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpensesViewModel : ViewModel() {
    private val controller = ExpensesController()

    var expenses by mutableStateOf<List<ExpenseData>>(emptyList())
        private set

    var totalExpenses by mutableStateOf(0.0)
        private set

    var monthlyExpenses by mutableStateOf(0.0)
        private set

    var yearlyExpenses by mutableStateOf(0.0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        val currentUser = user ?: return
        isLoading = true
        viewModelScope.launch {
            val fetchedExpenses = controller.getExpenses(currentUser.id)
            expenses = fetchedExpenses
            calculateMetrics()
            isLoading = false
        }
    }

    private fun calculateMetrics() {
        var total = 0.0
        var yearlyAverage = 0.0

        expenses.forEach { expense ->
            // Total Expenses (Sum of all)
            total += expense.amount

            // Calculate annualized amount based on recurring frequency
            val annualizedAmount = when (expense.recurring_frequency?.lowercase()) {
                "daily" -> expense.amount * 365
                "weekly" -> expense.amount * 52
                "monthly" -> expense.amount * 12
                "yearly" -> expense.amount
                else -> expense.amount // If no frequency, treat as one-time (yearly)
            }

            yearlyAverage += annualizedAmount
        }

        // Calculate monthly average from yearly total
        val monthlyAverage = yearlyAverage / 12

        totalExpenses = total
        monthlyExpenses = monthlyAverage
        yearlyExpenses = yearlyAverage
    }

    fun addExpense(
        name: String,
        amount: Double,
        dueDate: String, // yyyy-MM-dd
        imageUrl: String? = null,
        iconName: String? = null,
        colorHex: String? = null,
        tags: String? = null,
        recurringFrequency: String? = null,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val currentUser = user ?: return
        
        viewModelScope.launch {
            val newExpense = ExpenseData(
                user_id = currentUser.id,
                name = name,
                amount = amount,
                due_date = dueDate,
                image_url = imageUrl,
                icon_name = iconName,
                color_hex = colorHex,
                tags = tags,
                recurring_frequency = recurringFrequency
            )
            
            val success = controller.createExpense(newExpense)
            if (success) {
                loadExpenses() // Reload to update list and totals
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun updateExpense(
        id: String,
        name: String,
        amount: Double,
        dueDate: String,
        imageUrl: String? = null,
        iconName: String? = null,
        colorHex: String? = null,
        tags: String? = null,
        recurringFrequency: String? = null,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val currentUser = user ?: return

        viewModelScope.launch {
            val updatedExpense = ExpenseData(
                id = id,
                user_id = currentUser.id,
                name = name,
                amount = amount,
                due_date = dueDate,
                image_url = imageUrl,
                icon_name = iconName,
                color_hex = colorHex,
                tags = tags,
                recurring_frequency = recurringFrequency
            )

            val success = controller.updateExpense(updatedExpense)
            if (success) {
                loadExpenses()
                onSuccess()
            } else {
                onError()
            }
        }
    }
}