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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WalletApp()
                }
            }
        }
    }
}

@Composable
fun WalletApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentScreen by remember { mutableStateOf("language") }
    var mnemonic by remember { mutableStateOf("") }
    var importSeedInput by remember { mutableStateOf("") }
    
    val encryptedPrefs = remember {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "galaxy_wallet_secure", masterKeyAlias, context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as SharedPreferences
    }

    if (currentScreen == "language" && encryptedPrefs.contains("mnemonic_encrypted")) {
        currentScreen = "main"
    }

    when (currentScreen) {
        "language" -> {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Выберите язык", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { 
                    encryptedPrefs.edit().putString("language", "ru").apply()
                    currentScreen = "create_import" 
                }) { Text("Русский") }
            }
        }
        "create_import" -> {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Добро пожаловать", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { currentScreen = "create_wallet" }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Создать") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { currentScreen = "import_input" }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Импортировать") }
            }
        }
        "create_wallet" -> {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Создание", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { 
                    mnemonic = MnemonicUtils.generateMnemonic()
                    currentScreen = "seed" 
                }) { Text("Сгенерировать seed") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { currentScreen = "create_import" }) { Text("Назад") }
            }
        }
        "seed" -> {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Text("Сохраните seed", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(mnemonic, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    val seed = MnemonicUtils.generateSeed(mnemonic, "")
                    val keyPair = ECKeyPair.create(seed)
                    val hex = keyPair.privateKey.toString(16).padStart(64, '0')
                    val cred = Credentials.create(hex)
                    encryptedPrefs.edit().putString("mnemonic_encrypted", mnemonic).apply()
                    encryptedPrefs.edit().putString("address", cred.address).apply()
                    currentScreen = "pin"
                }, modifier = Modifier.fillMaxWidth()) { Text("Я сохранил") }
            }
        }
        "import_input" -> {
            var seed by remember { mutableStateOf("") }
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Text("Импорт", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = seed, onValueChange = { seed = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Seed-фраза") })
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    importSeedInput = seed
                    currentScreen = "import_processing"
                }, modifier = Modifier.fillMaxWidth(), enabled = seed.isNotEmpty()) { Text("Импортировать") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { currentScreen = "create_import" }, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
            }
        }
        "import_processing" -> {
            LaunchedEffect(Unit) {
                try {
                    val genSeed = MnemonicUtils.generateSeed(importSeedInput, "")
                    val keyPair = ECKeyPair.create(genSeed)
                    val hex = keyPair.privateKey.toString(16).padStart(64, '0')
                    val cred = Credentials.create(hex)
                    encryptedPrefs.edit().putString("mnemonic_encrypted", importSeedInput).apply()
                    encryptedPrefs.edit().putString("address", cred.address).apply()
                    currentScreen = "pin"
                } catch (e: Exception) { 
                    currentScreen = "import_input"
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB39DDB))
            }
        }
        "pin" -> {
            var pin by remember { mutableStateOf("") }
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Создайте PIN", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Text("PIN: ${pin.padEnd(4, '•')}", fontSize = 32.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { 
                    if (pin.length == 4) {
                        encryptedPrefs.edit().putString("pin", pin).apply()
                        currentScreen = "main"
                    }
                }) { Text("Подтвердить") }
            }
        }
        "main" -> {
            MainScreen(
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