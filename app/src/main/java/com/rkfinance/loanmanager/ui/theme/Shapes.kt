package com.rkfinance.loanmanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Smaller elements, buttons
    medium = RoundedCornerShape(16.dp), // Cards, dialogs
    large = RoundedCornerShape(24.dp)   // Larger components, bottom sheets
)