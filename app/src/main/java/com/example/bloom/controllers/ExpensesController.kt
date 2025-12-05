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
}