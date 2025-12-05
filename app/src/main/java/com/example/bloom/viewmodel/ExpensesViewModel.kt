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
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        var total = 0.0
        var monthly = 0.0
        var yearly = 0.0

        expenses.forEach { expense ->
            // Total Expenses (Sum of all)
            total += expense.amount

            try {
                // Parse due date to check for month/year matches
                // Assuming due_date is relevant for "when" the expense occurs
                val expenseDate = LocalDate.parse(expense.due_date, formatter)

                // Monthly: Matches current month and year
                if (expenseDate.month == now.month && expenseDate.year == now.year) {
                    monthly += expense.amount
                }

                // Yearly: Matches current year
                if (expenseDate.year == now.year) {
                    yearly += expense.amount
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If date parsing fails, skip month/year calc for this item
            }
        }

        totalExpenses = total
        monthlyExpenses = monthly
        yearlyExpenses = yearly
    }

    fun addExpense(
        name: String,
        amount: Double,
        dueDate: String, // yyyy-MM-dd
        imageUrl: String? = null,
        iconName: String? = null,
        colorHex: String? = null,
        tags: String? = null,
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
                tags = tags
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
}