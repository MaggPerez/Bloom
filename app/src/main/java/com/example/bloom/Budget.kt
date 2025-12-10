package com.example.bloom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.CategoryWithBudget
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.BudgetViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    budgetViewModel: BudgetViewModel = viewModel()
){
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    //defining colors for metrics
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val blueColor = Color(0xFF3B82F6)
    val yellowColor = Color(0xFFFBBF24)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { budgetViewModel.openEditBudgetDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    )
    { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()){
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .windowInsetsPadding(WindowInsets.captionBar)
                        .padding(16.dp)
                ) {
                    //budget overview cards - 2x2 Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                title = "Monthly Budget",
                                value = "$${String.format(Locale.US, "%.0f", budgetViewModel.monthlyBudget)}",
                                subtitle = "Total allocated",
                                icon = Icons.Default.Settings,
                                backgroundColor = blueColor,
                                iconTint = blueColor,
                                modifier = Modifier.weight(1f)
                            )

                            MetricCard(
                                title = "Spent",
                                value = "$${String.format(Locale.US, "%.2f", budgetViewModel.totalSpent)}",
                                subtitle = "${String.format(Locale.US, "%.1f", budgetViewModel.spentPercentage)}% of budget",
                                icon = Icons.Default.ShoppingCart,
                                backgroundColor = redColor,
                                iconTint = redColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MetricCard(
                                title = "Remaining",
                                value = "$${String.format(Locale.US, "%.2f", budgetViewModel.remaining)}",
                                subtitle = "Can still spend",
                                icon = Icons.Default.CheckCircle,
                                backgroundColor = greenColor,
                                iconTint = greenColor,
                                modifier = Modifier.weight(1f),
                                progressPercentage = budgetViewModel.remainingPercentage
                            )

                            MetricCard(
                                title = "Savings Goal",
                                value = "$${String.format(Locale.US, "%.0f", budgetViewModel.currentSavings)}",
                                subtitle = "of $${String.format(Locale.US, "%.0f", budgetViewModel.savingsGoal)}",
                                icon = Icons.Default.Star,
                                backgroundColor = yellowColor,
                                iconTint = yellowColor,
                                modifier = Modifier.weight(1f),
                                progressPercentage = budgetViewModel.savingsPercentage
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }


                    //budget allocation summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Budget Allocation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Allocated",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", budgetViewModel.totalBudgetAllocated)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Unallocated",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", budgetViewModel.unallocatedBudget)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (budgetViewModel.unallocatedBudget < 0) redColor else greenColor
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }


                    //category budget section header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category Budgets",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            IconButton(onClick = { budgetViewModel.openAddCategoryDialog() }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Category",
                                    tint = blueColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }


                    //category list
                    if(budgetViewModel.categories.isEmpty()){
                        item {
                            EmptyStateCard(
                                message = "No categories yet. Add your first category to start budgeting!",
                                onAddClick = { budgetViewModel.openAddCategoryDialog() }
                            )
                        }
                    } else {
                        items(budgetViewModel.categories) { category ->
                            CategoryBudgetCard(
                                category = category,
                                onEdit = { budgetViewModel.openEditCategoryDialog(category) },
                                onDelete = { budgetViewModel.openDeleteCategoryDialog(category) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                //loading overlay
                if(budgetViewModel.isLoading){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = blueColor)
                    }
                }
            }
        }

        //edit budget dialog
        if(budgetViewModel.showEditBudgetDialog){
            EditBudgetDialog(
                monthlyBudget = budgetViewModel.tempMonthlyBudget,
                savingsGoal = budgetViewModel.tempSavingsGoal,
                currentSavings = budgetViewModel.tempCurrentSavings,
                onMonthlyBudgetChange = { budgetViewModel.tempMonthlyBudget = it },
                onSavingsGoalChange = { budgetViewModel.tempSavingsGoal = it },
                onCurrentSavingsChange = { budgetViewModel.tempCurrentSavings = it },
                onDismiss = { budgetViewModel.showEditBudgetDialog = false },
                onSave = { budgetViewModel.saveBudget() }
            )
        }

        //add category dialog
        if (budgetViewModel.showAddCategoryDialog) {
            AddEditCategoryDialog(
                title = "Add Category",
                name = budgetViewModel.tempCategoryName,
                colorHex = budgetViewModel.tempCategoryColor,
                budgetAllocation = budgetViewModel.tempCategoryBudget,
                onNameChange = { budgetViewModel.tempCategoryName = it },
                onColorChange = { budgetViewModel.tempCategoryColor = it },
                onBudgetChange = { budgetViewModel.tempCategoryBudget = it },
                onDismiss = { budgetViewModel.showAddCategoryDialog = false },
                onSave = { budgetViewModel.addCategory() }
            )
        }


        //edit category dialog
        if (budgetViewModel.showEditCategoryDialog) {
            AddEditCategoryDialog(
                title = "Edit Category",
                name = budgetViewModel.tempCategoryName,
                colorHex = budgetViewModel.tempCategoryColor,
                budgetAllocation = budgetViewModel.tempCategoryBudget,
                onNameChange = { budgetViewModel.tempCategoryName = it },
                onColorChange = { budgetViewModel.tempCategoryColor = it },
                onBudgetChange = { budgetViewModel.tempCategoryBudget = it },
                onDismiss = { budgetViewModel.showEditCategoryDialog = false },
                onSave = { budgetViewModel.updateCategory() }
            )
        }


        //delete category confirmation dialog
        if (budgetViewModel.showDeleteCategoryDialog) {
            DeleteCategoryDialog(
                categoryName = budgetViewModel.selectedCategory?.name ?: "",
                onDismiss = { budgetViewModel.showDeleteCategoryDialog = false },
                onConfirm = { budgetViewModel.deleteCategory() }
            )
        }


        //error dialog
        budgetViewModel.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { budgetViewModel.clearError() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { budgetViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}



@Composable
fun CategoryBudgetCard(
    category: CategoryWithBudget,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(12.dp)
                            .clip(CircleShape)
                            .background(category.color)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row{
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Column{
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", category.budgetAllocation)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", category.spent)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (category.spent > category.budgetAllocation) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                    )
                }


                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", category.remaining)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (category.remaining < 0) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            LinearProgressBar(
                percentage = category.spentPercentage,
                color = category.color
            )
        }


    }
}




@Composable
fun EmptyStateCard(
    message: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Category")
            }
        }
    }
}



@Composable
fun EditBudgetDialog(
    monthlyBudget: String,
    savingsGoal: String,
    currentSavings: String,
    onMonthlyBudgetChange: (String) -> Unit,
    onSavingsGoalChange: (String) -> Unit,
    onCurrentSavingsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Budget") },
        text = {
            Column {
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = onMonthlyBudgetChange,
                    label = { Text("Monthly Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = savingsGoal,
                    onValueChange = onSavingsGoalChange,
                    label = { Text("Savings Goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = currentSavings,
                    onValueChange = onCurrentSavingsChange,
                    label = { Text("Current Savings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun AddEditCategoryDialog(
    title: String,
    name: String,
    colorHex: String,
    budgetAllocation: String,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = budgetAllocation,
                    onValueChange = onBudgetChange,
                    label = { Text("Budget Allocation") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Color Picker (simple preset colors)
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                ColorPicker(
                    selectedColor = colorHex,
                    onColorSelected = onColorChange
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}




@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        "#4CAF50", // Green
        "#2196F3", // Blue
        "#FFC107", // Yellow
        "#FF5722", // Orange
        "#9C27B0", // Purple
        "#E91E63", // Pink
        "#00BCD4", // Cyan
        "#FF9800", // Orange
        "#8BC34A", // Light Green
        "#3F51B5", // Indigo
        "#F44336", // Red
        "#607D8B"  // Blue Grey
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.take(6).forEach { colorHex ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .clickable { onColorSelected(colorHex) }
                    .then(
                        if (selectedColor == colorHex) {
                            Modifier.then(
                                Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == colorHex) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.drop(6).forEach { colorHex ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == colorHex) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}




@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category") },
        text = { Text("Are you sure you want to delete the category \"$categoryName\"? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Preview (showBackground = true)
@Composable
fun BudgetScreenPreview() {
    BloomTheme {
        BudgetScreen(navController = rememberNavController())
    }
}