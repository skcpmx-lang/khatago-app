package com.shohan.khatago.ui

enum class MainTab(val label: String) {
    DASHBOARD("Dashboard"),
    ACCOUNTS("Accounts"),
    TRANSACTIONS("Transactions"),
    REPORTS("Reports"),
    SETTINGS("Settings")
}

object Routes {
    const val ONBOARDING = "onboarding"
    const val SETUP = "setup"
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail"

    fun detail(type: String, id: Long): String = "$DETAIL/$type/$id"
}

enum class AddSheetType {
    SHOP_CREDIT,
    LOAN,
    EMI,
    PERSONAL_DEBT,
    PERSONAL_BORROWED,
    PERSONAL_LENT,
    INCOME,
    EXPENSE,
    PAYMENT
}
