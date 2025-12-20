package com.example.financetracker.ui.home

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.financetracker.data.entities.TransactionEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.navigation.Screen
import com.example.financetracker.ui.theme.ExpenseRed
import com.example.financetracker.ui.theme.IncomeGreen
import com.example.financetracker.utils.CurrencyUtils
import com.example.financetracker.utils.DateUtils
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    authRepository: AuthRepository,
    navController: NavController
) {
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(transactionRepository, categoryRepository, authRepository)
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Tracker") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddTransaction.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                // Balance Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Current Balance", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = CurrencyUtils.formatCurrency(uiState.balance),
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Income", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        CurrencyUtils.formatCurrency(uiState.totalIncome),
                                        color = androidx.compose.ui.graphics.Color(IncomeGreen.value)
                                    )
                                }
                                Column {
                                    Text("Expenses", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        CurrencyUtils.formatCurrency(uiState.totalExpense),
                                        color = androidx.compose.ui.graphics.Color(ExpenseRed.value)
                                    )
                                }
                            }
                        }
                    }
                }

                // Pie Chart
                if (uiState.categorySpending.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        PieChartView(categorySpending = uiState.categorySpending)
                    }
                }

                // Recent Transactions
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { navController.navigate(Screen.TransactionsList.route) }) {
                            Text("View All")
                        }
                    }
                }

                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        Text(
                            "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(uiState.recentTransactions) { transaction ->
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
}

@Composable
fun PieChartView(categorySpending: List<CategorySpending>) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.WHITE)
                setTransparentCircleColor(Color.WHITE)
                setTransparentCircleAlpha(110)
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                rotationAngle = 0f
                isRotationEnabled = true
                isHighlightPerTapEnabled = true
                legend.isEnabled = true
            }
        },
        update = { chart ->
            val entries = categorySpending.map {
                PieEntry(it.amount.toFloat(), it.categoryName)
            }

            // Define a color palette
            val colors = listOf(
                Color.rgb(64, 89, 128),
                Color.rgb(149, 165, 124),
                Color.rgb(217, 184, 162),
                Color.rgb(191, 134, 134),
                Color.rgb(179, 48, 80),
                Color.rgb(193, 37, 82),
                Color.rgb(255, 102, 0),
                Color.rgb(245, 199, 0),
                Color.rgb(106, 150, 31),
                Color.rgb(179, 100, 53)
            )

            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors)
                sliceSpace = 3f
                selectionShift = 5f
                valueTextSize = 12f
            }

            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    categoryName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(categoryName, style = MaterialTheme.typography.bodyLarge)
                if (transaction.notes.isNotEmpty()) {
                    Text(
                        transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    DateUtils.formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyUtils.formatCurrency(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.type == TransactionType.INCOME) {
                    androidx.compose.ui.graphics.Color(IncomeGreen.value)
                } else {
                    androidx.compose.ui.graphics.Color(ExpenseRed.value)
                }
            )
        }
    }
}