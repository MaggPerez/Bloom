package com.example.bloom.controllers

import com.example.bloom.SupabaseClient
import com.example.bloom.datamodels.IncomeData
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class IncomeController {
    private val supabase = SupabaseClient.client

    /**
     * Get current authenticated user ID
     */
    private fun getUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // =====================================================
    // INCOME CRUD OPERATIONS
    // =====================================================

    /**
     * Get all income entries for the current user
     */
    suspend fun getAllIncome(): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val income = supabase.from("income")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("income_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<IncomeData>()

                Result.success(income)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get income entries for a specific date range
     */
    suspend fun getIncomeByDateRange(
        startDate: String,
        endDate: String
    ): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val income = supabase.from("income")
                    .select {
                        filter {
                            eq("user_id", userId)
                            gte("income_date", startDate)
                            lte("income_date", endDate)
                        }
                        order("income_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<IncomeData>()

                Result.success(income)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get income entries for current month
     */
    suspend fun getCurrentMonthIncome(): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val currentDate = LocalDate.now()
                val startOfMonth = currentDate.withDayOfMonth(1).toString()
                val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

                getIncomeByDateRange(startOfMonth, endOfMonth)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get a single income entry by ID
     */
    suspend fun getIncomeById(incomeId: String): Result<IncomeData?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val income = supabase.from("income")
                    .select {
                        filter {
                            eq("id", incomeId)
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<IncomeData>()
                    .firstOrNull()

                Result.success(income)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Create a new income entry
     */
    suspend fun createIncome(income: IncomeData): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val newIncome = income.copy(user_id = userId)

                supabase.from("income")
                    .insert(newIncome)

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Update an existing income entry
     */
    suspend fun updateIncome(incomeId: String, income: IncomeData): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                supabase.from("income")
                    .update({
                        set("source", income.source)
                        set("amount", income.amount)
                        set("income_date", income.income_date)
                        set("description", income.description)
                        set("image_url", income.image_url)
                        set("icon_name", income.icon_name)
                        set("color_hex", income.color_hex)
                        set("tags", income.tags)
                        set("is_recurring", income.is_recurring)
                        set("recurring_frequency", income.recurring_frequency)
                    }) {
                        filter {
                            eq("id", incomeId)
                            eq("user_id", userId)
                        }
                    }

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Delete an income entry
     */
    suspend fun deleteIncome(incomeId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                supabase.from("income")
                    .delete {
                        filter {
                            eq("id", incomeId)
                            eq("user_id", userId)
                        }
                    }

                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =====================================================
    // RECURRING INCOME OPERATIONS
    // =====================================================

    /**
     * Get all recurring income entries
     */
    suspend fun getRecurringIncome(): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val income = supabase.from("income")
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_recurring", true)
                        }
                        order("income_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<IncomeData>()

                Result.success(income)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =====================================================
    // ANALYTICS & SUMMARY OPERATIONS
    // =====================================================

    /**
     * Get total income for current month
     */
    suspend fun getTotalIncomeThisMonth(): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val currentDate = LocalDate.now()
                val startOfMonth = currentDate.withDayOfMonth(1).toString()
                val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

                getTotalIncomeForDateRange(startOfMonth, endOfMonth)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get total income for a specific date range
     */
    suspend fun getTotalIncomeForDateRange(
        startDate: String,
        endDate: String
    ): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val incomeList = getIncomeByDateRange(startDate, endDate).getOrNull()
                    ?: return@withContext Result.success(0.0)

                val total = incomeList.sumOf { it.amount }

                Result.success(total)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    /**
     * Get income grouped by source
     */
    suspend fun getIncomeBySource(
        startDate: String,
        endDate: String
    ): Result<Map<String, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                val incomeList = getIncomeByDateRange(startDate, endDate).getOrNull()
                    ?: return@withContext Result.success(emptyMap())

                val grouped = incomeList
                    .groupBy { it.source }
                    .mapValues { (_, incomes) -> incomes.sumOf { it.amount } }

                Result.success(grouped)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get income grouped by tags
     */
    suspend fun getIncomeByTags(
        startDate: String,
        endDate: String
    ): Result<Map<String, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                val incomeList = getIncomeByDateRange(startDate, endDate).getOrNull()
                    ?: return@withContext Result.success(emptyMap())

                val tagMap = mutableMapOf<String, Double>()

                incomeList.forEach { income ->
                    val tags = income.tags?.split(",")?.map { it.trim() } ?: listOf("Untagged")
                    tags.forEach { tag ->
                        if (tag.isNotBlank()) {
                            tagMap[tag] = tagMap.getOrDefault(tag, 0.0) + income.amount
                        }
                    }
                }

                Result.success(tagMap)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get average monthly income (based on last N months)
     */
    suspend fun getAverageMonthlyIncome(months: Int = 6): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val currentDate = LocalDate.now()
                val startDate = currentDate.minusMonths(months.toLong()).withDayOfMonth(1).toString()
                val endDate = currentDate.toString()

                val total = getTotalIncomeForDateRange(startDate, endDate).getOrNull() ?: 0.0
                val average = if (months > 0) total / months else 0.0

                Result.success(average)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =====================================================
    // SEARCH & FILTER OPERATIONS
    // =====================================================

    /**
     * Search income by source or description
     */
    suspend fun searchIncome(query: String): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val allIncome = getAllIncome().getOrNull() ?: return@withContext Result.success(emptyList())

                val filtered = allIncome.filter { income ->
                    income.source.contains(query, ignoreCase = true) ||
                            income.description?.contains(query, ignoreCase = true) == true ||
                            income.tags?.contains(query, ignoreCase = true) == true
                }

                Result.success(filtered)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Filter income by tags
     */
    suspend fun filterIncomeByTag(tag: String): Result<List<IncomeData>> {
        return withContext(Dispatchers.IO) {
            try {
                val allIncome = getAllIncome().getOrNull() ?: return@withContext Result.success(emptyList())

                val filtered = allIncome.filter { income ->
                    income.tags?.split(",")?.map { it.trim() }?.contains(tag) == true
                }

                Result.success(filtered)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}