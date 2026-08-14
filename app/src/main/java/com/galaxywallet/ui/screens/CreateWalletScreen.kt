package com.galaxywallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galaxywallet.ui.theme.*

@Composable
fun CreateWalletScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Создание кошелька",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Придумайте пароль для шифрования",
            fontSize = 14.sp,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Пароль (мин. 8 символов)", color = TextSecondary) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = SurfaceHover,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                cursorColor = Accent
            ),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onCreated() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Background
            ),
            enabled = password.length >= 8
        ) {
            Text("Продолжить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
        ) {
            Text("Назад", fontSize = 16.sp)
        }
    }
}
