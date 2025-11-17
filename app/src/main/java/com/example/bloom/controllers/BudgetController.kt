package com.example.bloom.controllers

import com.example.bloom.SupabaseClient
import com.example.bloom.datamodels.BudgetSummaryData
import com.example.bloom.datamodels.CategoryData
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.LocalDate

class BudgetController {
    private val supabase = SupabaseClient.client

    //get current user ID
    private fun getUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }


    // =====================================================
    // BUDGET SUMMARY OPERATIONS
    // =====================================================



    /**
     * function to fetch budget summary for current month
     */
    suspend fun getCurrentMonthBudget(): Result<BudgetSummaryData?> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentDate = LocalDate.now()
            val month = currentDate.monthValue
            val year = currentDate.year

            val budgets = supabase.from("budget_summary")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("month", month)
                        eq("year", year)
                    }
                }
                .decodeList<BudgetSummaryData>()

            Result.success(budgets.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * function to create or update budget summary for current month
     */
    suspend fun upsertBudgetSummary(
        monthlyBudget: Double,
        savingsGoal: Double,
        currentSavings: Double
    ): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentDate = LocalDate.now()
            val month = currentDate.monthValue
            val year = currentDate.year

            //checking if budget already exists
            val existing = getCurrentMonthBudget().getOrNull()

            if (existing != null) {
                //update existing budget
                supabase.from("budget_summary")
                    .update({
                        set("monthly_budget", monthlyBudget)
                        set("savings_goal", savingsGoal)
                        set("current_savings", currentSavings)
                    }) {
                        filter {
                            eq("user_id", userId)
                            eq("month", month)
                            eq("year", year)
                        }
                    }
            } else {
                //insert new budget
                val newBudget = BudgetSummaryData(
                    user_id = userId,
                    month = month,
                    year = year,
                    monthly_budget = monthlyBudget,
                    savings_goal = savingsGoal,
                    current_savings = currentSavings
                )

                supabase.from("budget_summary")
                    .insert(newBudget)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =====================================================
    // CATEGORY OPERATIONS
    // =====================================================

    /**
     * function to fetch all categories for current user
     */
    suspend fun getUserCategories(): Result<List<CategoryData>> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            val categories = supabase.from("categories")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<CategoryData>()

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * function to create a new category
     */
    suspend fun createCategory(
        name: String,
        colorHex: String,
        iconName: String?,
        budgetAllocation: Double = 0.0,
        categoryType: String = "expense"
    ): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            //checking if category name already exists
            val existing = supabase.from("categories")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("name", name)
                    }
                }
                .decodeList<CategoryData>()

            if (existing.isNotEmpty()) {
                return Result.failure(Exception("Category with name '$name' already exists"))
            }

            val newCategory = CategoryData(
                user_id = userId,
                name = name,
                color_hex = colorHex,
                icon_name = iconName,
                budget_allocation = budgetAllocation,
                category_type = categoryType,
                is_default = false
            )

            supabase.from("categories")
                .insert(newCategory)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * function to update an existing category
     */
    suspend fun updateCategory(
        categoryId: String,
        name: String,
        colorHex: String,
        iconName: String?,
        budgetAllocation: Double,
        categoryType: String = "expense"
    ): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("categories")
                .update({
                    set("name", name)
                    set("color_hex", colorHex)
                    set("icon_name", iconName)
                    set("budget_allocation", budgetAllocation)
                    set("category_type", categoryType)
                }) {
                    filter {
                        eq("id", categoryId)
                        eq("user_id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    /**
     * function to delete a category
     */
    suspend fun deleteCategory(categoryId: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("categories")
                .delete {
                    filter {
                        eq("id", categoryId)
                        eq("user_id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }





    /**
     * function to get total spent amount for a category in current month
     */
    suspend fun getCategorySpent(categoryId: String): Result<Double> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentDate = LocalDate.now()
            val startOfMonth = currentDate.withDayOfMonth(1).toString()
            val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

            //querying transactions for this category in current month
            val response = supabase.from("transactions")
                .select(columns = Columns.raw("amount")) {
                    filter {
                        eq("user_id", userId)
                        eq("category_id", categoryId)
                        eq("transaction_type", "expense")
                        gte("transaction_date", startOfMonth)
                        lte("transaction_date", endOfMonth)
                    }
                }

            //summing up all amounts
            val transactions = response.decodeList<Map<String, Any>>()
            val total = transactions.sumOf {
                (it["amount"] as? Number)?.toDouble() ?: 0.0
            }

            Result.success(total)
        } catch (e: Exception) {
            //if table doesn't exist yet or no transactions, return 0
            Result.success(0.0)
        }
    }




    /**
     * function to get total spent for current month (all categories)
     */
    suspend fun getTotalSpentThisMonth(): Result<Double> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentDate = LocalDate.now()
            val startOfMonth = currentDate.withDayOfMonth(1).toString()
            val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

            val response = supabase.from("transactions")
                .select(columns = Columns.raw("amount")) {
                    filter {
                        eq("user_id", userId)
                        eq("transaction_type", "expense")
                        gte("transaction_date", startOfMonth)
                        lte("transaction_date", endOfMonth)
                    }
                }

            val transactions = response.decodeList<Map<String, Any>>()
            val total = transactions.sumOf {
                (it["amount"] as? Number)?.toDouble() ?: 0.0
            }

            Result.success(total)
        } catch (e: Exception) {
            Result.success(0.0)
        }
    }
}