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
        categoryId: String,
        amount: Double,
        transactionDate: String,
        transactionType: String,
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
        categoryId: String,
        amount: Double,
        transactionDate: String,
        transactionType: String,
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

            // Fetch transactions with category join
            val response = supabase.from("transactions")
                .select(
                    columns = Columns.raw(
                        """
                        id,
                        user_id,
                        category_id,
                        amount,
                        transaction_date,
                        transaction_type,
                        description,
                        payment_method,
                        tags,
                        receipt_url,
                        created_at,
                        categories!inner(id, name, color_hex, icon_name)
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

            val transactions = response.decodeList<Map<String, Any>>()

            // Parse and map transactions
            val transactionsWithCategory = transactions.mapNotNull { txn ->
                try {
                    val category = txn["categories"] as? Map<*, *>

                    // Handle tags - can be array or string depending on Supabase client
                    val tagsList = when (val tagsValue = txn["tags"]) {
                        is List<*> -> tagsValue.mapNotNull { it as? String }
                        is String -> tagsValue.split(",").filter { it.isNotBlank() }
                        else -> emptyList()
                    }

                    TransactionWithCategory(
                        id = txn["id"] as String,
                        userId = txn["user_id"] as String,
                        categoryId = txn["category_id"] as String,
                        categoryName = category?.get("name") as? String ?: "Unknown",
                        categoryColorHex = category?.get("color_hex") as? String ?: "#808080",
                        categoryIconName = category?.get("icon_name") as? String,
                        amount = (txn["amount"] as? Number)?.toDouble() ?: 0.0,
                        transactionDate = txn["transaction_date"] as String,
                        transactionType = txn["transaction_type"] as String,
                        description = txn["description"] as? String,
                        paymentMethod = (txn["payment_method"] as? String)?.let {
                            try { PaymentMethod.valueOf(it) } catch (_: Exception) { null }
                        },
                        tags = tagsList,
                        receiptUrl = txn["receipt_url"] as? String
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

            val count = response.decodeList<Map<String, Any>>().size
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
                        amount,
                        transaction_date,
                        transaction_type,
                        description,
                        payment_method,
                        tags,
                        categories!inner(name)
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

            val transactions = response.decodeList<Map<String, Any>>()

            // Sort by date (newest first)
            val sortedTransactions = transactions.sortedByDescending {
                it["transaction_date"] as? String ?: ""
            }

            // Create CSV file
            val fileName = "transactions_${LocalDate.now()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            FileWriter(file).use { writer ->
                // Write CSV header
                writer.append("Date,Category,Type,Amount,Payment Method,Description,Tags\n")

                // Write transaction rows
                sortedTransactions.forEach { txn ->
                    val category = txn["categories"] as? Map<*, *>
                    val categoryName = category?.get("name") as? String ?: "Unknown"

                    // Handle tags
                    val tagsStr = when (val tagsValue = txn["tags"]) {
                        is List<*> -> tagsValue.mapNotNull { it as? String }.joinToString("; ")
                        is String -> tagsValue
                        else -> ""
                    }

                    writer.append("\"${txn["transaction_date"]}\",")
                    writer.append("\"$categoryName\",")
                    writer.append("\"${txn["transaction_type"]}\",")
                    writer.append("${txn["amount"]},")
                    writer.append("\"${txn["payment_method"] ?: ""}\",")
                    writer.append("\"${txn["description"] ?: ""}\",")
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
}
