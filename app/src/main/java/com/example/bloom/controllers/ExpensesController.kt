package com.example.bloom.controllers

import com.example.bloom.datamodels.ExpenseData
import com.example.bloom.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpensesController {

    suspend fun getExpenses(userId: String): List<ExpenseData> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("expenses")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<ExpenseData>()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun createExpense(expense: ExpenseData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                supabase.from("expenses")
                    .insert(expense)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun updateExpense(expense: ExpenseData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                expense.id?.let { id ->
                    supabase.from("expenses")
                        .update(expense) {
                            filter {
                                eq("id", id)
                            }
                        }
                    true
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun deleteExpenses(expenseIds: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                expenseIds.forEach { id ->
                    supabase.from("expenses")
                        .delete {
                            filter {
                                eq("id", id)
                            }
                        }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}