package com.tmark.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.tmark.client.ui.theme.*

// ── Screen Header ─────────────────────────────────────────────────────────────

@Composable
fun ScreenHeader(
    title: String,
    eyebrow: String = "",
    onBack: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    compact: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TMarkBlack)
            // grid background
            .drawBehind {
                val step = 32.dp.toPx()
                val lineColor = Color.White.copy(alpha = 0.025f)
                var y = 0f
                while (y <= size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }
                var x = 0f
                while (x <= size.width) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), 1f)
                    x += step
                }
            }
    ) {
        // Red glow top-left
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset((-40).dp, (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TMarkRed.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = if (compact) 6.dp else 20.dp)
        ) {
            if (compact) {
                // Compact: back arrow + eyebrow on one line, then title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (onBack != null) {
                        Text(
                            text = "←",
                            color = TMarkRed,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable(onClick = onBack)
                                .padding(end = 10.dp)
                        )
                    }
                    if (eyebrow.isNotBlank()) {
                        Text(
                            text = eyebrow.uppercase(),
                            fontFamily = BarlowCondensed,
                            fontSize = 10.sp,
                            letterSpacing = 0.28.em,
                            color = TMarkMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    action?.invoke()
                }
                Text(
                    text = title,
                    fontFamily = BebasNeue,
                    fontSize = 24.sp,
                    color = Color.White
                )
            } else {
                // Full header: back arrow, eyebrow, then title + action
                if (onBack != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "←",
                            color = TMarkRed,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable(onClick = onBack)
                                .padding(end = 12.dp, bottom = 4.dp)
                        )
                    }
                }
                if (eyebrow.isNotBlank()) {
                    Text(
                        text = eyebrow.uppercase(),
                        fontFamily = BarlowCondensed,
                        fontSize = 10.sp,
                        letterSpacing = 0.28.em,
                        color = TMarkMuted
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontFamily = BebasNeue,
                        fontSize = 32.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    action?.invoke()
                }
            }
        }
    }
}

// ── Status Badge (outline style — matches mockup exactly) ─────────────────────

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status.lowercase()) {
        "pending"   -> Color(0xFFD97706)
        "confirmed" -> Color(0xFF16A34A)
        "declined"  -> TMarkRed
        "converted" -> Color(0xFF2563EB)
        "active"    -> Color(0xFFD97706)
        "completed", "done" -> Color(0xFF16A34A)
        "paid"      -> Color(0xFF16A34A)
        "sent"      -> Color(0xFF2563EB)
        "partial"   -> Color(0xFFD97706)
        "overdue"   -> TMarkRed
        else        -> TMarkMuted
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(1.dp, color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.uppercase(),
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            letterSpacing = 0.18.em,
            color = color
        )
    }
}

// ── Buttons ───────────────────────────────────────────────────────────────────

@Composable
fun TMarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth()
            .background(if (enabled) TMarkRed else TMarkMuted)
            .clickable(enabled = enabled && !loading, onClick = onClick)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(
                text = text,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.28.em,
                color = Color.White
            )
        }
    }
}

@Composable
fun TMarkOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TMarkBlack
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .border(1.dp, TMarkBorder)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 0.22.em,
            color = color
        )
    }
}

// ── Section Label ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        fontFamily = BarlowCondensed,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 0.2.em,
        color = TMarkMuted,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

// ── Divider ───────────────────────────────────────────────────────────────────

@Composable
fun TMarkDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.Black.copy(alpha = 0.05f))
    )
}

// ── Error / Loading ───────────────────────────────────────────────────────────

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Text(message, color = TMarkMuted, fontFamily = Barlow, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        TMarkOutlineButton("RETRY", onClick = onRetry, modifier = Modifier.width(160.dp), color = TMarkRed)
    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = TMarkRed, strokeWidth = 2.dp)
    }
}
