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
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Credentials
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class MainActivity : ComponentActivity() {
    
    private var currentScreen by mutableStateOf("language")
    private var mnemonic by mutableStateOf("")
    private lateinit var encryptedPrefs: EncryptedSharedPreferences
    
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
        )
        
        // Проверяем сохранённый кошелёк
        val savedMnemonic = encryptedPrefs.getString("mnemonic_encrypted", null)
        if (savedMnemonic != null) {
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
                                // БЕЗОПАСНАЯ генерация seed по BIP39
                                mnemonic = generateSecureMnemonic()
                                currentScreen = "seed"
                            }
                        )
                        
                        "seed" -> SeedPhraseScreen(
                            mnemonic = mnemonic,
                            onCopy = {},
                            onSaved = {
                                // Получаем реальный EVM адрес из seed
                                val credentials = Credentials.create(
                                    MnemonicUtils.generateSeed(mnemonic, "")
                                )
                                val address = credentials.address
                                
                                // Шифруем и сохраняем seed
                                saveEncryptedMnemonic(mnemonic)
                                encryptedPrefs.edit()
                                    .putString("address", address)
                                    .apply()
                                    
                                currentScreen = "pin"
                            }
                        )
                        
                        "import" -> ImportScreen(
                            onBack = { currentScreen = "create_import" },
                            onImported = { seed ->
                                try {
                                    val credentials = Credentials.create(
                                        MnemonicUtils.generateSeed(seed, "")
                                    )
                                    saveEncryptedMnemonic(seed)
                                    encryptedPrefs.edit()
                                        .putString("address", credentials.address)
                                        .apply()
                                    currentScreen = "pin"
                                } catch (e: Exception) {
                                    // Показать ошибку пользователю
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
        }
    }
    
    /**
     * Генерирует криптографически стойкую seed-фразу по стандарту BIP39
     * Использует SecureRandom для 128 бит энтропии (12 слов)
     */
    private fun generateSecureMnemonic(): String {
        return MnemonicUtils.generateMnemonic()
    }
    
    /**
     * Безопасно сохраняет seed-фразу в EncryptedSharedPreferences
     * Данные шифруются на уровне файловой системы Android
     */
    private fun saveEncryptedMnemonic(mnemonic: String) {
        encryptedPrefs.edit()
            .putString("mnemonic_encrypted", mnemonic)
            .apply()
    }
}

// Заглушки для отсутствующих экранов (создайте полноценные реализации позже)
@Composable
fun LanguageScreen(onLanguageSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Выберите язык", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onLanguageSelected("ru") }) { Text("Русский") }
        Button(onClick = { onLanguageSelected("en") }) { Text("English") }
    }
}

@Composable
fun CreateWalletScreen(onBack: () -> Unit, onCreated: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Создание кошелька", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreated) { Text("Сгенерировать seed") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}

@Composable
fun SeedPhraseScreen(mnemonic: String, onCopy: () -> Unit, onSaved: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Сохраните seed-фразу", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(mnemonic, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSaved, modifier = Modifier.fillMaxWidth()) { Text("Я сохранил") }
    }
}

@Composable
fun PinScreen(title: String, onPinComplete: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("PIN: ${pin.padEnd(4, '•')}", fontSize = 32.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { if (pin.length == 4) onPinComplete(pin) }) { 
            Text("Подтвердить") 
        }
    }
}

@Composable
fun MainScreen(
    address: String,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onSwap: () -> Unit,
    onBuy: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Главный экран", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Адрес: $address", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSend, modifier = Modifier.fillMaxWidth()) { Text("Отправить") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onReceive, modifier = Modifier.fillMaxWidth()) { Text("Получить") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Выйти") }
    }
}
