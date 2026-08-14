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
import org.web3j.crypto.Bip32ECKeyPair
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
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Galaxy Wallet", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Выберите язык", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    encryptedPrefs.edit().putString("language", "ru").apply()
                    currentScreen = "create_import"
                }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Русский") }
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    encryptedPrefs.edit().putString("language", "en").apply()
                    currentScreen = "create_import"
                }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("English") }
            }
        }

        "create_import" -> {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Добро пожаловать", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Создайте или импортируйте кошелек", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { currentScreen = "create_wallet" },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Создать кошелек") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { currentScreen = "import_input" },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Импортировать") }
            }
        }

        "create_wallet" -> {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Создание кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        try {
                            mnemonic = MnemonicUtils.generateMnemonic()
                            currentScreen = "seed"
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Сгенерировать seed-фразу") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { currentScreen = "create_import" },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Назад") }
            }
        }

        "seed" -> {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(Modifier.height(48.dp))
                Text(
                    "Сохраните seed-фразу",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Запишите эти 12 слов. Никому не показывайте!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    mnemonic,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        try {
                            val seed = MnemonicUtils.generateSeed(mnemonic, "")
                            val masterKey = Bip32ECKeyPair.generateKeyPair(seed)
                            val path = intArrayOf(
                                44 or Bip32ECKeyPair.HARDENED_BIT,
                                60 or Bip32ECKeyPair.HARDENED_BIT,
                                0 or Bip32ECKeyPair.HARDENED_BIT,
                                0, 0
                            )
                            val derivedKey = Bip32ECKeyPair.deriveKeyPair(masterKey, path)
                            val credentials = Credentials.create(derivedKey.privateKey)
                            encryptedPrefs.edit().putString("mnemonic_encrypted", mnemonic).apply()
                            encryptedPrefs.edit().putString("address", credentials.address).apply()
                            currentScreen = "pin"
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Я сохранил") }
            }
        }

        "import_input" -> {
            var seed by remember { mutableStateOf("") }
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Spacer(Modifier.height(48.dp))
                Text("Импорт кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Введите seed-фразу", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = seed,
                    onValueChange = { seed = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("word1 word2 word3...") }
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        importSeedInput = seed
                        currentScreen = "import_processing"
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = seed.isNotEmpty()
                ) { Text("Импортировать") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { currentScreen = "create_import" },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Назад") }
            }
        }

        "import_processing" -> {
            LaunchedEffect(Unit) {
                try {
                    val genSeed = MnemonicUtils.generateSeed(importSeedInput, "")
                    val masterKey = Bip32ECKeyPair.generateKeyPair(genSeed)
                    val path = intArrayOf(
                        44 or Bip32ECKeyPair.HARDENED_BIT,
                        60 or Bip32ECKeyPair.HARDENED_BIT,
                        0 or Bip32ECKeyPair.HARDENED_BIT,
                        0, 0
                    )
                    val derivedKey = Bip32ECKeyPair.deriveKeyPair(masterKey, path)
                    val credentials = Credentials.create(derivedKey.privateKey)
                    encryptedPrefs.edit().putString("mnemonic_encrypted", importSeedInput).apply()
                    encryptedPrefs.edit().putString("address", credentials.address).apply()
                    currentScreen = "pin"
                } catch (e: Exception) {
                    currentScreen = "import_input"
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFB39DDB))
                    Spacer(Modifier.height(16.dp))
                    Text("Импортируем...", color = Color.Gray)
                }
            }
        }

        "pin" -> {
            var pin by remember { mutableStateOf("") }
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Создайте PIN-код", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Для быстрого доступа", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Text("PIN: ${pin.padEnd(4, '•')}", fontSize = 32.sp, letterSpacing = 8.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (pin.length == 4) {
                            encryptedPrefs.edit().putString("pin", pin).apply()
                            currentScreen = "main"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = pin.length == 4
                ) { Text("Подтвердить") }
            }
        }

        "main" -> {
            MainScreen(
                address = encryptedPrefs.getString("address", "0x...") ?: "0x...",
                onSend = {},
                onReceive = {},
                onSwap = {},
                onBuy = {},
                onLogout = {
                    encryptedPrefs.edit().clear().apply()
                    currentScreen = "language"
                }
            )
        }
    }
}
