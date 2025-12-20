package com.example.financetracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.utils.Constants
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categoryRepository: CategoryRepository,
    authRepository: AuthRepository,
    navController: NavController
) {
    val viewModel: CategoriesViewModel = viewModel {
        CategoriesViewModel(categoryRepository, authRepository)
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Expense Categories
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expense Categories", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { viewModel.showAddDialog(TransactionType.EXPENSE) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Expense Category")
                        }
                    }
                }

                val expenseCategories = uiState.categories.filter { it.type == TransactionType.EXPENSE }
                items(expenseCategories) { category ->
                    CategoryItem(
                        category = category,
                        onDelete = { viewModel.deleteCategory(category.id.toString()) }
                    )
                }

                // Income Categories
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Income Categories", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { viewModel.showAddDialog(TransactionType.INCOME) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Income Category")
                        }
                    }
                }

                val incomeCategories = uiState.categories.filter { it.type == TransactionType.INCOME }
                items(incomeCategories) { category ->
                    CategoryItem(
                        category = category,
                        onDelete = { viewModel.deleteCategory(category.id.toString()) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddCategoryDialog(
            type = uiState.dialogType,
            name = uiState.dialogName,
            selectedColor = uiState.dialogColor,
            onNameChange = viewModel::onDialogNameChange,
            onColorChange = viewModel::onDialogColorChange,
            onConfirm = viewModel::addCategory,
            onDismiss = viewModel::hideAddDialog
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(category.color.toColorInt()),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(category.name, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    type: TransactionType,
    name: String,
    selectedColor: String,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${if (type == TransactionType.EXPENSE) "Expense" else "Income"} Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Color", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Constants.CATEGORY_COLORS) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(color.toColorInt()),
                                    shape = CircleShape
                                )
                                .then(
                                    if (color == selectedColor) {
                                        Modifier.padding(2.dp)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { onColorChange(color) }) {
                                if (color == selectedColor) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}