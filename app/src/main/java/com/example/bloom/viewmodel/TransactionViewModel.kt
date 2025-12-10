package com.example.bloom.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.TransactionController
import com.example.bloom.datamodels.PaymentMethod
import com.example.bloom.datamodels.TransactionFilter
import com.example.bloom.datamodels.TransactionWithCategory
import kotlinx.coroutines.launch
import java.time.LocalDate

class TransactionViewModel : ViewModel() {
    private val transactionController = TransactionController()

    // =====================================================
    // STATE VARIABLES
    // =====================================================

    // Transaction list state
    var transactions by mutableStateOf<List<TransactionWithCategory>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Pagination state
    var currentPage by mutableStateOf(0)
        private set

    var hasMoreTransactions by mutableStateOf(true)
        private set

    var totalTransactionCount by mutableStateOf(0)
        private set

    private val pageSize = 50

    // Filter state
    var currentFilter by mutableStateOf(TransactionFilter())
        private set

    var isFilterActive by mutableStateOf(false)
        private set

    // Search state
    var searchQuery by mutableStateOf("")
        private set

    // Dialog states for Add/Edit/Delete
    var showAddTransactionDialog by mutableStateOf(false)
        private set

    var showEditTransactionDialog by mutableStateOf(false)
        private set

    var showDeleteConfirmDialog by mutableStateOf(false)
        private set

    var selectedTransaction by mutableStateOf<TransactionWithCategory?>(null)
        private set

    // Form state for Add/Edit dialogs
    var formTransactionName by mutableStateOf("")
    var formAmount by mutableStateOf("")
    var formTransactionDate by mutableStateOf(LocalDate.now().toString())
    var formTransactionType by mutableStateOf("expense")
    var formDescription by mutableStateOf("")
    var formPaymentMethod by mutableStateOf<PaymentMethod?>(null)
    var formTags by mutableStateOf("")
    var formReceiptUrl by mutableStateOf("")

    // CSV export state
    var isExporting by mutableStateOf(false)
        private set

    var exportMessage by mutableStateOf<String?>(null)
        private set

    // =====================================================
    // INITIALIZATION
    // =====================================================

    init {
        loadTransactions()
    }

    // =====================================================
    // TRANSACTION CRUD OPERATIONS
    // =====================================================

    /**
     * Load transactions with current filter and pagination
     */
    fun loadTransactions(resetPage: Boolean = false) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                if (resetPage) {
                    currentPage = 0
                    transactions = emptyList()
                }

                val filter = if (isFilterActive) currentFilter else null

                val result = transactionController.fetchTransactions(
                    page = currentPage,
                    pageSize = pageSize,
                    filter = filter
                )

                result.onSuccess { fetchedTransactions ->
                    transactions = if (resetPage) {
                        fetchedTransactions
                    } else {
                        transactions + fetchedTransactions
                    }

                    hasMoreTransactions = fetchedTransactions.size >= pageSize

                    // Get total count
                    transactionController.getTransactionCount(filter).onSuccess { count ->
                        totalTransactionCount = count
                    }
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load transactions"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Load more transactions (pagination)
     */
    fun loadMoreTransactions() {
        if (!isLoading && hasMoreTransactions) {
            currentPage++
            loadTransactions(resetPage = false)
        }
    }

    /**
     * Refresh transactions (reload from beginning)
     */
    fun refreshTransactions() {
        loadTransactions(resetPage = true)
    }

    /**
     * Add a new transaction
     */
    fun addTransaction() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                if (formTransactionName.isBlank()) {
                    errorMessage = "Please enter a transaction name"
                    return@launch
                }

                val amount = formAmount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    errorMessage = "Please enter a valid amount"
                    return@launch
                }

                val tagsList = formTags.split(",").map { it.trim() }.filter { it.isNotBlank() }

                val result = transactionController.createTransaction(
                    transactionName = formTransactionName,
                    amount = amount,
                    transactionDate = formTransactionDate,
                    transactionType = formTransactionType,
                    description = formDescription.ifBlank { null },
                    paymentMethod = formPaymentMethod,
                    tags = tagsList,
                    receiptUrl = formReceiptUrl.ifBlank { null }
                )

                result.onSuccess {
                    clearForm()
                    showAddTransactionDialog = false
                    refreshTransactions()
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to add transaction"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Update an existing transaction
     */
    fun updateTransaction() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val transactionId = selectedTransaction?.id
                if (transactionId == null) {
                    errorMessage = "No transaction selected"
                    return@launch
                }

                if (formTransactionName.isBlank()) {
                    errorMessage = "Please enter a transaction name"
                    return@launch
                }

                val amount = formAmount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    errorMessage = "Please enter a valid amount"
                    return@launch
                }

                val tagsList = formTags.split(",").map { it.trim() }.filter { it.isNotBlank() }

                val result = transactionController.updateTransaction(
                    transactionId = transactionId,
                    transactionName = formTransactionName,
                    amount = amount,
                    transactionDate = formTransactionDate,
                    transactionType = formTransactionType,
                    description = formDescription.ifBlank { null },
                    paymentMethod = formPaymentMethod,
                    tags = tagsList,
                    receiptUrl = formReceiptUrl.ifBlank { null }
                )

                result.onSuccess {
                    clearForm()
                    selectedTransaction = null
                    showEditTransactionDialog = false
                    refreshTransactions()
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to update transaction"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Delete a transaction
     */
    fun deleteTransaction() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val transactionId = selectedTransaction?.id
                if (transactionId == null) {
                    errorMessage = "No transaction selected"
                    return@launch
                }

                val result = transactionController.deleteTransaction(transactionId)

                result.onSuccess {
                    selectedTransaction = null
                    showDeleteConfirmDialog = false
                    refreshTransactions()
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to delete transaction"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    // =====================================================
    // FILTER AND SEARCH OPERATIONS
    // =====================================================

    /**
     * Apply filters to transactions
     */
    fun applyFilter(filter: TransactionFilter) {
        currentFilter = filter
        isFilterActive = filter.isActive()
        refreshTransactions()
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        currentFilter = TransactionFilter()
        isFilterActive = false
        refreshTransactions()
    }

    /**
     * Search transactions by description
     */
    fun searchTransactions(query: String) {
        searchQuery = query
        currentFilter = currentFilter.copy(searchQuery = query)
        isFilterActive = currentFilter.isActive()
        refreshTransactions()
    }

    /**
     * Filter by date range
     */
    fun filterByDateRange(startDate: String?, endDate: String?) {
        currentFilter = currentFilter.copy(
            startDate = startDate,
            endDate = endDate
        )
        isFilterActive = currentFilter.isActive()
        refreshTransactions()
    }

    /**
     * Filter by categories
     */
    fun filterByCategories(categoryIds: List<String>) {
        currentFilter = currentFilter.copy(categoryIds = categoryIds)
        isFilterActive = currentFilter.isActive()
        refreshTransactions()
    }

    /**
     * Filter by transaction type
     */
    fun filterByTransactionType(types: List<String>) {
        currentFilter = currentFilter.copy(transactionTypes = types)
        isFilterActive = currentFilter.isActive()
        refreshTransactions()
    }

    /**
     * Filter by amount range
     */
    fun filterByAmountRange(minAmount: Double?, maxAmount: Double?) {
        currentFilter = currentFilter.copy(
            minAmount = minAmount,
            maxAmount = maxAmount
        )
        isFilterActive = currentFilter.isActive()
        refreshTransactions()
    }

    // =====================================================
    // CSV EXPORT OPERATIONS
    // =====================================================

    /**
     * Export transactions to CSV and save to device
     */
    fun exportToCSVFile(context: Context) {
        viewModelScope.launch {
            try {
                isExporting = true
                exportMessage = null

                val filter = if (isFilterActive) currentFilter else null

                val result = transactionController.exportToCSV(context, filter)

                result.onSuccess { file ->
                    exportMessage = "Transactions exported to ${file.name}"
                }.onFailure { error ->
                    exportMessage = "Export failed: ${error.message}"
                }
            } catch (e: Exception) {
                exportMessage = "Export failed: ${e.message}"
            } finally {
                isExporting = false
            }
        }
    }

    /**
     * Export transactions to CSV and share via Android Share Sheet
     */
    fun exportAndShareCSV(context: Context) {
        viewModelScope.launch {
            try {
                isExporting = true
                exportMessage = null

                val filter = if (isFilterActive) currentFilter else null

                val fileResult = transactionController.exportToCSV(context, filter)

                fileResult.onSuccess { file ->
                    val shareResult = transactionController.shareCSV(context, file)

                    shareResult.onSuccess { intent ->
                        context.startActivity(intent)
                        exportMessage = "Share dialog opened"
                    }.onFailure { error ->
                        exportMessage = "Failed to share: ${error.message}"
                    }
                }.onFailure { error ->
                    exportMessage = "Export failed: ${error.message}"
                }
            } catch (e: Exception) {
                exportMessage = "Export failed: ${e.message}"
            } finally {
                isExporting = false
            }
        }
    }

    // =====================================================
    // DIALOG STATE MANAGEMENT
    // =====================================================

    /**
     * Show add transaction dialog
     */
    fun openAddTransactionDialog() {
        clearForm()
        showAddTransactionDialog = true
    }

    /**
     * Show edit transaction dialog
     */
    fun openEditTransactionDialog(transaction: TransactionWithCategory) {
        selectedTransaction = transaction
        populateFormFromTransaction(transaction)
        showEditTransactionDialog = true
    }

    /**
     * Show delete confirmation dialog
     */
    fun openDeleteConfirmDialog(transaction: TransactionWithCategory) {
        selectedTransaction = transaction
        showDeleteConfirmDialog = true
    }

    /**
     * Close all dialogs
     */
    fun closeDialogs() {
        showAddTransactionDialog = false
        showEditTransactionDialog = false
        showDeleteConfirmDialog = false
        selectedTransaction = null
        clearForm()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Clear export message
     */
    fun clearExportMessage() {
        exportMessage = null
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    /**
     * Clear form fields
     */
    private fun clearForm() {
        formTransactionName = ""
        formAmount = ""
        formTransactionDate = LocalDate.now().toString()
        formTransactionType = "expense"
        formDescription = ""
        formPaymentMethod = null
        formTags = ""
        formReceiptUrl = ""
    }

    /**
     * Populate form from transaction (for editing)
     */
    private fun populateFormFromTransaction(transaction: TransactionWithCategory) {
        formTransactionName = transaction.transactionName ?: ""
        formAmount = transaction.amount.toString()
        formTransactionDate = transaction.transactionDate
        formTransactionType = transaction.transactionType
        formDescription = transaction.description ?: ""
        formPaymentMethod = transaction.paymentMethod
        formTags = transaction.tags.joinToString(", ")
        formReceiptUrl = transaction.receiptUrl ?: ""
    }
}
