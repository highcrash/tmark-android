package com.tmark.client.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.data.model.SelectedEquipment
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    vm: CartViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()
    val list = items.values.toList()
    val totalPerDay = list.sumOf { it.pricePerDay * it.quantity }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
            // Header
            Box(
                Modifier.fillMaxWidth().background(TMarkBlack)
                    .drawBehind {
                        val step = 32.dp.toPx(); val lc = Color.White.copy(alpha = 0.025f)
                        var y = 0f; while (y <= size.height) { drawLine(lc, Offset(0f, y), Offset(size.width, y), 1f); y += step }
                        var x = 0f; while (x <= size.width) { drawLine(lc, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
                    }
            ) {
                Box(Modifier.size(180.dp).offset((-30).dp, (-30).dp)
                    .background(Brush.radialGradient(listOf(TMarkRed.copy(alpha = 0.15f), Color.Transparent))))
                Column(
                    Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
                ) {
                    Text("←", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier.clickable(onClick = onBack).padding(bottom = 4.dp))
                    Text("CLIENT PORTAL", fontFamily = BarlowCondensed, fontSize = 10.sp,
                        letterSpacing = 0.28.em, color = TMarkMuted)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Request Basket", fontFamily = BebasNeue, fontSize = 28.sp, color = Color.White)
                        if (list.isNotEmpty()) {
                            Text("${list.sumOf { it.quantity }} item${if (list.sumOf { it.quantity } != 1) "s" else ""}",
                                fontFamily = BarlowCondensed, fontSize = 11.sp, letterSpacing = 0.15.em, color = TMarkMuted)
                        }
                    }
                }
            }

            if (list.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("BASKET EMPTY", fontFamily = BebasNeue, fontSize = 32.sp, color = TMarkMuted)
                    Spacer(Modifier.height(8.dp))
                    Text("Add packages or items from Browse.",
                        fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    TMarkOutlineButton("← BACK TO BROWSE", onClick = onBack, modifier = Modifier.width(220.dp), color = TMarkRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("SELECTED EQUIPMENT", fontFamily = BarlowCondensed, fontSize = 9.sp,
                            letterSpacing = 0.28.em, color = TMarkMuted)
                    }
                    items(list, key = { it.entityId }) { eq ->
                        CartItemRow(
                            eq = eq,
                            onIncrement = { vm.increment(eq) },
                            onDecrement = { vm.decrement(eq.entityId) },
                            onRemove = { vm.remove(eq.entityId) }
                        )
                    }
                    item { Spacer(Modifier.height(4.dp)) }
                    // Total per day banner
                    item {
                        Row(
                            Modifier.fillMaxWidth().background(TMarkBlack)
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL PER DAY", fontFamily = BarlowCondensed, fontSize = 11.sp,
                                letterSpacing = 0.2.em, color = TMarkMuted)
                            Text("৳${"%,.0f".format(totalPerDay)}", fontFamily = BebasNeue,
                                fontSize = 28.sp, color = TMarkRed)
                        }
                    }
                    item {
                        Text("Final price depends on number of days selected.",
                            fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }

                // Sticky bottom CTA
                Box(
                    Modifier.fillMaxWidth().background(Color.White)
                        .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    TMarkButton("SCHEDULE DATES →", onClick = onContinue)
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    eq: SelectedEquipment,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val atMax = eq.quantity >= eq.maxQtyPerDay

    Row(
        Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(eq.type.uppercase(), fontFamily = BarlowCondensed, fontSize = 8.sp,
                letterSpacing = 0.2.em, color = TMarkMuted)
            Text(eq.name, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, color = TMarkBlack)
            Text("৳${"%,.0f".format(eq.pricePerDay)}/day", fontFamily = Barlow,
                fontSize = 11.sp, color = TMarkMuted)
        }
        Spacer(Modifier.width(8.dp))
        // Qty controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).border(1.dp, TMarkBorder).clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Text("−", fontSize = 16.sp, color = TMarkBlack, textAlign = TextAlign.Center)
            }
            Text(eq.quantity.toString(), fontFamily = BebasNeue, fontSize = 18.sp,
                modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, color = TMarkBlack)
            Box(
                Modifier.size(32.dp)
                    .background(if (atMax) TMarkBorder else TMarkRed)
                    .clickable(enabled = !atMax, onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.width(8.dp))
        // Remove
        Icon(Icons.Outlined.Close, "Remove", tint = TMarkMuted,
            modifier = Modifier.size(18.dp).clickable(onClick = onRemove))
    }
    TMarkDivider()
}
