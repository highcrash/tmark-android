package com.tmark.client.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
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
import com.tmark.client.data.model.OrderSummary
import com.tmark.client.data.model.RentalRequest
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*
import com.tmark.client.ui.screens.requests.RequestsViewModel

@Composable
fun OrdersTabScreen(
    onOrderClick: (String) -> Unit,
    onNewRequest: () -> Unit,
    ordersVm: OrdersViewModel = hiltViewModel(),
    requestsVm: RequestsViewModel = hiltViewModel()
) {
    val ordersState by ordersVm.ui.collectAsState()
    val requestsState by requestsVm.ui.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
        // Combined header with tabs
        Box(
            modifier = Modifier.fillMaxWidth().background(TMarkBlack)
                .drawBehind {
                    val step = 32.dp.toPx(); val lc = Color.White.copy(alpha = 0.025f)
                    var y = 0f; while (y <= size.height) { drawLine(lc, Offset(0f, y), Offset(size.width, y), 1f); y += step }
                    var x = 0f; while (x <= size.width) { drawLine(lc, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
                }
        ) {
            Box(
                Modifier.size(200.dp).offset((-40).dp, (-40).dp)
                    .background(Brush.radialGradient(listOf(TMarkRed.copy(alpha = 0.18f), Color.Transparent)))
            )
            Column(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 0.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("CLIENT PORTAL", fontFamily = BarlowCondensed, fontSize = 10.sp,
                            letterSpacing = 0.28.em, color = TMarkMuted)
                        Text("My Activity", fontFamily = BebasNeue, fontSize = 26.sp, color = Color.White)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.background(TMarkRed).clickable(onClick = onNewRequest)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("+ NEW REQUEST", fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold,
                            fontSize = 10.sp, letterSpacing = 0.2.em, color = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Tab row
                Row(Modifier.fillMaxWidth()) {
                    listOf("My Orders", "My Requests").forEachIndexed { i, label ->
                        val active = selectedTab == i
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).clickable { selectedTab = i }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(label.uppercase(), fontFamily = BarlowCondensed,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp, letterSpacing = 0.15.em,
                                color = if (active) Color.White else TMarkMuted)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.fillMaxWidth().height(2.dp)
                                .background(if (active) TMarkRed else Color.Transparent))
                        }
                    }
                }
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> OrdersTabContent(ordersState, onOrderClick, ordersVm::load)
            1 -> RequestsTabContent(requestsState, onNewRequest, requestsVm::load, requestsVm::cancel)
        }
    }
}

// ── Orders Tab Content ────────────────────────────────────────────────────────

@Composable
private fun OrdersTabContent(
    state: OrdersUiState,
    onOrderClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error!!, onRetry = onRetry)
        state.orders.isEmpty() -> EmptyOrders()
        else -> {
            LazyColumn(Modifier.fillMaxSize()) {
                val total = state.orders.sumOf { it.totalAmount }
                val outstanding = state.orders.sumOf { it.balanceDue }
                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text("FINANCIAL SUMMARY", fontFamily = BarlowCondensed, fontSize = 10.sp,
                            letterSpacing = 0.28.em, color = TMarkMuted,
                            modifier = Modifier.padding(bottom = 10.dp))
                        Row(
                            Modifier.fillMaxWidth().background(Color.White)
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryStat("Total", state.orders.size.toString())
                            SummaryStat("Value", "৳${"%,.0f".format(total)}")
                            SummaryStat("Outstanding", "৳${"%,.0f".format(outstanding)}", danger = outstanding > 0)
                        }
                    }
                }
                items(state.orders) { order ->
                    OrderRow(order, onClick = { onOrderClick(order.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, danger: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.15.em, color = TMarkMuted)
        Text(value, fontFamily = BebasNeue, fontSize = 20.sp, color = if (danger) TMarkRed else TMarkBlack)
    }
}

@Composable
private fun OrderRow(order: OrderSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(order.orderCode, fontFamily = BebasNeue, fontSize = 14.sp, color = TMarkRed)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(order.projectName, fontFamily = Barlow, fontSize = 13.sp, color = TMarkBlack,
                    modifier = Modifier.weight(1f, fill = false))
                StatusBadge(order.status)
            }
            val activeDates = order.dates.filter { !it.cancelled }.map { it.date }.sorted()
            if (activeDates.isNotEmpty()) {
                val dateLabel = if (activeDates.size == 1) activeDates.first()
                    else "${activeDates.first()} – ${activeDates.last()}"
                Text(dateLabel, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("৳${"%,.0f".format(order.totalAmount)}", fontFamily = BebasNeue,
                fontSize = 18.sp, color = TMarkBlack)
            val (balLabel, balColor) = if (order.balanceDue <= 0)
                "Paid" to Color(0xFF16A34A) else "Due ৳${"%,.0f".format(order.balanceDue)}" to TMarkRed
            Text(balLabel, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, color = balColor)
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Outlined.ChevronRight, null, tint = TMarkMuted, modifier = Modifier.size(18.dp))
    }
    TMarkDivider()
}

@Composable
private fun EmptyOrders() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Text("NO ORDERS YET", fontFamily = BebasNeue, fontSize = 28.sp, color = TMarkMuted)
        Spacer(Modifier.height(8.dp))
        Text("Your confirmed orders will appear here.",
            fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted, textAlign = TextAlign.Center)
    }
}

// ── Requests Tab Content ──────────────────────────────────────────────────────

@Composable
private fun RequestsTabContent(
    state: com.tmark.client.ui.screens.requests.RequestsUiState,
    onNewRequest: () -> Unit,
    onRetry: () -> Unit,
    onCancel: (String) -> Unit
) {
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error!!, onRetry = onRetry)
        state.requests.isEmpty() -> EmptyRequests(onNewRequest)
        else -> {
            LazyColumn(Modifier.fillMaxSize().background(Color.White)) {
                items(state.requests) { req ->
                    RequestRow(req, cancelling = state.cancelling == req.id, onCancel = { onCancel(req.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RequestRow(req: RentalRequest, cancelling: Boolean, onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(req.projectName, fontFamily = BebasNeue, fontSize = 16.sp, color = TMarkBlack,
                modifier = Modifier.weight(1f).padding(end = 8.dp))
            StatusBadge(req.status)
        }
        if (!req.adminMessage.isNullOrBlank()) {
            Box(
                Modifier.fillMaxWidth().border(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                    .background(Color(0xFFFFFBEB)).padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(req.adminMessage, fontFamily = Barlow, fontSize = 11.sp, color = Color(0xFF92400E))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (req.requestedDates.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(13.dp), tint = TMarkMuted)
                    val dateLabel = if (req.requestedDates.size == 1) req.requestedDates.first()
                        else "${req.requestedDates.first()} – ${req.requestedDates.last()} · ${req.requestedDates.size} days"
                    Text(dateLabel, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
                }
            }
            Text("৳${"%,.0f".format(req.estimatedTotal)}", fontFamily = BebasNeue, fontSize = 18.sp, color = TMarkBlack)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Submitted ${req.createdAt.take(10)}", fontFamily = Barlow, fontSize = 10.sp, color = TMarkMuted)
            if (req.status == "pending") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.border(1.dp, TMarkRed.copy(alpha = 0.3f))
                        .clickable(enabled = !cancelling) { onCancel() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (cancelling) "…" else "Cancel", fontFamily = BarlowCondensed,
                        fontSize = 9.sp, color = TMarkRed)
                }
            }
        }
    }
    TMarkDivider()
}

@Composable
private fun EmptyRequests(onNewRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Text("NO REQUESTS YET", fontFamily = BebasNeue, fontSize = 28.sp, color = TMarkMuted)
        Spacer(Modifier.height(8.dp))
        Text("Browse our catalog and submit a rental request.",
            fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        TMarkButton("+ NEW REQUEST", onClick = onNewRequest, modifier = Modifier.width(200.dp))
    }
}
