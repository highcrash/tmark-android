package com.tmark.client.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tmark.client.data.local.TokenStore
import com.tmark.client.ui.screens.auth.LoginScreen
import com.tmark.client.ui.screens.catalog.CatalogScreen
import com.tmark.client.ui.screens.catalog.PackageDetailScreen
import com.tmark.client.ui.screens.dashboard.DashboardScreen
import com.tmark.client.ui.screens.cart.CartCalendarScreen
import com.tmark.client.ui.screens.cart.CartScreen
import com.tmark.client.ui.screens.orders.OrdersTabScreen
import com.tmark.client.ui.screens.profile.ProfileScreen
import com.tmark.client.ui.screens.invoices.InvoicesScreen
import com.tmark.client.ui.screens.requests.NewRequestScreen
import com.tmark.client.ui.screens.profile.ChangePasswordScreen
import com.tmark.client.ui.screens.profile.EditProfileScreen
import com.tmark.client.ui.theme.*

// 5 tabs: Home, Browse, +Request, Orders, Profile
private data class TabItem(val route: String, val label: String, val icon: ImageVector)
private val newRequestBaseRoute = "new-request"

private val tabs = listOf(
    TabItem(Screen.Dashboard.route,          "HOME",    Icons.Outlined.Home),
    TabItem(Screen.Catalog.route,            "BROWSE",  Icons.Outlined.GridView),
    TabItem(Screen.NewRequest.createRoute(), "REQUEST", Icons.Outlined.AddCircle),
    TabItem(Screen.Orders.route,             "ORDERS",  Icons.Outlined.ShoppingBag),
    TabItem(Screen.Profile.route,            "PROFILE", Icons.Outlined.Person)
)

private val noTabRoutes = setOf(
    Screen.Login.route,
    Screen.PackageDetail.route,
    Screen.NewRequest.route,
    Screen.Cart.route,
    Screen.CartCalendar.route,
    Screen.Invoices.route,
    Screen.OrderDetail.route,
    Screen.ChangePassword.route,
    Screen.EditProfile.route
)

private fun isActiveTab(currentRoute: String, tabRoute: String): Boolean {
    if (tabRoute.startsWith(newRequestBaseRoute)) return currentRoute.startsWith(newRequestBaseRoute)
    return currentRoute == tabRoute
}

@Composable
fun AppNavigation(tokenStore: TokenStore) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val isLoggedIn by tokenStore.accessToken.collectAsState(initial = null)
    val startDest = if (isLoggedIn != null) Screen.Dashboard.route else Screen.Login.route

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNewRequest = { navController.navigate(Screen.NewRequest.route) },
                    onOrders = { navController.navigate(Screen.Orders.route) },
                    onInvoices = { navController.navigate(Screen.Invoices.route) },
                    tokenStore = tokenStore
                )
            }

            composable(Screen.Catalog.route) {
                CatalogScreen(
                    onPackageClick = { id ->
                        navController.navigate(Screen.PackageDetail.createRoute(id))
                    },
                    onViewRequest = {
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Screen.CartCalendar.route) }
                )
            }

            composable(Screen.CartCalendar.route) {
                CartCalendarScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = {
                        navController.navigate(Screen.NewRequest.route) {
                            popUpTo(Screen.Catalog.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.PackageDetail.route,
                arguments = listOf(navArgument("packageId") { type = NavType.StringType })
            ) { backStack ->
                val pkgId = backStack.arguments?.getString("packageId") ?: return@composable
                PackageDetailScreen(
                    packageId = pkgId,
                    onBack = { navController.popBackStack() },
                    onAddedToCart = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.NewRequest.route,
                arguments = listOf(
                    navArgument("entityId")   { type = NavType.StringType; defaultValue = "" },
                    navArgument("entityType") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStack ->
                val entityId   = backStack.arguments?.getString("entityId")   ?: ""
                val entityType = backStack.arguments?.getString("entityType") ?: ""
                NewRequestScreen(
                    preSelectedEntityId   = entityId,
                    preSelectedEntityType = entityType,
                    onCancel = { navController.popBackStack() },
                    onSubmitted = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdersTabScreen(
                    onOrderClick = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) },
                    onNewRequest = { navController.navigate(Screen.NewRequest.route) }
                )
            }

            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStack ->
                val orderId = backStack.arguments?.getString("orderId") ?: return@composable
                OrderDetailPlaceholder(orderId = orderId, onBack = { navController.popBackStack() })
            }

            composable(Screen.Invoices.route) {
                InvoicesScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onChangePassword = { hasPassword ->
                        navController.navigate(Screen.ChangePassword.createRoute(hasPassword))
                    },
                    onEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ChangePassword.route,
                arguments = listOf(navArgument("hasPassword") { type = NavType.BoolType; defaultValue = true })
            ) { backStack ->
                val hasPassword = backStack.arguments?.getBoolean("hasPassword") ?: true
                ChangePasswordScreen(
                    hasPassword = hasPassword,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
        }

        val hideTab = currentRoute == null ||
            noTabRoutes.any { currentRoute == it } ||
            currentRoute.startsWith(newRequestBaseRoute)
        if (!hideTab) {
            BottomTabBar(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun BottomTabBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TMarkBlack)
            .navigationBarsPadding()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            val active = isActiveTab(currentRoute, tab.route)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected(tab.route) }
                    .padding(vertical = 8.dp)
            ) {
                if (active) {
                    Box(Modifier.size(4.dp).background(TMarkRed))
                    Spacer(Modifier.height(2.dp))
                }
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    modifier = Modifier.size(20.dp),
                    tint = if (active) TMarkRed else TMarkMuted
                )
                Text(
                    text = tab.label,
                    fontFamily = BarlowCondensed,
                    fontSize = 8.sp,
                    letterSpacing = 0.1.em,
                    color = if (active) TMarkRed else TMarkMuted
                )
            }
        }
    }
}

@Composable
private fun OrderDetailPlaceholder(orderId: String, onBack: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize().background(TMarkOffWhite)
    ) {
        com.tmark.client.ui.components.ScreenHeader(
            title = "Order Detail",
            eyebrow = "My Orders",
            onBack = onBack,
            compact = true
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order: $orderId", fontFamily = Barlow, color = TMarkMuted)
        }
    }
}
