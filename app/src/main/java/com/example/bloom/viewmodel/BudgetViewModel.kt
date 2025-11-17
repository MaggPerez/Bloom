package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.BudgetController
import com.example.bloom.datamodels.CategoryWithBudget
import kotlinx.coroutines.launch

class BudgetViewModel {

    private val budgetController = BudgetController()


    //*****************************************************
    //state variables
    //*****************************************************


    //budget summary state
    var monthlyBudget by mutableStateOf(0.0)
    var savingsGoal by mutableStateOf(0.0)
    var currentSavings by mutableStateOf(0.0)
    var totalSpent by mutableStateOf(0.0)



    //categories state
    var categories by mutableStateOf<List<CategoryWithBudget>>(emptyList())


    //loading and error states
    var loading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)



    //dialog states
    var showEditBudgetDialog by mutableStateOf(false)
    var showAddCategoryDialog by mutableStateOf(false)
    var showEditCategoryDialog by mutableStateOf(false)
    var showDeleteCategoryDialog by mutableStateOf(false)



    //temporary state for editing budget
    var tempMonthlyBudget by mutableStateOf("")
    var tempSavingsGoal by mutableStateOf("")
    var tempCurrentSavings by mutableStateOf("")



    //temporary state for editing/adding category
    var selectedCategory by mutableStateOf<CategoryWithBudget?>(null)
    var tempCategoryName by mutableStateOf("")
    var tempCategoryColor by mutableStateOf("#4CAF50")
    var tempCategoryIcon by mutableStateOf("category")
    var tempCategoryBudget by mutableStateOf("")
    var tempCategoryType by mutableStateOf("expense")


    //computing properties
    val remaining: Double
        get() = monthlyBudget - totalSpent


    val remainingPercentage: Float
        get() = if (monthlyBudget > 0) ((remaining / monthlyBudget) * 100).toFloat().coerceIn(0f, 100f) else 0f


    val spentPercentage: Float
        get() = if (monthlyBudget > 0) ((totalSpent / monthlyBudget) * 100).toFloat() else 0f


    val savingsPercentage: Float
        get() = if (savingsGoal > 0) ((currentSavings / savingsGoal) * 100).toFloat().coerceIn(0f, 100f) else 0f


    val totalBudgetAllocated: Double
        get() = categories.sumOf { it.budgetAllocation }


    val unallocatedBudget: Double
        get() = monthlyBudget - totalBudgetAllocated



    //*****************************************************
    //initialization
    //*****************************************************

    init {
        loadBudgetData()
    }


    //*****************************************************
    //data loading functions
    //*****************************************************


    /**
     * function to load all budget data (summary + category)
     */
    fun loadBudgetData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                //load budget summary
                loadBudgetSummary()

                //load categories
                loadCategories()

                //load total spent
                loadTotalSpent()
            } catch (e: Exception) {
                errorMessage = "Failed to load budget data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }


    /**
     * load budget summary for current month
     */
    private suspend fun loadBudgetSummary() {
        budgetController.getCurrentMonthBudget().fold(
            onSuccess = { budget ->
                if(budget != null){
                    monthlyBudget = budget.monthly_budget
                    savingsGoal = budget.savings_goal
                    currentSavings = budget.current_savings
                }
                else{
                    //no budget exists, we defaults
                    monthlyBudget = 0.0
                    savingsGoal = 0.0
                    currentSavings = 0.0
                }
            },
            onFailure = {
                errorMessage = "Failed to load budget summary: ${it.message}"
            }

        )
    }


    /**
     * loading all categories for user
     */
    private suspend fun loadCategories() {
        budgetController.getUserCategories().fold(
            onSuccess = { categoryDataList ->
                //converting CategoryData to CategoryWithBudget with spent amounts
                val categoriesWithSpent = categoryDataList.map { categoryData ->
                    val spent = budgetController.getCategorySpent(categoryData.id ?: "").getOrDefault(0.0)

                    CategoryWithBudget(
                        id = categoryData.id ?: "",
                        name = categoryData.name,
                        colorHex = categoryData.color_hex,
                        iconName = categoryData.icon_name,
                        budgetAllocation = categoryData.budget_allocation,
                        spent = spent,
                        categoryType = categoryData.category_type
                    )
                }

                categories = categoriesWithSpent
            },
            onFailure = { e ->
                throw e
            }
        )
    }


    private suspend fun loadTotalSpent(){
        budgetController.getTotalSpentThisMonth().fold(
            onSuccess = { spent ->
                totalSpent = spent
            },
            onFailure = { e ->
                throw e
            }
        )
    }




    //*****************************************************
    //budget operations
    //*****************************************************






}