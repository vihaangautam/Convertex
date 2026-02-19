package com.example.contertex

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Colors
val BgDark = Color(0xFF101922)
val InputBg = Color(0xFF1A2632)
val PrimaryBlue = Color(0xFF137FEC)
val TextGray = Color(0xFF94A3B8)
val KeypadBg = Color(0xFF151E28)
val PillBg = Color(0xFF1E293B)
val GreenTrend = Color(0xFF22C55E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    ContertexApp()
                }
            }
        }
    }
}

@Composable
fun ContertexApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var inputAmount by remember { mutableStateOf("1") }
    var currentRate by remember { mutableStateOf(91.16) }

    // Fetch rate logic
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("ContertexPrefs", Context.MODE_PRIVATE)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("last_fetch_date", "")

        currentRate = prefs.getFloat("saved_rate", 91.16f).toDouble()

        if (savedDate != todayDate) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val apiUrl = "https://open.er-api.com/v6/latest/USD"
                    val response = URL(apiUrl).readText()
                    val newRate = JSONObject(response).getJSONObject("rates").getDouble("INR")

                    withContext(Dispatchers.Main) {
                        currentRate = newRate
                        prefs.edit()
                            .putString("last_fetch_date", todayDate)
                            .putFloat("saved_rate", newRate.toFloat())
                            .apply()
                        Toast.makeText(context, "Rate updated: ₹$newRate", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Ignore, fallback to saved rate
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- TOP SECTION (UI) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text("Convertex", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Enter Amount", color = TextGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AttachMoney,
                    contentDescription = "USD Icon",
                    tint = TextGray,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Format the input text with commas for display
                val formattedInput = if (inputAmount.isEmpty()) "0" else {
                    val parts = inputAmount.split(".")
                    val intPart = parts[0].toLongOrNull()?.let {
                        String.format(Locale.getDefault(), "%,d", it)
                    } ?: parts[0]
                    if (parts.size > 1) "$intPart.${parts[1]}" else intPart
                }

                Text(
                    text = formattedInput,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text("USD", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd))
            }

            // Connector Arrow
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(TextGray.copy(alpha = 0.3f)))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(InputBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Convert Arrow",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(TextGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(16.dp))

            // Result Area
            val inrAmount = (inputAmount.toDoubleOrNull() ?: 0.0) * currentRate

            // Added commas (%,.0f and %,.2f) to the format strings!
            val formattedInr = if (inrAmount % 1.0 == 0.0) {
                String.format(Locale.getDefault(), "%,.0f", inrAmount)
            } else {
                String.format(Locale.getDefault(), "%,.2f", inrAmount)
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text("₹ ", color = TextGray, fontSize = 28.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                Text(formattedInr, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Indian Rupee", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .background(PillBg, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Formatting the pill rate to 2 decimals with commas
                val formattedRate = String.format(Locale.getDefault(), "%,.2f", currentRate)
                Text("1 USD ≈ ₹ $formattedRate", color = TextGray, fontSize = 12.sp)

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = "Trend Up",
                    tint = GreenTrend,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // --- BOTTOM SECTION (KEYPAD) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeypadBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(bottom = 32.dp, top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Drag handle placeholder
                Box(modifier = Modifier.width(40.dp).height(4.dp).background(PillBg, CircleShape))
                Spacer(modifier = Modifier.height(24.dp))

                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "⌫")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        when (key) {
                                            "⌫" -> if (inputAmount.isNotEmpty()) inputAmount = inputAmount.dropLast(1)
                                            "." -> if (!inputAmount.contains(".")) inputAmount += "."
                                            else -> if (inputAmount.length < 12) {
                                                if (inputAmount == "0") inputAmount = key else inputAmount += key
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "⌫") {
                                    Icon(
                                        imageVector = Icons.Rounded.Backspace,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}