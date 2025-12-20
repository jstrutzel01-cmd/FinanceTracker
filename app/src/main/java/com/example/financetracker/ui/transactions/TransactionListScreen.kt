package com.example.financetracker.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.navigation.Screen
import com.example.financetracker.ui.home.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    authRepository: AuthRepository,
    navController: NavController
) {
    val viewModel: TransactionsListViewModel = viewModel {
        TransactionsListViewModel(transactionRepository, categoryRepository, authRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddTransaction.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                if (uiState.filteredTransactions.isEmpty()) {
                    item {
                        Text(
                            "No transactions found",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(uiState.filteredTransactions) { transaction ->
                        val category = uiState.categories.find { it.id == transaction.categoryId }
                        TransactionItem(
                            transaction = transaction,
                            categoryName = category?.name ?: "Unknown",
                            onClick = { navController.navigate(Screen.EditTransaction.createRoute(transaction.id.toString())) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilterType = uiState.filterType,
            currentFilterCategory = uiState.filterCategory,
            currentSortAscending = uiState.sortAscending,
            categories = uiState.categories,
            onFilterTypeChange = { viewModel.setFilterType(it) },
            onFilterCategoryChange = { viewModel.setFilterCategory(it) },
            onSortOrderChange = { viewModel.setSortOrder(it) },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun FilterDialog(
    currentFilterType: TransactionType?,
    currentFilterCategory: String?,
    currentSortAscending: Boolean,
    categories: List<CategoryEntity>,
    onFilterTypeChange: (TransactionType?) -> Unit,
    onFilterCategoryChange: (String?) -> Unit,
    onSortOrderChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Sort") },
        text = {
            Column {
                Text("Transaction Type", style = MaterialTheme.typography.labelLarge)
                Row {
                    FilterChip(
                        selected = currentFilterType == null,
                        onClick = { onFilterTypeChange(null) },
                        label = { Text("All") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = currentFilterType == TransactionType.INCOME,
                        onClick = { onFilterTypeChange(TransactionType.INCOME) },
                        label = { Text("Income") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = currentFilterType == TransactionType.EXPENSE,
                        onClick = { onFilterTypeChange(TransactionType.EXPENSE) },
                        label = { Text("Expense") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Sort Order", style = MaterialTheme.typography.labelLarge)
                Row {
                    FilterChip(
                        selected = !currentSortAscending,
                        onClick = { onSortOrderChange(false) },
                        label = { Text("Newest First") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = currentSortAscending,
                        onClick = { onSortOrderChange(true) },
                        label = { Text("Oldest First") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}