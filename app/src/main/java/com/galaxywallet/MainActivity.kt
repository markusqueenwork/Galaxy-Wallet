package com.galaxywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.galaxywallet.ui.screens.*
import com.galaxywallet.ui.theme.GalaxyWalletTheme

class MainActivity : ComponentActivity() {
    
    private var currentScreen by mutableStateOf("language")
    private var mnemonic by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Проверяем сохранённый кошелёк
        val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
        val saved = prefs.getString("wallet_data", null)
        
        if (saved != null) {
            currentScreen = "main"
        }
        
        setContent {
            GalaxyWalletTheme {
                when (currentScreen) {
                    "language" -> LanguageScreen(
                        onLanguageSelected = { lang ->
                            prefs.edit().putString("language", lang).apply()
                            currentScreen = "create_import"
                        }
                    )
                    
                    "create_import" -> CreateImportScreen(
                        onCreate = { currentScreen = "create_wallet" },
                        onImport = { currentScreen = "import" }
                    )
                    
                    "create_wallet" -> CreateWalletScreen(
                        onBack = { currentScreen = "create_import" },
                        onCreated = {
                            // Генерируем seed-фразу
                            mnemonic = generateMnemonic()
                            currentScreen = "seed"
                        }
                    )
                    
                    "seed" -> SeedPhraseScreen(
                        mnemonic = mnemonic,
                        onCopy = {},
                        onSaved = {
                            // Сохраняем кошелёк
                            val walletData = mapOf(
                                "address" to generateAddress(),
                                "mnemonic" to mnemonic
                            )
                            prefs.edit().putString("wallet_data", walletData.toString()).apply()
                            currentScreen = "pin"
                        }
                    )
                    
                    "import" -> ImportScreen(
                        onBack = { currentScreen = "create_import" },
                        onImported = {
                            currentScreen = "pin"
                        }
                    )
                    
                    "pin" -> PinScreen(
                        title = "Создайте PIN-код",
                        onPinComplete = { pin ->
                            prefs.edit().putString("pin", pin).apply()
                            currentScreen = "main"
                        }
                    )
                    
                    "main" -> MainScreen(
                        address = prefs.getString("address", "0x...") ?: "0x...",
                        onSend = {},
                        onReceive = {},
                        onSwap = {},
                        onBuy = {},
                        onLogout = {
                            prefs.edit().clear().apply()
                            currentScreen = "language"
                        }
                    )
                }
            }
        }
    }
    
    private fun generateMnemonic(): String {
        val words = listOf(
            "apple", "banana", "cherry", "dog", "eagle", "forest",
            "green", "house", "island", "jungle", "king", "lion"
        )
        return words.joinToString(" ")
    }
    
    private fun generateAddress(): String {
        val chars = "0123456789abcdef"
        val sb = StringBuilder("0x")
        repeat(40) { sb.append(chars.random()) }
        return sb.toString()
    }
}

@Composable
fun CreateImportScreen(
    onCreate: () -> Unit,
    onImport: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(com.galaxywallet.ui.theme.Background)
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            "Добро пожаловать",
            fontSize = 28.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = com.galaxywallet.ui.theme.TextMain
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Создайте новый кошелек или восстановите",
            fontSize = 14.sp,
            color = com.galaxywallet.ui.theme.TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.galaxywallet.ui.theme.Accent,
                contentColor = com.galaxywallet.ui.theme.Background
            )
        ) {
            Text("Создать кошелек", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Импортировать", color = com.galaxywallet.ui.theme.TextMain)
        }
    }
}

@Composable
fun ImportScreen(
    onBack: () -> Unit,
    onImported: () -> Unit
) {
    var seed by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.galaxywallet.ui.theme.Background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "Импорт кошелька",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = com.galaxywallet.ui.theme.TextMain
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = seed,
            onValueChange = { seed = it },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("Seed-фраза или приватный ключ") }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onImported,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.galaxywallet.ui.theme.Accent,
                contentColor = com.galaxywallet.ui.theme.Background
            ),
            enabled = seed.isNotEmpty()
        ) {
            Text("Импортировать", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Назад", color = com.galaxywallet.ui.theme.TextMain)
        }
    }
}
