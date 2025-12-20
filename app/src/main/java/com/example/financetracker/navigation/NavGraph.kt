package com.example.financetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.ui.auth.LoginScreen
import com.example.financetracker.ui.auth.SignUpScreen
import com.example.financetracker.ui.categories.CategoriesScreen
import com.example.financetracker.ui.home.HomeScreen
import com.example.financetracker.ui.profile.ProfileScreen
import com.example.financetracker.ui.transactions.AddTransactionScreen
import com.example.financetracker.ui.transactions.EditTransactionScreen
import com.example.financetracker.ui.transactions.TransactionsListScreen

@Composable
fun AppNavigation(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    authRepository: AuthRepository
) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isUserLoggedIn) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                authRepository = authRepository,
                categoryRepository,
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Main screens
        composable(Screen.Home.route) {
            HomeScreen(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                authRepository = authRepository,
                navController = navController
            )
        }

        composable(Screen.TransactionsList.route) {
            TransactionsListScreen(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                authRepository = authRepository,
                navController = navController
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                transactionRepository = transactionRepository,
                categoryRepository = categoryRepository,
                authRepository = authRepository,
                navController = navController
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            if (transactionId != null) {
                EditTransactionScreen(
                    transactionId = transactionId,
                    transactionRepository = transactionRepository,
                    categoryRepository = categoryRepository,
                    authRepository = authRepository,
                    navController = navController
                )
            }
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                categoryRepository = categoryRepository,
                authRepository = authRepository,
                navController = navController
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authRepository = authRepository,
                navController = navController
            )
        }
    }
}