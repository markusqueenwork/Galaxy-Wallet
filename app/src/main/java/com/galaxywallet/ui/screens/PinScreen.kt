package com.galaxywallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galaxywallet.ui.theme.*

@Composable
fun PinScreen(
    title: String,
    onPinComplete: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Для быстрого доступа",
            fontSize = 13.sp,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Точки PIN
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = if (index < pin.length) Accent else SurfaceHover,
                            shape = CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Цифровая клавиатура
        val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            keys.chunked(3).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(70.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(Surface, CircleShape)
                                    .clickable {
                                        when(key) {
                                            "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                            else -> {
                                                if (pin.length < 4) {
                                                    pin += key
                                                    if (pin.length == 4) {
                                                        onPinComplete(pin)
                                                        pin = ""
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMain
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
