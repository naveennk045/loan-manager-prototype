package com.rkfinance.loanmanager.ui.navigation


sealed class Screen(val route: String) {
    object ClientList : Screen("client_list")
    object AddEditClient : Screen("add_edit_client?clientId={clientId}") { // Optional clientId
        fun createRoute(clientId: Int? = null): String {
            return if (clientId != null) "add_edit_client?clientId=$clientId" else "add_edit_client"
        }
    }
    object LoanList : Screen("loan_list/{clientId}") { // Requires clientId
        fun createRoute(clientId: Int): String = "loan_list/$clientId"
    }
    object AddEditLoan : Screen("add_edit_loan?loanId={loanId}&clientId={clientId}") { // Optional loanId, required clientId for new loan
        fun createRoute(clientId: Int, loanId: Int? = null): String {
            return "add_edit_loan?clientId=$clientId" + (loanId?.let { "&loanId=$it" } ?: "")
        }
    }
    object PaymentList : Screen("payment_list/{loanId}") { // Requires loanId
        fun createRoute(loanId: Int): String = "payment_list/$loanId"
    }
    object AddPayment : Screen("add_payment/{loanId}") { // Requires loanId
        fun createRoute(loanId: Int): String = "add_payment/$loanId"
    }
    object Dashboard : Screen("dashboard")
}
