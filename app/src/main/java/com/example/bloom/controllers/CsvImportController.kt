package com.example.bloom.controllers

import android.net.Uri
import android.util.Log
import com.example.bloom.SupabaseClient
import com.example.bloom.aifeatures.CsvImportResponse
import com.example.bloom.aifeatures.CsvImportTransaction
import com.example.bloom.datamodels.TransactionData
import com.example.bloom.retrofitapi.RetrofitInstance
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CsvImportController {

    private val api = RetrofitInstance.instance
    private val supabase = SupabaseClient.client

    /**
     * Upload and process a CSV file to extract transactions
     * @param fileUri URI of the CSV file to upload
     * @return Result containing the CSV import response
     */
    suspend fun uploadCsvFile(fileUri: Uri, file: File): Result<CsvImportResponse> {
        return try {
            Log.d("CsvImportController", "Uploading CSV file: ${file.name}")

            // Create multipart request body
            val requestFile = file.asRequestBody("text/csv".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Call the API
            val response = api.importCSV(filePart)
            Log.d("CsvImportController", "CSV import response: ${response.message}, valid rows: ${response.validRows}")

            Result.success(response)
        } catch (e: Exception) {
            Log.e("CsvImportController", "Failed to upload CSV file", e)
            Result.failure(e)
        }
    }

    /**
     * Save imported transactions to Supabase
     * @param transactions List of transactions from CSV import
     * @param userId User ID to associate with transactions
     * @return Result containing the number of successfully saved transactions
     */
    suspend fun saveTransactionsToDatabase(
        transactions: List<CsvImportTransaction>,
        userId: String,
        categoryId: String? = null
    ): Result<Int> {
        return try {
            Log.d("CsvImportController", "Saving ${transactions.size} transactions to database for user: $userId")

            var savedCount = 0
            val errors = mutableListOf<String>()

            for (transaction in transactions) {
                try {
                    // Parse amount to Double
                    val amount = transaction.amount.replace("$", "").replace(",", "").toDoubleOrNull()
                    if (amount == null) {
                        errors.add("Invalid amount for transaction: ${transaction.transactionName}")
                        continue
                    }

                    // Normalize transaction type to lowercase
                    val transactionType = transaction.transactionType.lowercase()
                    if (transactionType != "expense" && transactionType != "income") {
                        errors.add("Invalid transaction type for: ${transaction.transactionName}")
                        continue
                    }

                    // Parse and validate date (expected format: yyyy-MM-dd or similar)
                    val date = normalizeDate(transaction.date)
                    if (date.isEmpty()) {
                        errors.add("Invalid date for transaction: ${transaction.transactionName}")
                        continue
                    }

                    // Create transaction data
                    val transactionData = TransactionData(
                        user_id = userId,
                        category_id = categoryId,
                        transaction_name = transaction.transactionName,
                        amount = amount,
                        transaction_date = date,
                        transaction_type = transactionType,
                        description = transaction.description.ifBlank { null },
                        payment_method = transaction.paymentMethod.ifBlank { null },
                        is_recurring = false
                    )

                    // Insert into Supabase
                    supabase.from("transactions").insert(transactionData)
                    savedCount++
                    Log.d("CsvImportController", "Saved transaction: ${transaction.transactionName}")

                } catch (e: Exception) {
                    Log.e("CsvImportController", "Failed to save transaction: ${transaction.transactionName}", e)
                    errors.add("Failed to save ${transaction.transactionName}: ${e.message}")
                }
            }

            if (errors.isNotEmpty()) {
                Log.w("CsvImportController", "Saved $savedCount/${transactions.size} transactions. Errors: ${errors.joinToString("; ")}")
            } else {
                Log.d("CsvImportController", "Successfully saved all $savedCount transactions")
            }

            Result.success(savedCount)
        } catch (e: Exception) {
            Log.e("CsvImportController", "Failed to save transactions to database", e)
            Result.failure(e)
        }
    }

    /**
     * Normalize date string to yyyy-MM-dd format
     * Supports common date formats like MM/DD/YYYY, DD/MM/YYYY, YYYY-MM-DD
     */
    private fun normalizeDate(dateString: String): String {
        return try {
            // Remove any extra whitespace
            val trimmed = dateString.trim()

            // If already in yyyy-MM-dd format, return as is
            if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                return trimmed
            }

            // Try to parse MM/DD/YYYY or M/D/YYYY
            if (trimmed.contains("/")) {
                val parts = trimmed.split("/")
                if (parts.size == 3) {
                    val month = parts[0].padStart(2, '0')
                    val day = parts[1].padStart(2, '0')
                    val year = parts[2]

                    // Handle 2-digit year
                    val fullYear = if (year.length == 2) {
                        "20$year"
                    } else {
                        year
                    }

                    return "$fullYear-$month-$day"
                }
            }

            // If we can't parse it, return empty string
            ""
        } catch (e: Exception) {
            Log.e("CsvImportController", "Failed to normalize date: $dateString", e)
            ""
        }
    }
}