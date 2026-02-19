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
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.SwapVert
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

// Custom Colors
val BgDark = Color(0xFF101922)
val InputBg = Color(0xFF1A2632)
val PrimaryBlue = Color(0xFF137FEC)
val TextGray = Color(0xFF94A3B8)
val KeypadBg = Color(0xFF151E28)
val PillBg = Color(0xFF1E293B)
val GreenTrend = Color(0xFF22C55E)

// --- Formatting Helpers ---

/**
 * Formats a number using the Indian numbering system (lakhs, crores).
 * e.g. 1234567.89 -> "12,34,567.89"
 */
fun formatIndian(value: Double, showDecimal: Boolean): String {
    val intPart = value.toLong()
    val decPart = value - intPart

    // Format integer part with Indian grouping
    val intStr = intPart.toString()
    val formatted = StringBuilder()

    if (intStr.length <= 3) {
        formatted.append(intStr)
    } else {
        // Last 3 digits, then groups of 2
        val last3 = intStr.takeLast(3)
        val remaining = intStr.dropLast(3)
        val remainingFormatted = remaining.reversed().chunked(2).joinToString(",").reversed()
        formatted.append(remainingFormatted)
        formatted.append(",")
        formatted.append(last3)
    }

    return if (showDecimal && decPart != 0.0) {
        val dec = String.format(Locale.US, "%.2f", decPart).drop(2) // gets digits after "0."
        "$formatted.$dec"
    } else {
        formatted.toString()
    }
}

/**
 * Formats a number using the US/Western numbering system (thousands).
 * e.g. 1234567.89 -> "1,234,567.89"
 */
fun formatUS(value: Double, showDecimal: Boolean): String {
    return if (showDecimal && value % 1.0 != 0.0) {
        String.format(Locale.US, "%,.2f", value)
    } else {
        String.format(Locale.US, "%,.0f", value)
    }
}

/**
 * Formats the raw input string for display in the input box.
 * Uses Indian system when INR is the input (isUsdToInr == false),
 * and US system when USD is the input (isUsdToInr == true).
 */
fun formatInput(inputAmount: String, isUsdToInr: Boolean): String {
    if (inputAmount.isEmpty()) return "0"
    val parts = inputAmount.split(".")
    val intPartLong = parts[0].toLongOrNull() ?: return inputAmount

    val intFormatted = if (isUsdToInr) {
        // USD input -> US commas
        String.format(Locale.US, "%,d", intPartLong)
    } else {
        // INR input -> Indian commas
        val intStr = intPartLong.toString()
        if (intStr.length <= 3) {
            intStr
        } else {
            val last3 = intStr.takeLast(3)
            val remaining = intStr.dropLast(3)
            val remainingFormatted = remaining.reversed().chunked(2).joinToString(",").reversed()
            "$remainingFormatted,$last3"
        }
    }

    return if (parts.size > 1) "$intFormatted.${parts[1]}" else intFormatted
}

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

    var inputAmount by remember { mutableStateOf("1") }
    var currentRate by remember { mutableStateOf(91.16) }
    var isUsdToInr by remember { mutableStateOf(true) }

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
        // --- TOP SECTION ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                Text(
                    text = if (isUsdToInr) "$" else "₹",
                    color = TextGray,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Use correct comma system for input display
                val formattedInput = formatInput(inputAmount, isUsdToInr)

                Text(
                    text = formattedInput,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    text = if (isUsdToInr) "USD" else "INR",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            // Swap Button
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(TextGray.copy(alpha = 0.3f)))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(InputBg)
                    .clickable { isUsdToInr = !isUsdToInr },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapVert,
                    contentDescription = "Swap Currencies",
                    tint = TextGray,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(TextGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.height(16.dp))

            // Result calculation
            val inputValue = inputAmount.toDoubleOrNull() ?: 0.0
            val convertedAmount = if (isUsdToInr) inputValue * currentRate else inputValue / currentRate
            val hasDecimal = convertedAmount % 1.0 != 0.0

            // Use Indian commas for INR output (USD->INR), US commas for USD output (INR->USD)
            val formattedConverted = if (isUsdToInr) {
                formatIndian(convertedAmount, hasDecimal)
            } else {
                formatUS(convertedAmount, hasDecimal)
            }

            val dynamicFontSize = if (formattedConverted.length > 10) 44.sp else 64.sp

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (isUsdToInr) "₹ " else "$ ",
                        color = TextGray,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = if (formattedConverted.length > 10) 4.dp else 8.dp)
                    )
                    Text(
                        text = formattedConverted,
                        color = Color.White,
                        fontSize = dynamicFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isUsdToInr) "Indian Rupee" else "US Dollar",
                color = PrimaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .background(PillBg, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pillText = if (isUsdToInr) {
                    val formattedRate = String.format(Locale.US, "%,.2f", currentRate)
                    "1 USD ≈ ₹ $formattedRate"
                } else {
                    val invertedRate = 1.0 / currentRate
                    val formattedRate = String.format(Locale.US, "%,.4f", invertedRate)
                    "1 INR ≈ $ $formattedRate"
                }

                Text(pillText, color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = "Trend Up",
                    tint = GreenTrend,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // --- KEYPAD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KeypadBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(bottom = 32.dp, top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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