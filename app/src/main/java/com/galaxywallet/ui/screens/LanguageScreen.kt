package com.galaxywallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galaxywallet.ui.theme.*

@Composable
fun LanguageScreen(
    onLanguageSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Логотип
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(AccentDark, Accent)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Galaxy Wallet",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Мультивалютный кошелек",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопки языков
            LanguageButton("🇷🇺 Русский") { onLanguageSelected("ru") }
            Spacer(modifier = Modifier.height(12.dp))
            LanguageButton("🇬🇧 English") { onLanguageSelected("en") }
            Spacer(modifier = Modifier.height(12.dp))
            LanguageButton("🇯🇵 日本語") { onLanguageSelected("ja") }
        }
    }
}

@Composable
private fun LanguageButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Surface,
            contentColor = TextMain
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
