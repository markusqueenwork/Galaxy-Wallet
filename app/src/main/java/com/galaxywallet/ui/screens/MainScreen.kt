package com.galaxywallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galaxywallet.ui.theme.*

data class Token(
    val name: String,
    val symbol: String,
    val network: String,
    val amount: Double,
    val usdValue: Double,
    val iconColor: Color
)

@Composable
fun MainScreen(
    address: String,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onSwap: () -> Unit,
    onBuy: () -> Unit,
    onLogout: () -> Unit
) {
    var balanceHidden by remember { mutableStateOf(false) }
    
    val tokens = listOf(
        Token("Bitcoin", "BTC", "Bitcoin", 0.0, 0.0, BitcoinOrange),
        Token("Ethereum", "ETH", "Base", 0.0, 0.0, EthereumBlue),
        Token("Solana", "SOL", "Solana", 0.0, 0.0, SolanaGreen),
        Token("Tron", "TRX", "Tron", 0.0, 0.0, TronRed),
        Token("TON", "TON", "TON", 0.0, 0.0, TonBlue),
        Token("Sui", "SUI", "Sui", 0.0, 0.0, SuiBlue)
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp)
    ) {
        // Верхняя навигация
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillButton("Аккаунт 1", false) {}
                PillButton("Главная", true) {}
                PillButton("Торговля", false) {}
                PillButton("NFT", false) {}
                PillButton("Активность", false) {}
            }
        }
        
        // Адрес
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Аккаунт 1",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TextMain)
                        .clickable { balanceHidden = !balanceHidden },
                    contentAlignment = Alignment.Center
                ) {
                    Text("•", fontSize = 18.sp, color = Background)
                }
            }
        }
        
        // Баланс
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (balanceHidden) "••••••" else "$0.00",
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+$0.00",
                    fontSize = 15.sp,
                    color = Green,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .background(Green, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("+0.00%", fontSize = 13.sp, color = Background, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Кнопки действий
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton("↑", "Отправить", onSend)
                ActionButton("↓", "Получить", onReceive)
                ActionButton("⇄", "Конвертация", onSwap)
                ActionButton("+", "Купить", onBuy)
            }
        }
        
        // Заголовок токенов
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Токены",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )
                Text("›", fontSize = 18.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Список токенов
        items(tokens) { token ->
            TokenCard(token)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Сети
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Сети",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        items(listOf("Bitcoin", "Ethereum", "Solana", "Tron", "TON", "Sui")) { network ->
            NetworkCard(network)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Выход
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Surface,
                    contentColor = Red
                )
            ) {
                Text("Выйти", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (active) Accent else Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) Background else TextSecondary
        )
    }
}

@Composable
private fun ActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Surface, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp, color = Accent)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
private fun TokenCard(token: Token) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(token.iconColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = token.symbol.take(1),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(token.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
            Text(
                "${token.amount.toFixed(4)} ${token.symbol} · ${token.network}",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Text(
            "$${token.usdValue.toFixed(2)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMain
        )
    }
}

@Composable
private fun NetworkCard(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
        Spacer(modifier = Modifier.weight(1f))
        Text("Подключено", fontSize = 14.sp, color = Green)
    }
}

fun Double.toFixed(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
