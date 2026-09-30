package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.auth.AuthRepository
import com.example.ui.ArthroscanShellScreen
import com.example.ui.ArthroscanViewModel
import com.example.ui.portal.AshaLoginScreen
import com.example.ui.portal.AshaPortalMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: ArthroscanViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AshaPortalMainScreen(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "ARTHROSCAN-NER: $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Active") }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun AshaLoginScreenPreview() {
  MyApplicationTheme {
    AshaLoginScreen(
      authRepository = AuthRepository(),
      onLoginSuccess = {}
    )
  }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun AshaPortalMainScreenPreview() {
  MyApplicationTheme {
    AshaPortalMainScreen(viewModel = ArthroscanViewModel())
  }
}


@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun ArthroscanShellScreenPreview() {
  MyApplicationTheme {
    ArthroscanShellScreen(viewModel = ArthroscanViewModel())
  }
}


