package com.keofi.poonamashishmehta_votergen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keofi.poonamashishmehta_votergen.ui.MainScreen
import com.keofi.poonamashishmehta_votergen.ui.theme.PoonamAshishMehtaVoterGenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoonamAshishMehtaVoterGenTheme {
                MainScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PoonamAshishMehtaVoterGenTheme {
        MainScreen()
    }
}
