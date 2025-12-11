package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.IncomeController
import com.example.bloom.datamodels.IncomeData
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for Income screen
 * Manages income data state and user interactions
 */
class IncomeViewModel : ViewModel() {

    private val incomeController = IncomeController()

    // =====================================================
    // STATE VARIABLES
    // =====================================================

    // Income list
    var allIncome by mutableStateOf<List<IncomeData>>(emptyList())
        private set
    var filteredIncome by mutableStateOf<List<IncomeData>>(emptyList())
        private set
    var recurringIncome by mutableStateOf<List<IncomeData>>(emptyList())
        private set

    // Selected income for editing/viewing
    var selectedIncome by mutableStateOf<IncomeData?>(null)
        private set

    // Analytics data
    var totalIncomeThisMonth by mutableStateOf(0.0)
        private set
    var incomeBySource by mutableStateOf<Map<String, Double>>(emptyMap())
        private set
    var averageMonthlyIncome by mutableStateOf(0.0)
        private set

    // UI state
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var showAddIncomeDialog by mutableStateOf(false)
    var showEditIncomeDialog by mutableStateOf(false)
    var showDeleteConfirmation by mutableStateOf(false)

    // Search and filter
    var searchQuery by mutableStateOf("")
    var selectedFilter by mutableStateOf(IncomeFilter.ALL)
    var selectedDateRange by mutableStateOf(DateRangeFilter.ALL_TIME)

    init {
        loadAllIncome()
        loadIncomeStats()
    }

    // =====================================================
    // DATA LOADING FUNCTIONS
    // =====================================================

    /**
     * Load all income entries
     */
    fun loadAllIncome() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            incomeController.getAllIncome().fold(
                onSuccess = { income ->
                    allIncome = income
                    applyFilters()
                },
                onFailure = { e ->
                    errorMessage = "Failed to load income: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    /**
     * Load income for a specific date range
     */
    fun loadIncomeByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            incomeController.getIncomeByDateRange(startDate, endDate).fold(
                onSuccess = { income ->
                    filteredIncome = income
                },
                onFailure = { e ->
                    errorMessage = "Failed to load income: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    /**
     * Load current month income
     */
    fun loadCurrentMonthIncome() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            incomeController.getCurrentMonthIncome().fold(
                onSuccess = { income ->
                    filteredIncome = income
                },
                onFailure = { e ->
                    errorMessage = "Failed to load income: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    /**
     * Load recurring income
     */
    fun loadRecurringIncome() {
        viewModelScope.launch {
            incomeController.getRecurringIncome().fold(
                onSuccess = { income ->
                    recurringIncome = income
                },
                onFailure = { e ->
                    errorMessage = "Failed to load recurring income: ${e.message}"
                }
            )
        }
    }

    /**
     * Load income statistics
     */
    private fun loadIncomeStats() {
        viewModelScope.launch {
            // Load total for current month
            incomeController.getTotalIncomeThisMonth().fold(
                onSuccess = { total ->
                    totalIncomeThisMonth = total
                },
                onFailure = { e ->
                    e.printStackTrace()
                    totalIncomeThisMonth = 0.0
                }
            )

            // Load income by source for current month
            val currentDate = LocalDate.now()
            val startOfMonth = currentDate.withDayOfMonth(1).toString()
            val endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth()).toString()

            incomeController.getIncomeBySource(startOfMonth, endOfMonth).fold(
                onSuccess = { sourceMap ->
                    incomeBySource = sourceMap
                },
                onFailure = { e ->
                    e.printStackTrace()
                    incomeBySource = emptyMap()
                }
            )

            // Load average monthly income
            incomeController.getAverageMonthlyIncome().fold(
                onSuccess = { average ->
                    averageMonthlyIncome = average
                },
                onFailure = { e ->
                    e.printStackTrace()
                    averageMonthlyIncome = 0.0
                }
            )

            loadRecurringIncome()
        }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadAllIncome()
        loadIncomeStats()
    }

    // =====================================================
    // CRUD OPERATIONS
    // =====================================================

    /**
     * Create a new income entry
     */
    fun createIncome(income: IncomeData) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            incomeController.createIncome(income).fold(
                onSuccess = {
                    successMessage = "Income added successfully"
                    showAddIncomeDialog = false
                    refresh()
                },
                onFailure = { e ->
                    errorMessage = "Failed to add income: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    /**
     * Update an existing income entry
     */
    fun updateIncome(incomeId: String, income: IncomeData) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            incomeController.updateIncome(incomeId, income).fold(
                onSuccess = {
                    successMessage = "Income updated successfully"
                    showEditIncomeDialog = false
                    selectedIncome = null
                    refresh()
                },
                onFailure = { e ->
                    errorMessage = "Failed to update income: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    /**
     * Delete an income entry
     */
    fun deleteIncome(incomeId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            incomeController.deleteIncome(incomeId).fold(
                onSuccess = {
                    successMessage = "Income deleted successfully"
                    showDeleteConfirmation = false
                    selectedIncome = null
                    refresh()
                },
                onFailure = { e ->
                    errorMessage = "Failed to delete income: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    // =====================================================
    // SEARCH AND FILTER FUNCTIONS
    // =====================================================

    /**
     * Update search query and apply filters
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    /**
     * Update selected filter
     */
    fun updateFilter(filter: IncomeFilter) {
        selectedFilter = filter
        applyFilters()
    }

    /**
     * Update date range filter
     */
    fun updateDateRange(dateRange: DateRangeFilter) {
        selectedDateRange = dateRange
        applyFilters()
    }

    /**
     * Apply current filters to income list
     */
    private fun applyFilters() {
        var filtered = allIncome

        // Apply search query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { income ->
                income.source.contains(searchQuery, ignoreCase = true) ||
                        income.description?.contains(searchQuery, ignoreCase = true) == true ||
                        income.tags?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        // Apply type filter
        filtered = when (selectedFilter) {
            IncomeFilter.ALL -> filtered
            IncomeFilter.RECURRING -> filtered.filter { it.is_recurring }
            IncomeFilter.ONE_TIME -> filtered.filter { !it.is_recurring }
        }

        // Apply date range filter
        val today = LocalDate.now()
        filtered = when (selectedDateRange) {
            DateRangeFilter.ALL_TIME -> filtered
            DateRangeFilter.THIS_MONTH -> {
                val startOfMonth = today.withDayOfMonth(1).toString()
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).toString()
                filtered.filter { it.income_date in startOfMonth..endOfMonth }
            }
            DateRangeFilter.LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val startOfMonth = lastMonth.withDayOfMonth(1).toString()
                val endOfMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).toString()
                filtered.filter { it.income_date in startOfMonth..endOfMonth }
            }
            DateRangeFilter.LAST_3_MONTHS -> {
                val threeMonthsAgo = today.minusMonths(3).toString()
                filtered.filter { it.income_date >= threeMonthsAgo }
            }
            DateRangeFilter.THIS_YEAR -> {
                val startOfYear = today.withDayOfYear(1).toString()
                filtered.filter { it.income_date >= startOfYear }
            }
        }

        filteredIncome = filtered
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        searchQuery = ""
        selectedFilter = IncomeFilter.ALL
        selectedDateRange = DateRangeFilter.ALL_TIME
        applyFilters()
    }

    // =====================================================
    // UI DIALOG CONTROLS
    // =====================================================

    /**
     * Show add income dialog
     */
    fun showAddDialog() {
        selectedIncome = null
        showAddIncomeDialog = true
    }

    /**
     * Show edit income dialog
     */
    fun showEditDialog(income: IncomeData) {
        selectedIncome = income
        showEditIncomeDialog = true
    }

    /**
     * Show delete confirmation dialog
     */
    fun showDeleteDialog(income: IncomeData) {
        selectedIncome = income
        showDeleteConfirmation = true
    }

    /**
     * Hide all dialogs
     */
    fun hideDialogs() {
        showAddIncomeDialog = false
        showEditIncomeDialog = false
        showDeleteConfirmation = false
        selectedIncome = null
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    /**
     * Format currency for display
     */
    fun formatCurrency(amount: Double): String {
        return "$${String.format("%.2f", amount)}"
    }

    /**
     * Get total for filtered income
     */
    fun getFilteredTotal(): Double {
        return filteredIncome.sumOf { it.amount }
    }

    /**
     * Get income count
     */
    fun getIncomeCount(): Int {
        return filteredIncome.size
    }

    /**
     * Get top income sources (top 5)
     */
    fun getTopSources(): List<Pair<String, Double>> {
        return incomeBySource.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
    }

    /**
     * Get income for a specific source
     */
    fun getIncomeForSource(source: String): List<IncomeData> {
        return filteredIncome.filter { it.source == source }
    }

    /**
     * Calculate average income amount
     */
    fun getAverageIncomeAmount(): Double {
        return if (filteredIncome.isEmpty()) {
            0.0
        } else {
            filteredIncome.sumOf { it.amount } / filteredIncome.size
        }
    }
}

// =====================================================
// FILTER ENUMS
// =====================================================

enum class IncomeFilter {
    ALL,
    RECURRING,
    ONE_TIME;

    fun displayName(): String = when (this) {
        ALL -> "All Income"
        RECURRING -> "Recurring"
        ONE_TIME -> "One-time"
    }
}

enum class DateRangeFilter {
    ALL_TIME,
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    THIS_YEAR;

    fun displayName(): String = when (this) {
        ALL_TIME -> "All Time"
        THIS_MONTH -> "This Month"
        LAST_MONTH -> "Last Month"
        LAST_3_MONTHS -> "Last 3 Months"
        THIS_YEAR -> "This Year"
    }
}