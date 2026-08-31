package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.screens.BusinessProfileScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.QuoteBuilderScreen
import com.example.ui.screens.QuoteDetailScreen
import com.example.ui.screens.QuotesListScreen
import com.example.ui.theme.MyApplicationTheme

object AppRoutes {
    const val QUOTES_LIST = "quotes_list"
    const val QUOTE_BUILDER = "quote_builder"
    const val QUOTE_DETAIL = "quote_detail/{quoteId}"
    const val CATALOG = "catalog"
    const val CONTACTS = "contacts"
    const val BUSINESS_PROFILE = "business_profile"

    fun quoteDetail(quoteId: Long): String = "quote_detail/$quoteId"
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.QUOTES_LIST
    ) {
        composable(AppRoutes.QUOTES_LIST) {
            QuotesListScreen(
                viewModel = viewModel,
                onNewQuoteClick = {
                    viewModel.prepareNewQuote()
                    navController.navigate(AppRoutes.QUOTE_BUILDER)
                },
                onQuoteClick = { quoteId ->
                    navController.navigate(AppRoutes.quoteDetail(quoteId))
                },
                onCatalogClick = {
                    navController.navigate(AppRoutes.CATALOG)
                },
                onContactsClick = {
                    navController.navigate(AppRoutes.CONTACTS)
                },
                onProfileClick = {
                    navController.navigate(AppRoutes.BUSINESS_PROFILE)
                }
            )
        }

        composable(AppRoutes.QUOTE_BUILDER) {
            QuoteBuilderScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate(AppRoutes.BUSINESS_PROFILE) },
                onQuoteSaved = { savedId ->
                    navController.popBackStack()
                    navController.navigate(AppRoutes.quoteDetail(savedId))
                }
            )
        }

        composable(
            route = AppRoutes.QUOTE_DETAIL,
            arguments = listOf(navArgument("quoteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getLong("quoteId") ?: 0L
            QuoteDetailScreen(
                quoteId = quoteId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditQuote = { id ->
                    viewModel.loadQuoteForEdit(id)
                    navController.navigate(AppRoutes.QUOTE_BUILDER)
                }
            )
        }

        composable(AppRoutes.CATALOG) {
            CatalogScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.CONTACTS) {
            ContactsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.BUSINESS_PROFILE) {
            BusinessProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
