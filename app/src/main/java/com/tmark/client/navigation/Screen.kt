package com.tmark.client.navigation

sealed class Screen(val route: String) {
    object Login        : Screen("login")
    object Dashboard    : Screen("dashboard")
    object Catalog      : Screen("catalog")
    object PackageDetail: Screen("package/{packageId}") {
        fun createRoute(id: String) = "package/$id"
    }
    object Cart         : Screen("cart")
    object CartCalendar : Screen("cart-calendar")
    object NewRequest   : Screen("new-request?entityId={entityId}&entityType={entityType}") {
        fun createRoute(entityId: String = "", entityType: String = "") =
            "new-request?entityId=$entityId&entityType=$entityType"
    }
    object Requests     : Screen("requests")
    object Orders       : Screen("orders")
    object OrderDetail  : Screen("order/{orderId}") {
        fun createRoute(id: String) = "order/$id"
    }
    object Invoices        : Screen("invoices")
    object Profile         : Screen("profile")
    object ChangePassword  : Screen("change-password?hasPassword={hasPassword}") {
        fun createRoute(hasPassword: Boolean) = "change-password?hasPassword=$hasPassword"
    }
    object EditProfile     : Screen("edit-profile")
}
