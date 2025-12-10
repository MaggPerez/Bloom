package com.example.bloom.aifeatures

import kotlinx.serialization.Serializable

/**
 * Data models for CSV import feature
 */

/**
 * Individual transaction from CSV import API response
 */
@Serializable
data class CsvImportTransaction(
    val transactionName: String,
    val amount: String,
    val transactionType: String,
    val date: String,
    val description: String,
    val paymentMethod: String
)

/**
 * Response from CSV import API endpoint
 */
@Serializable
data class CsvImportResponse(
    val success: Boolean,
    val message: String,
    val transactions: List<CsvImportTransaction>,
    val totalRows: Int,
    val validRows: Int,
    val skippedRows: Int
)