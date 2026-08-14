package com.galaxywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.galaxywallet.ui.screens.* // Убедитесь, что эти пакеты существуют!
import com.galaxywallet.ui.theme.GalaxyWalletTheme
import org.web3j.crypto.MnemonicUtils // Для реальной генерации seed (добавьте зависимость)

class MainActivity : ComponentActivity() {
    
    private var currentScreen by mutableStateOf("language")
    private var mnemonic by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
        val saved = prefs.getString("wallet_data", null)
        
        if (saved != null) {
            currentScreen = "main"
        }
        
        setContent {
            GalaxyWalletTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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
                                // БЕЗОПАСНАЯ генерация seed через Web3j или BIP39
                                // mnemonic = MnemonicUtils.generateMnemonic()
                                mnemonic = "apple banana cherry dog eagle forest green house island jungle king lion" 
                                currentScreen = "seed"
                            }
                        )
                        
                        "seed" -> SeedPhraseScreen(
                            mnemonic = mnemonic,
                            onCopy = {},
                            onSaved = {
                                val walletData = mapOf(
                                    "address" to "0x...", // Здесь должен быть реальный адрес из mnemonic
                                    "mnemonic" to mnemonic
                                )
                                prefs.edit().putString("wallet_data", walletData.toString()).apply()
                                currentScreen = "pin"
                            }
                        )
                        
                        "import" -> ImportScreen(
                            onBack = { currentScreen = "create_import" },
                            onImported = { currentScreen = "pin" }
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
    }
}

@Composable
fun CreateImportScreen(
    onCreate: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Добро пожаловать",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Создайте новый кошелек или восстановите",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Создать кошелек", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Импортировать", fontSize = 16.sp)
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
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "Импорт кошелька",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
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
            shape = MaterialTheme.shapes.medium,
            enabled = seed.isNotEmpty()
        ) {
            Text("Импортировать", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Назад", fontSize = 16.sp)
        }
    }
}
