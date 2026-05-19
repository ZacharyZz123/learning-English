package com.xuexi.learningenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xuexi.learningenglish.ui.navigation.AppNavHost
import com.xuexi.learningenglish.ui.theme.LearningEnglishTheme
import com.xuexi.learningenglish.ui.viewmodel.WordViewModel
import com.xuexi.learningenglish.ui.viewmodel.WordViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = LearningEnglishAppContainer(applicationContext)
        setContent {
            LearningEnglishTheme {
                Surface(modifier = Modifier) {
                    val viewModel: WordViewModel = viewModel(
                        factory = WordViewModelFactory(appContainer.repository)
                    )
                    AppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
