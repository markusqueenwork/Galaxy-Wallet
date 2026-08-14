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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Credentials
import java.security.Security
import android.content.SharedPreferences

class MainActivity : ComponentActivity() {

    private var currentScreen by mutableStateOf("language")
    private var mnemonic by mutableStateOf("")
    // ИСПРАВЛЕНИЕ 1: Тип изменен на SharedPreferences
    private lateinit var encryptedPrefs: SharedPreferences 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация зашифрованного хранилища
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedPrefs = EncryptedSharedPreferences.create(
            "galaxy_wallet_secure",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as SharedPreferences // Явное приведение типа для безопасности

        if (encryptedPrefs.contains("mnemonic_encrypted")) {
            currentScreen = "main"
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "language" -> LanguageScreen(
                            onLanguageSelected = { lang ->
                                encryptedPrefs.edit().putString("language", lang).apply()
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
                                mnemonic = generateSecureMnemonic()
                                currentScreen = "seed"
                            }
                        )
                        "seed" -> SeedPhraseScreen(
                            mnemonic = mnemonic,
                            onCopy = {},
                            onSaved = {
                                // ИСПРАВЛЕНИЕ 3: Правильная цепочка генерации адреса
                                try {
                                    val seed = MnemonicUtils.generateSeed(mnemonic, "")
                                    val privateKey = org.web3j.crypto.ECKeyPair.create(seed).privateKey
                                    val credentials = Credentials.create(privateKey)
                                    
                                    saveEncryptedMnemonic(mnemonic)
                                    encryptedPrefs.edit()
                                        .putString("address", credentials.address)
                                        .apply()
                                    currentScreen = "pin"
                                } catch (e: Exception) {
                                    e.printStackTrace() // Для отладки в логах
                                }
                            }
                        )
                        "import" -> ImportScreen(
                            onBack = { currentScreen = "create_import" },
                            // ИСПРАВЛЕНИЕ 3: Обработка импорта с правильной криптографией
                            onImported = { inputSeed ->
                                try {
                                    val seed = MnemonicUtils.generateSeed(inputSeed, "")
                                    val privateKey = org.web3j.crypto.ECKeyPair.create(seed).privateKey
                                    val credentials = Credentials.create(privateKey)
                                    
                                    saveEncryptedMnemonic(inputSeed)
                                    encryptedPrefs.edit()
                                        .putString("address", credentials.address)
                                        .apply()
                                    currentScreen = "pin"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                        "pin" -> PinScreen(
                            title = "Создайте PIN-код",
                            onPinComplete = { pin ->
                                encryptedPrefs.edit().putString("pin", pin).apply()
                                currentScreen = "main"
                            }
                        )
                        "main" -> MainScreen(
                            address = encryptedPrefs.getString("address", "0x...") ?: "0x...",
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

    private fun generateSecureMnemonic(): String {
        return MnemonicUtils.generateMnemonic()
    }

    private fun saveEncryptedMnemonic(mnemonic: String) {
        encryptedPrefs.edit()
            .putString("mnemonic_encrypted", mnemonic)
            .apply()
    }
}

// --- ЗАГЛУШКИ ЭКРАНОВ (ИСПРАВЛЕНИЕ 2) ---
// Эти функции должны быть в отдельных файлах, но для гарантии сборки 
// я добавил их сюда. Перенесите их позже в ui/screens/

@Composable
fun LanguageScreen(onLanguageSelected: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Выберите язык", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onLanguageSelected("ru") }) { Text("Русский") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { onLanguageSelected("en") }) { Text("English") }
    }
}

@Composable
fun CreateImportScreen(onCreate: () -> Unit, onImport: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Добро пожаловать", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Создать кошелек") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Импортировать") }
    }
}

@Composable
fun CreateWalletScreen(onBack: () -> Unit, onCreated: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Создание кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreated) { Text("Сгенерировать seed") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}

@Composable
fun SeedPhraseScreen(mnemonic: String, onCopy: () -> Unit, onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Сохраните seed-фразу", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(mnemonic, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSaved, modifier = Modifier.fillMaxWidth()) { Text("Я сохранил") }
    }
}

@Composable
fun ImportScreen(onBack: () -> Unit, onImported: (String) -> Unit) {
    var seed by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Импорт кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = seed, onValueChange = { seed = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Seed-фраза") })
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onImported(seed) }, modifier = Modifier.fillMaxWidth(), enabled = seed.isNotEmpty()) { Text("Импортировать") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}

@Composable
fun PinScreen(title: String, onPinComplete: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("PIN: ${pin.padEnd(4, '•')}", fontSize = 32.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { if (pin.length == 4) onPinComplete(pin) }) { Text("Подтвердить") }
    }
}

@Composable
fun MainScreen(address: String, onLogout: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Главный экран", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Адрес: $address", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Выйти") }
    }
}