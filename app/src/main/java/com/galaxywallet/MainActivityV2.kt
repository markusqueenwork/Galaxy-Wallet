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
        mutableStateOf("import")
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
        currentScreen == "import" &&
        encryptedPrefs.contains("mnemonic_encrypted") &&
        encryptedPrefs.contains("address")
    ) {
        currentScreen = "main"
    }

    when (currentScreen) {
        "import" -> {
            ImportScreen(
                onImport = { seed ->
                    importSeedInput = seed
                    currentScreen = "processing"
                }
            )
        }

        "processing" -> {
            LaunchedEffect(importSeedInput) {
                try {
                    val normalizedMnemonic = importSeedInput
                        .trim()
                        .lowercase()
                        .split(Regex("\\s+"))
                        .joinToString(" ")

                    if (normalizedMnemonic.isBlank()) {
                        throw IllegalArgumentException(
                            "Seed-фраза пустая"
                        )
                    }

                    val isValid =
                        MnemonicUtils.validateMnemonic(
                            normalizedMnemonic
                        )

                    if (!isValid) {
                        throw IllegalArgumentException(
                            "Неверная seed-фраза"
                        )
                    }

                    val seed =
                        MnemonicUtils.generateSeed(
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
                    currentScreen = "import"
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

                    importSeedInput = ""
                    currentScreen = "import"
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
private fun ImportScreen(
    onImport: (String) -> Unit
) {
    var seed by remember {
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
            text = "Galaxy Wallet",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Вход по существующей seed-фразе",
            fontSize = 15.sp,
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
                .height(130.dp),
            placeholder = {
                Text(
                    text = "Введите 12 или 24 слова"
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onImport(seed)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = seed.trim().isNotEmpty()
        ) {
            Text(text = "Войти")
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
                text = "Проверяем seed-фразу...",
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
            text = "PIN будет использоваться для входа",
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
            Text(text = "Продолжить")
        }
    }
}