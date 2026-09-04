package com.ultimate.filemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ultimate.filemanager.ui.UfmApp
import com.ultimate.filemanager.ui.theme.UfmTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UfmTheme {
                UfmApp()
            }
        }
    }
}
