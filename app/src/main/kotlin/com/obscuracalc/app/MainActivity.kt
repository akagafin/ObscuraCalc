package com.obscuracalc.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.obscuracalc.app.ui.ObscuraCalcApp
import com.obscuracalc.app.ui.theme.ObscuraCalcTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as ObscuraCalcApplication).appContainer
        setContent {
            ObscuraCalcTheme {
                ObscuraCalcApp(container = container)
            }
        }
    }
}
