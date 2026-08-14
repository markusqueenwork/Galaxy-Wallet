package com.galaxywallet

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {
    
    private lateinit var screenContainer: LinearLayout
    private val gson = Gson()
    private val client = OkHttpClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Проверяем сохранённый кошелёк
        val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
        val savedWallet = prefs.getString("wallet_data", null)
        
        if (savedWallet != null) {
            showMainScreen()
        } else {
            showLanguageScreen()
        }
    }
    
    // ============ ЭКРАН ВЫБОРА ЯЗЫКА ============
    private fun showLanguageScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#000000"))
        }
        
        val logo = TextView(this).apply {
            text = "G"
            textSize = 40f
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#6D28D9"))
        }
        
        val title = TextView(this).apply {
            text = "Galaxy Wallet"
            textSize = 28f
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
        }
        
        val ruBtn = Button(this).apply {
            text = "Русский"
            setOnClickListener { 
                saveLanguage("ru")
                showCreateImportScreen()
            }
        }
        
        val enBtn = Button(this).apply {
            text = "English"
            setOnClickListener { 
                saveLanguage("en")
                showCreateImportScreen()
            }
        }
        
        val jaBtn = Button(this).apply {
            text = "日本語"
            setOnClickListener { 
                saveLanguage("ja")
                showCreateImportScreen()
            }
        }
        
        layout.addView(logo)
        layout.addView(title)
        layout.addView(ruBtn)
        layout.addView(enBtn)
        layout.addView(jaBtn)
        
        setContentView(layout)
    }
    
    // ============ ЭКРАН СОЗДАНИЯ/ИМПОРТА ============
    private fun showCreateImportScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#000000"))
        }
        
        val title = TextView(this).apply {
            text = "Добро пожаловать"
            textSize = 24f
            setTextColor(android.graphics.Color.WHITE)
        }
        
        val createBtn = Button(this).apply {
            text = "Создать новый кошелек"
            setOnClickListener { showCreateWalletScreen() }
        }
        
        val importBtn = Button(this).apply {
            text = "Импортировать кошелек"
            setOnClickListener { showImportScreen() }
        }
        
        layout.addView(title)
        layout.addView(createBtn)
        layout.addView(importBtn)
        
        setContentView(layout)
    }
    
    // ============ ЭКРАН СОЗДАНИЯ КОШЕЛЬКА ============
    private fun showCreateWalletScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#000000"))
        }
        
        val title = TextView(this).apply {
            text = "Создание кошелька"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
        }
        
        val passwordInput = EditText(this).apply {
            hint = "Пароль (мин. 8 символов)"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }
        
        val generateBtn = Button(this).apply {
            text = "Создать кошелек"
            setOnClickListener {
                val password = passwordInput.text.toString()
                if (password.length >= 8) {
                    generateWallet()
                } else {
                    Toast.makeText(this@MainActivity, "Минимум 8 символов", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        layout.addView(title)
        layout.addView(passwordInput)
        layout.addView(generateBtn)
        
        setContentView(layout)
    }
    
    // ============ ГЕНЕРАЦИЯ КОШЕЛЬКА ============
    private fun generateWallet() {
        // Генерируем seed-фразу
        val words = listOf(
            "apple", "banana", "cherry", "dog", "eagle", "forest",
            "green", "house", "island", "jungle", "king", "lion"
        )
        
        val walletData = mapOf(
            "address" to generateAddress(),
            "mnemonic" to words.joinToString(" "),
            "created" to System.currentTimeMillis()
        )
        
        val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
        prefs.edit().putString("wallet_data", gson.toJson(walletData)).apply()
        
        showMainScreen()
    }
    
    // ============ ЭКРАН ИМПОРТА ============
    private fun showImportScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#000000"))
        }
        
        val title = TextView(this).apply {
            text = "Импорт кошелька"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
        }
        
        val seedInput = EditText(this).apply {
            hint = "Seed-фраза или приватный ключ"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
        }
        
        val importBtn = Button(this).apply {
            text = "Импортировать"
            setOnClickListener {
                val seed = seedInput.text.toString()
                if (seed.isNotEmpty()) {
                    val walletData = mapOf(
                        "address" to generateAddress(),
                        "mnemonic" to seed,
                        "created" to System.currentTimeMillis()
                    )
                    
                    val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
                    prefs.edit().putString("wallet_data", gson.toJson(walletData)).apply()
                    
                    showMainScreen()
                }
            }
        }
        
        layout.addView(title)
        layout.addView(seedInput)
        layout.addView(importBtn)
        
        setContentView(layout)
    }
    
    // ============ ГЛАВНЫЙ ЭКРАН ============
    private fun showMainScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(android.graphics.Color.parseColor("#000000"))
        }
        
        val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
        val walletData = gson.fromJson(prefs.getString("wallet_data", "{}"), Map::class.java)
        val address = walletData?.get("address") as? String ?: "0x..."
        
        val addressText = TextView(this).apply {
            text = address.substring(0, 6) + "..." + address.substring(address.length - 4)
            textSize = 14f
            setTextColor(android.graphics.Color.GRAY)
        }
        
        val balanceText = TextView(this).apply {
            text = "$0.00"
            textSize = 48f
            setTextColor(android.graphics.Color.WHITE)
        }
        
        // Кнопки действий
        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val sendBtn = Button(this).apply {
            text = "↑\nОтправить"
            setOnClickListener { Toast.makeText(this@MainActivity, "Скоро", Toast.LENGTH_SHORT).show() }
        }
        
        val receiveBtn = Button(this).apply {
            text = "↓\nПолучить"
            setOnClickListener { Toast.makeText(this@MainActivity, "Скоро", Toast.LENGTH_SHORT).show() }
        }
        
        val swapBtn = Button(this).apply {
            text = "⇄\nОбмен"
            setOnClickListener { Toast.makeText(this@MainActivity, "Скоро", Toast.LENGTH_SHORT).show() }
        }
        
        val buyBtn = Button(this).apply {
            text = "+\nКупить"
            setOnClickListener { Toast.makeText(this@MainActivity, "Скоро", Toast.LENGTH_SHORT).show() }
        }
        
        actionsRow.addView(sendBtn)
        actionsRow.addView(receiveBtn)
        actionsRow.addView(swapBtn)
        actionsRow.addView(buyBtn)
        
        // Токены
        val tokensList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        val btcCard = createTokenCard("Bitcoin", "0.0000 BTC", "$0.00")
        val ethCard = createTokenCard("Ethereum", "0.0000 ETH", "$0.00")
        val solCard = createTokenCard("Solana", "0.0000 SOL", "$0.00")
        
        tokensList.addView(btcCard)
        tokensList.addView(ethCard)
        tokensList.addView(solCard)
        
        val logoutBtn = Button(this).apply {
            text = "Выйти"
            setOnClickListener {
                prefs.edit().remove("wallet_data").apply()
                showLanguageScreen()
            }
        }
        
        layout.addView(addressText)
        layout.addView(balanceText)
        layout.addView(actionsRow)
        layout.addView(tokensList)
        layout.addView(logoutBtn)
        
        setContentView(layout)
    }
    
    // ============ КАРТОЧКА ТОКЕНА ============
    private fun createTokenCard(name: String, qty: String, usd: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"))
            
            val nameText = TextView(this@MainActivity).apply {
                text = name
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
            }
            
            val qtyText = TextView(this@MainActivity).apply {
                text = qty
                textSize = 13f
                setTextColor(android.graphics.Color.GRAY)
            }
            
            addView(nameText)
            addView(qtyText)
        }
    }
    
    // ============ ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ============
    private fun saveLanguage(lang: String) {
        val prefs = getSharedPreferences("galaxy_wallet", MODE_PRIVATE)
        prefs.edit().putString("language", lang).apply()
    }
    
    private fun generateAddress(): String {
        val chars = "0123456789abcdef"
        val random = SecureRandom()
        val sb = StringBuilder("0x")
        for (i in 0 until 40) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }
}
