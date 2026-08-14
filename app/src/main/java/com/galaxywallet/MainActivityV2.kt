package com.galaxywallet

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.galaxywallet.ui.screens.MainScreen
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils

class MainActivityV2 : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WalletApp()
                }
            }
        }
    }
}

@Composable
fun WalletApp() {
    val context = LocalContext.current

    var currentScreen by remember {
        mutableStateOf("language")
    }

    var mnemonic by remember {
        mutableStateOf("")
    }

    var importSeedInput by remember {
        mutableStateOf("")
    }

    val encryptedPrefs: SharedPreferences = remember {
        val masterKeyAlias = MasterKeys.getOrCreate(
            MasterKeys.AES256_GCM_SPEC
        )

        EncryptedSharedPreferences.create(
            "galaxy_wallet_secure",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    if (
        currentScreen == "language" &&
        encryptedPrefs.contains("mnemonic_encrypted")
    ) {
        currentScreen = "main"
    }

    when (currentScreen) {
        "language" -> {
            LanguageScreen(
                onRussian = {
                    encryptedPrefs.edit()
                        .putString("language", "ru")
                        .apply()

                    currentScreen = "create_import"
                },
                onEnglish = {
                    encryptedPrefs.edit()
                        .putString("language", "en")
                        .apply()

                    currentScreen = "create_import"
                }
            )
        }

        "create_import" -> {
            CreateImportScreen(
                onCreate = {
                    currentScreen = "create_wallet"
                },
                onImport = {
                    currentScreen = "import_input"
                }
            )
        }

        "create_wallet" -> {
            CreateWalletScreen(
                onGenerate = {
                    try {
                        mnemonic = MnemonicUtils.generateMnemonic()
                        currentScreen = "seed"
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                },
                onBack = {
                    currentScreen = "create_import"
                }
            )
        }

        "seed" -> {
            SeedScreen(
                mnemonic = mnemonic,
                onSaved = {
                    try {
                        val seed = MnemonicUtils.generateSeed(
                            mnemonic,
                            ""
                        )

                        val masterKey =
                            Bip32ECKeyPair.generateKeyPair(seed)

                        val derivedKey =
                            Bip32ECKeyPair.deriveKeyPair(
                                masterKey,
                                ethereumPath()
                            )

                        val credentials =
                            Credentials.create(derivedKey)

                        encryptedPrefs.edit()
                            .putString(
                                "mnemonic_encrypted",
                                mnemonic
                            )
                            .putString(
                                "address",
                                credentials.address
                            )
                            .apply()

                        currentScreen = "pin"
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
            )
        }

        "import_input" -> {
            ImportScreen(
                onImport = { input ->
                    importSeedInput = input
                    currentScreen = "import_processing"
                },
                onBack = {
                    currentScreen = "create_import"
                }
            )
        }

        "import_processing" -> {
            LaunchedEffect(importSeedInput) {
                try {
                    val normalizedMnemonic = importSeedInput
                        .trim()
                        .split(Regex("\\s+"))
                        .joinToString(" ")

                    if (
                        !MnemonicUtils.validateMnemonic(
                            normalizedMnemonic
                        )
                    ) {
                        throw IllegalArgumentException(
                            "Неверная seed-фраза"
                        )
                    }

                    val seed = MnemonicUtils.generateSeed(
                        normalizedMnemonic,
                        ""
                    )

                    val masterKey =
                        Bip32ECKeyPair.generateKeyPair(seed)

                    val derivedKey =
                        Bip32ECKeyPair.deriveKeyPair(
                            masterKey,
                            ethereumPath()
                        )

                    val credentials =
                        Credentials.create(derivedKey)

                    encryptedPrefs.edit()
                        .putString(
                            "mnemonic_encrypted",
                            normalizedMnemonic
                        )
                        .putString(
                            "address",
                            credentials.address
                        )
                        .apply()

                    currentScreen = "pin"
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    currentScreen = "import_input"
                }
            }

            LoadingScreen()
        }

        "pin" -> {
            PinScreen(
                onConfirm = { pin ->
                    encryptedPrefs.edit()
                        .putString("pin", pin)
                        .apply()

                    currentScreen = "main"
                }
            )
        }

        "main" -> {
            MainScreen(
                address = encryptedPrefs.getString(
                    "address",
                    "0x..."
                ) ?: "0x...",
                onSend = {},
                onReceive = {},
                onSwap = {},
                onBuy = {},
                onLogout = {
                    encryptedPrefs.edit()
                        .clear()
                        .apply()

                    mnemonic = ""
                    importSeedInput = ""
                    currentScreen = "language"
                }
            )
        }
    }
}

private fun ethereumPath(): IntArray {
    return intArrayOf(
        44 or Bip32ECKeyPair.HARDENED_BIT,
        60 or Bip32ECKeyPair.HARDENED_BIT,
        0 or Bip32ECKeyPair.HARDENED_BIT,
        0,
        0
    )
}

@Composable
private fun LanguageScreen(
    onRussian: () -> Unit,
    onEnglish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Galaxy Wallet",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Выберите язык",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRussian,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Русский")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEnglish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "English")
        }
    }
}

@Composable
private fun CreateImportScreen(
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
            text = "Добро пожаловать",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Создайте или импортируйте кошелек",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Создать кошелек")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onImport,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Импортировать")
        }
    }
}

@Composable
private fun CreateWalletScreen(
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Создание кошелька",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Сгенерировать seed-фразу")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Назад")
        }
    }
}

@Composable
private fun SeedScreen(
    mnemonic: String,
    onSaved: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Сохраните seed-фразу",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Запишите эти слова. Никому их не показывайте!",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = mnemonic,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSaved,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Я сохранил")
        }
    }
}

@Composable
private fun ImportScreen(
    onImport: (String) -> Unit,
    onBack: () -> Unit
) {
    var seed by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Импорт кошелька",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Введите seed-фразу",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = seed,
            onValueChange = {
                seed = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = {
                Text(text = "word1 word2 word3...")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onImport(seed.trim())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = seed.trim().isNotEmpty()
        ) {
            Text(text = "Импортировать")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Назад")
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFFB39DDB)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Импортируем...",
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun PinScreen(
    onConfirm: (String) -> Unit
) {
    var pin by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Создайте PIN-код",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Для быстрого доступа",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PIN: ${pin.padEnd(4, '•')}",
            fontSize = 32.sp,
            letterSpacing = 8.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { value ->
                if (
                    value.length <= 4 &&
                    value.all { character ->
                        character.isDigit()
                    }
                ) {
                    pin = value
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "PIN-код")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onConfirm(pin)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = pin.length == 4
        ) {
            Text(text = "Подтвердить")
        }
    }
}