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
     * Simple diagnostic function to test if basic transaction fetching works
     * Returns count of transactions in database for current user
     */
    suspend fun testTransactionFetch(): Result<String> {
        return try {
            val userId = getUserId()
            if (userId == null) {
                return Result.success("ERROR: User not authenticated - getUserId() returned null")
            }

            // Test simple query
            val response = supabase.from("transactions")
                .select(columns = Columns.raw("id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            val transactions = response.decodeList<JsonObject>()
            Result.success("SUCCESS: Found ${transactions.size} transactions for user $userId")
        } catch (e: Exception) {
            Result.success("ERROR: ${e.message}\n${e.stackTraceToString()}")
        }
    }

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
                tags = if (tags.isNotEmpty()) tags else null, // Send as List<String> array
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

            // PostgreSQL expects an array for tags column, not null or comma-separated string
            // Send empty array [] instead of null to avoid "malformed array literal" error
            supabase.from("transactions")
                .update({
                    set("category_id", categoryId)
                    set("transaction_name", transactionName)
                    set("amount", amount)
                    set("transaction_date", transactionDate)
                    set("transaction_type", transactionType)
                    set("description", description)
                    set("payment_method", paymentMethod?.name)
                    set("tags", tags)  // Send as List<String>, not comma-separated
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
            val userId = getUserId()
            if (userId == null) {
                println("TransactionController: User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            println("TransactionController: Fetching transactions for user: $userId, page: $page")

            // First, fetch all categories for the user to have them available for lookup
            val categoriesMap = try {
                val categoriesResponse = supabase.from("categories")
                    .select(columns = Columns.raw("id, name, color_hex, icon_name")) {
                        filter {
                            eq("user_id", userId)
                        }
                    }

                val categories = categoriesResponse.decodeList<JsonObject>()
                println("TransactionController: Fetched ${categories.size} categories")
                categories.associate { cat ->
                    val id = cat["id"]?.jsonPrimitive?.content ?: ""
                    id to cat
                }
            } catch (e: Exception) {
                println("TransactionController: Error fetching categories: ${e.message}")
                e.printStackTrace()
                emptyMap() // If categories fetch fails, continue without them
            }

            // Fetch transactions without join to avoid foreign key issues
            val response = supabase.from("transactions")
                .select(columns = Columns.raw("*")) {
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
                                // Search in both transaction_name and description
                                or {
                                    ilike("transaction_name", "%$it%")
                                    ilike("description", "%$it%")
                                }
                            }
                        }
                    }
                    // Order by transaction_date DESC, then by created_at DESC
                    order(column = "transaction_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)

                    // Apply pagination at database level
                    range(from = (page * pageSize).toLong(), to = ((page + 1) * pageSize - 1).toLong())
                }

            val transactions = response.decodeList<JsonObject>()
            println("TransactionController: Fetched ${transactions.size} raw transactions from Supabase")

            // Parse and map transactions, manually looking up category info
            val transactionsWithCategory = transactions.mapNotNull { txn ->
                try {
                    val categoryId = txn["category_id"]?.jsonPrimitive?.content
                    val category = categoryId?.let { categoriesMap[it] }

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
                        categoryId = categoryId,
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
                } catch (e: Exception) {
                    // Log individual transaction parsing errors but don't fail the whole operation
                    e.printStackTrace()
                    null
                }
            }

            println("TransactionController: Returning ${transactionsWithCategory.size} parsed transactions")
            Result.success(transactionsWithCategory)
        } catch (e: Exception) {
            // Log the actual error for debugging
            e.printStackTrace()
            Result.failure(Exception("Failed to fetch transactions: ${e.message}", e))
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
