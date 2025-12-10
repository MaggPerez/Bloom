package com.example.bloom.controllers

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.bloom.SupabaseClient
import com.example.bloom.datamodels.PaymentMethod
import com.example.bloom.datamodels.TransactionData
import com.example.bloom.datamodels.TransactionFilter
import com.example.bloom.datamodels.TransactionWithCategory
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileWriter
import java.time.LocalDate

class TransactionController {
    private val supabase = SupabaseClient.client

    // Get current user ID
    private fun getUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Create a new transaction
     */
    suspend fun createTransaction(
        transactionName: String,
        amount: Double,
        transactionDate: String,
        transactionType: String,
        categoryId: String? = null,
        description: String? = null,
        paymentMethod: PaymentMethod? = null,
        tags: List<String> = emptyList(),
        receiptUrl: String? = null
    ): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            val newTransaction = TransactionData(
                user_id = userId,
                category_id = categoryId,
                transaction_name = transactionName,
                amount = amount,
                transaction_date = transactionDate,
                transaction_type = transactionType,
                description = description,
                payment_method = paymentMethod?.name,
                tags = if (tags.isNotEmpty()) tags.joinToString(",") else null,
                receipt_url = receiptUrl
            )

            supabase.from("transactions")
                .insert(newTransaction)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing transaction
     */
    suspend fun updateTransaction(
        transactionId: String,
        transactionName: String,
        amount: Double,
        transactionDate: String,
        transactionType: String,
        categoryId: String? = null,
        description: String? = null,
        paymentMethod: PaymentMethod? = null,
        tags: List<String> = emptyList(),
        receiptUrl: String? = null
    ): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("transactions")
                .update({
                    set("category_id", categoryId)
                    set("transaction_name", transactionName)
                    set("amount", amount)
                    set("transaction_date", transactionDate)
                    set("transaction_type", transactionType)
                    set("description", description)
                    set("payment_method", paymentMethod?.name)
                    set("tags", if (tags.isNotEmpty()) tags.joinToString(",") else null)
                    set("receipt_url", receiptUrl)
                }) {
                    filter {
                        eq("id", transactionId)
                        eq("user_id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a transaction
     */
    suspend fun deleteTransaction(transactionId: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("transactions")
                .delete {
                    filter {
                        eq("id", transactionId)
                        eq("user_id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =====================================================
    // FETCH OPERATIONS WITH PAGINATION
    // =====================================================

    /**
     * Fetch transactions with pagination and optional filtering
     */
    suspend fun fetchTransactions(
        page: Int = 0,
        pageSize: Int = 50,
        filter: TransactionFilter? = null
    ): Result<List<TransactionWithCategory>> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            // Fetch transactions with optional category join
            val response = supabase.from("transactions")
                .select(
                    columns = Columns.raw(
                        """
                        id,
                        user_id,
                        category_id,
                        transaction_name,
                        amount,
                        transaction_date,
                        transaction_type,
                        description,
                        payment_method,
                        tags,
                        receipt_url,
                        created_at,
                        categories!left(id, name, color_hex, icon_name)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("user_id", userId)

                        // Apply filters
                        filter?.let { f ->
                            f.startDate?.let { gte("transaction_date", it) }
                            f.endDate?.let { lte("transaction_date", it) }
                            if (f.categoryIds.isNotEmpty()) {
                                isIn("category_id", f.categoryIds)
                            }
                            if (f.transactionTypes.isNotEmpty()) {
                                isIn("transaction_type", f.transactionTypes)
                            }
                            f.minAmount?.let { gte("amount", it) }
                            f.maxAmount?.let { lte("amount", it) }
                            f.searchQuery?.takeIf { it.isNotBlank() }?.let {
                                ilike("description", "%$it%")
                            }
                        }
                    }
                }

            val transactions = response.decodeList<JsonObject>()

            // Parse and map transactions
            val transactionsWithCategory = transactions.mapNotNull { txn ->
                try {
                    val category = txn["categories"]?.jsonObject

                    // Handle tags - can be array or string depending on Supabase client
                    val tagsList = when (val tagsValue = txn["tags"]) {
                        null -> emptyList()
                        else -> {
                            try {
                                tagsValue.jsonArray.map { it.jsonPrimitive.content }
                            } catch (e: Exception) {
                                // If it's a string, split by comma
                                tagsValue.jsonPrimitive.content.split(",").filter { it.isNotBlank() }
                            }
                        }
                    }

                    TransactionWithCategory(
                        id = txn["id"]?.jsonPrimitive?.content ?: "",
                        userId = txn["user_id"]?.jsonPrimitive?.content ?: "",
                        categoryId = txn["category_id"]?.jsonPrimitive?.content,
                        transactionName = txn["transaction_name"]?.jsonPrimitive?.content,
                        categoryName = category?.get("name")?.jsonPrimitive?.content,
                        categoryColorHex = category?.get("color_hex")?.jsonPrimitive?.content,
                        categoryIconName = category?.get("icon_name")?.jsonPrimitive?.content,
                        amount = txn["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                        transactionDate = txn["transaction_date"]?.jsonPrimitive?.content ?: "",
                        transactionType = txn["transaction_type"]?.jsonPrimitive?.content ?: "expense",
                        description = txn["description"]?.jsonPrimitive?.content,
                        paymentMethod = txn["payment_method"]?.jsonPrimitive?.content?.let {
                            try { PaymentMethod.valueOf(it) } catch (_: Exception) { null }
                        },
                        tags = tagsList,
                        receiptUrl = txn["receipt_url"]?.jsonPrimitive?.content
                    )
                } catch (_: Exception) {
                    null
                }
            }

            // Sort by transaction date (newest first), then by created_at
            val sortedTransactions = transactionsWithCategory.sortedWith(
                compareByDescending<TransactionWithCategory> { it.transactionDate }
                    .thenByDescending { it.id }
            )

            // Apply pagination in-memory
            val startIndex = page * pageSize
            val endIndex = (startIndex + pageSize).coerceAtMost(sortedTransactions.size)
            val paginatedTransactions = if (startIndex < sortedTransactions.size) {
                sortedTransactions.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            Result.success(paginatedTransactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get transaction count for pagination info
     */
    suspend fun getTransactionCount(filter: TransactionFilter? = null): Result<Int> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            val response = supabase.from("transactions")
                .select(columns = Columns.raw("id")) {
                    filter {
                        eq("user_id", userId)

                        // Apply same filters as fetchTransactions
                        filter?.let { f ->
                            f.startDate?.let { gte("transaction_date", it) }
                            f.endDate?.let { lte("transaction_date", it) }
                            if (f.categoryIds.isNotEmpty()) {
                                isIn("category_id", f.categoryIds)
                            }
                            if (f.transactionTypes.isNotEmpty()) {
                                isIn("transaction_type", f.transactionTypes)
                            }
                            f.minAmount?.let { gte("amount", it) }
                            f.maxAmount?.let { lte("amount", it) }
                            f.searchQuery?.takeIf { it.isNotBlank() }?.let {
                                ilike("description", "%$it%")
                            }
                        }
                    }
                }

            val count = response.decodeList<JsonObject>().size
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =====================================================
    // CSV EXPORT
    // =====================================================

    /**
     * Export transactions to CSV file and save to device storage
     */
    suspend fun exportToCSV(
        context: Context,
        filter: TransactionFilter? = null
    ): Result<File> {
        return try {
            // Fetch all transactions matching filter (no pagination limit)
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            val response = supabase.from("transactions")
                .select(
                    columns = Columns.raw(
                        """
                        id,
                        transaction_name,
                        amount,
                        transaction_date,
                        transaction_type,
                        description,
                        payment_method,
                        tags,
                        categories!left(name)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("user_id", userId)

                        // Apply filters
                        filter?.let { f ->
                            f.startDate?.let { gte("transaction_date", it) }
                            f.endDate?.let { lte("transaction_date", it) }
                            if (f.categoryIds.isNotEmpty()) {
                                isIn("category_id", f.categoryIds)
                            }
                            if (f.transactionTypes.isNotEmpty()) {
                                isIn("transaction_type", f.transactionTypes)
                            }
                            f.minAmount?.let { gte("amount", it) }
                            f.maxAmount?.let { lte("amount", it) }
                        }
                    }
                }

            val transactions = response.decodeList<JsonObject>()

            // Sort by date (newest first)
            val sortedTransactions = transactions.sortedByDescending {
                it["transaction_date"]?.jsonPrimitive?.content ?: ""
            }

            // Create CSV file
            val fileName = "transactions_${LocalDate.now()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            FileWriter(file).use { writer ->
                // Write CSV header
                writer.append("Date,Transaction Name,Category,Type,Amount,Payment Method,Description,Tags\n")

                // Write transaction rows
                sortedTransactions.forEach { txn ->
                    val transactionName = txn["transaction_name"]?.jsonPrimitive?.content ?: ""
                    val category = txn["categories"]?.jsonObject
                    val categoryName = category?.get("name")?.jsonPrimitive?.content ?: ""

                    // Handle tags
                    val tagsStr = when (val tagsValue = txn["tags"]) {
                        null -> ""
                        else -> {
                            try {
                                tagsValue.jsonArray.joinToString("; ") { it.jsonPrimitive.content }
                            } catch (e: Exception) {
                                tagsValue.jsonPrimitive.content
                            }
                        }
                    }

                    writer.append("\"${txn["transaction_date"]?.jsonPrimitive?.content ?: ""}\",")
                    writer.append("\"$transactionName\",")
                    writer.append("\"$categoryName\",")
                    writer.append("\"${txn["transaction_type"]?.jsonPrimitive?.content ?: ""}\",")
                    writer.append("${txn["amount"]?.jsonPrimitive?.content ?: "0"},")
                    writer.append("\"${txn["payment_method"]?.jsonPrimitive?.content ?: ""}\",")
                    writer.append("\"${txn["description"]?.jsonPrimitive?.content ?: ""}\",")
                    writer.append("\"$tagsStr\"\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share CSV file via Android Share Sheet
     */
    fun shareCSV(context: Context, file: File): Result<Intent> {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Bloom Transactions Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Transactions")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Result.success(chooserIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =====================================================
    // FINANCIAL ANALYTICS
    // =====================================================

    /**
     * Get total income for current month
     */
    suspend fun getMonthlyIncome(): Result<Double> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentDate = LocalDate.now()
            val month = currentDate.monthValue
            val year = currentDate.year

            // Get first and last day of current month
            val firstDay = LocalDate.of(year, month, 1).toString()
            val lastDay = LocalDate.of(year, month, currentDate.lengthOfMonth()).toString()

            val response = supabase.from("transactions")
                .select(columns = Columns.raw("amount")) {
                    filter {
                        eq("user_id", userId)
                        eq("transaction_type", "income")
                        gte("transaction_date", firstDay)
                        lte("transaction_date", lastDay)
                    }
                }

            val transactions = response.decodeList<JsonObject>()
            val totalIncome = transactions.sumOf {
                it["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            }

            Result.success(totalIncome)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get average monthly expenses over last 3 months
     */
    suspend fun getAverageMonthlyExpenses(): Result<Double> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            val currentDate = LocalDate.now()

            // Get date from 3 months ago
            val threeMonthsAgo = currentDate.minusMonths(3).toString()

            val response = supabase.from("transactions")
                .select(columns = Columns.raw("amount, transaction_date")) {
                    filter {
                        eq("user_id", userId)
                        eq("transaction_type", "expense")
                        gte("transaction_date", threeMonthsAgo)
                    }
                }

            val transactions = response.decodeList<JsonObject>()

            if (transactions.isEmpty()) {
                return Result.success(0.0)
            }

            val totalExpenses = transactions.sumOf {
                it["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
            }

            // Calculate average over 3 months
            val averageMonthly = totalExpenses / 3.0

            Result.success(averageMonthly)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
