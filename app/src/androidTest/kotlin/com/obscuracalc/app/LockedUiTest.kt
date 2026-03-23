package com.obscuracalc.app

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LockedUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun vaultDestinationIsHiddenWhileLocked() {
        composeRule.onNodeWithText("Calc").assertExists()
        composeRule.onNodeWithText("Convert").assertExists()
        composeRule.onNodeWithText("Vault").assertDoesNotExist()
    }
}
