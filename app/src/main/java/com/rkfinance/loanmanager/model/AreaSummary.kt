package com.rkfinance.loanmanager.model

import com.rkfinance.loanmanager.data.enums.Area

data class AreaSummary(
    val area: Area,
    val totalOutstandingExposure: Double,
    val activeLoansCount: Int,
    val totalLoansInArea: Int
)