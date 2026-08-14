package com.galaxywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import android.content.SharedPreferences
import com.galaxywallet.ui.screens.MainScreen

// Жесткий HEX-код акцентного цвета, чтобы не зависеть от импортов темы
private val ACCENT_COLOR = Color(0xFFB39DDB)

class MainActivity : ComponentActivity() {
    private var currentScreen by mutableStateOf("language")
    private var mnemonic by mutableStateOf("")
    private var importSeedInput by mutableStateOf("") 
    private lateinit var encryptedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedPrefs = EncryptedSharedPreferences.create(
            "galaxy_wallet_secure", masterKeyAlias, this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as SharedPreferences

        if (encryptedPrefs.contains("mnemonic_encrypted")) currentScreen = "main"

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "language" -> LocalLanguageScreen(onNext = { 
                            encryptedPrefs.edit().putString("language", "ru").apply()
                            currentScreen = "create_import" 
                        })
                        
                        "create_import" -> LocalCreateImportScreen(
                            onCreate = { currentScreen = "create_wallet" },
                            onImportClick = { currentScreen = "import_input" }
                        )
                        
                        "create_wallet" -> LocalCreateWalletScreen(
                            onBack = { currentScreen = "create_import" },
                            onCreated = {
                                mnemonic = MnemonicUtils.generateMnemonic()
                                currentScreen = "seed"
                            }
                        )
                        
                        "seed" -> LocalSeedPhraseScreen(mnemonic = mnemonic, onSaved = {
                            try {
                                val seed = MnemonicUtils.generateSeed(mnemonic, "")
                                val keyPair = ECKeyPair.create(seed)
                                val privateKeyHex = keyPair.privateKey.toString(16).padStart(64, '0')
                                val credentials = Credentials.create(privateKeyHex)
                                encryptedPrefs.edit().putString("mnemonic_encrypted", mnemonic).apply()
                                encryptedPrefs.edit().putString("address", credentials.address).apply()
                                currentScreen = "pin"
                            } catch (e: Exception) { e.printStackTrace() }
                        })
                        
                        "import_input" -> LocalImportInputScreen(
                            onBack = { currentScreen = "create_import" },
                            onConfirm = { input ->
                                importSeedInput = input
                                currentScreen = "import_processing"
                            }
                        )
                        
                        "import_processing" -> {
                            LaunchedEffect(Unit) {
                                try {
                                    val generatedSeed = MnemonicUtils.generateSeed(importSeedInput, "")
                                    val keyPair = ECKeyPair.create(generatedSeed)
                                    val privateKeyHex = keyPair.privateKey.toString(16).padStart(64, '0')
                                    val credentials = Credentials.create(privateKeyHex)
                                    encryptedPrefs.edit().putString("mnemonic_encrypted", importSeedInput).apply()
                                    encryptedPrefs.edit().putString("address", credentials.address).apply()
                                    currentScreen = "pin"
                                } catch (e: Exception) { 
                                    e.printStackTrace() 
                                    currentScreen = "import_input"
                                }
                            }
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = ACCENT_COLOR)
                            }
                        }

                        "pin" -> LocalPinScreen(onComplete = { pin ->
                            encryptedPrefs.edit().putString("pin", pin).apply()
                            currentScreen = "main"
                        })
                        
                        "main" -> MainScreen(
                            address = encryptedPrefs.getString("address", "0x...") ?: "0x...",
                            onSend = {}, onReceive = {}, onSwap = {}, onBuy = {},
                            onLogout = {
                                encryptedPrefs.edit().clear().apply()
                                currentScreen = "language"
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- ПРОСТЫЕ ЗАГЛУШКИ БЕЗ СЛОЖНЫХ ПАРАМЕТРОВ ---

@Composable
fun LocalLanguageScreen(onNext: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Выберите язык", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNext) { Text("Русский") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNext) { Text("English") }
    }
}

@Composable
fun LocalCreateImportScreen(onCreate: () -> Unit, onImportClick: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Добро пожаловать", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Создать кошелек") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onImportClick, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Импортировать") }
    }
}

@Composable
fun LocalCreateWalletScreen(onBack: () -> Unit, onCreated: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Создание кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreated) { Text("Сгенерировать seed") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}

@Composable
fun LocalSeedPhraseScreen(mnemonic: String, onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Сохраните seed-фразу", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(mnemonic, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSaved, modifier = Modifier.fillMaxWidth()) { Text("Я сохранил") }
    }
}

@Composable
fun LocalImportInputScreen(onBack: () -> Unit, onConfirm: (String) -> Unit) {
    var seed by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Импорт кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = seed, onValueChange = { seed = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Seed-фраза") })
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onConfirm(seed) }, modifier = Modifier.fillMaxWidth(), enabled = seed.isNotEmpty()) { Text("Импортировать") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}

@Composable
fun LocalPinScreen(onComplete: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Создайте PIN-код", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("PIN: ${pin.padEnd(4, '•')}", fontSize = 32.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { if (pin.length == 4) onComplete(pin) }) { Text("Подтвердить") }
    }
}