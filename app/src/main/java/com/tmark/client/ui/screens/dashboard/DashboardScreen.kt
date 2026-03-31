package com.tmark.client.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.data.local.TokenStore
import com.tmark.client.data.model.LastInvoice
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun DashboardScreen(
    onNewRequest: () -> Unit,
    onOrders: () -> Unit,
    onInvoices: () -> Unit,
    tokenStore: TokenStore,
    vm: DashboardViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val clientName by tokenStore.clientName.collectAsState(initial = "")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TMarkOffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Dark Header with glow + grid ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TMarkBlack)
                .drawBehind {
                    val step = 32.dp.toPx()
                    val lc = Color.White.copy(alpha = 0.025f)
                    var y = 0f; while (y <= size.height) { drawLine(lc, Offset(0f, y), Offset(size.width, y), 1f); y += step }
                    var x = 0f; while (x <= size.width) { drawLine(lc, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
                }
        ) {
            // Glow
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset((-40).dp, (-40).dp)
                    .background(Brush.radialGradient(listOf(TMarkRed.copy(alpha = 0.18f), Color.Transparent)))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "CLIENT PORTAL",
                    fontFamily = BarlowCondensed,
                    fontSize = 10.sp,
                    letterSpacing = 0.28.em,
                    color = TMarkMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Welcome back,",
                    fontFamily = Barlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.7f)
                )
                val parts = (clientName ?: "").trim().split(" ")
                Row {
                    if (parts.size >= 2) {
                        Text(parts.dropLast(1).joinToString(" ") + " ", fontFamily = BebasNeue, fontSize = 28.sp, color = Color.White)
                        Text(parts.last(), fontFamily = BebasNeue, fontSize = 28.sp, color = TMarkRed)
                    } else {
                        Text(clientName ?: "", fontFamily = BebasNeue, fontSize = 28.sp, color = Color.White)
                    }
                }
                Text(
                    text = "Here's your account overview.",
                    fontFamily = Barlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = TMarkMuted
                )
            }
        }

        when {
            state.loading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = vm::load)
            else -> {
                val d = state.data!!

                // ── Stats ─────────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text("OVERVIEW", fontFamily = BarlowCondensed, fontSize = 10.sp,
                        letterSpacing = 0.28.em, color = TMarkMuted,
                        modifier = Modifier.padding(bottom = 10.dp))

                    StatCard("Pending Requests", d.pendingRequests.toString(),
                        "awaiting confirmation", redBar = false)
                    Spacer(Modifier.height(1.dp))
                    StatCard("Active Orders", d.activeOrders.toString(),
                        "currently in progress", redBar = false)
                    Spacer(Modifier.height(1.dp))
                    StatCard("Outstanding Balance",
                        if (d.outstandingBalance > 0) "৳${"%,.0f".format(d.outstandingBalance)}" else "৳0",
                        "payment due", redBar = true, valueDanger = d.outstandingBalance > 0)
                }

                // ── Quick Actions ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("QUICK ACTIONS", fontFamily = BarlowCondensed, fontSize = 10.sp,
                        letterSpacing = 0.28.em, color = TMarkMuted,
                        modifier = Modifier.padding(bottom = 2.dp))
                    TMarkButton(text = "+ NEW RENTAL REQUEST", onClick = onNewRequest)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TMarkOutlineButton("MY ORDERS", onClick = onOrders, modifier = Modifier.weight(1f))
                        TMarkOutlineButton("MY INVOICES", onClick = onInvoices, modifier = Modifier.weight(1f))
                    }
                }

                // ── Latest Invoice ────────────────────────────────────────────
                d.lastInvoice?.let { inv ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Text("LATEST INVOICE", fontFamily = BarlowCondensed, fontSize = 10.sp,
                            letterSpacing = 0.28.em, color = TMarkMuted,
                            modifier = Modifier.padding(bottom = 10.dp))
                        InvoiceCard(inv, onClick = onInvoices)
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String, value: String, sub: String,
    redBar: Boolean, valueDanger: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111110))
            .border(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .matchParentSize()
                .background(if (redBar) TMarkRed else Color.White.copy(alpha = 0.2f))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 20.dp, bottom = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                fontFamily = BarlowCondensed,
                fontSize = 9.sp,
                letterSpacing = 0.3.em,
                color = Color.White.copy(alpha = 0.45f)
            )
            Text(
                text = value,
                fontFamily = BebasNeue,
                fontSize = 48.sp,
                color = if (valueDanger) TMarkRed else Color.White,
                lineHeight = 48.sp
            )
            Text(
                text = sub,
                fontFamily = Barlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun InvoiceCard(inv: LastInvoice, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, TMarkBorder)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(inv.invoiceCode, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, color = TMarkBlack)
            StatusBadge(inv.status)
        }
        Spacer(Modifier.height(4.dp))
        Text(inv.issueDate.take(10), fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
        Text("৳${"%,.0f".format(inv.totalAmount)}", fontFamily = BebasNeue, fontSize = 26.sp, color = TMarkBlack)
        TMarkDivider()
        Spacer(Modifier.height(10.dp))
        Text(
            text = "View All Invoices →",
            fontFamily = Barlow,
            fontSize = 12.sp,
            color = TMarkRed,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}
