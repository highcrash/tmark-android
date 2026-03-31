package com.tmark.client.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.data.model.OrderSummary
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    vm: OrdersViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
        ScreenHeader(title = "My Orders", eyebrow = "Client Portal")

        when {
            state.loading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = vm::load)
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    // Financial Summary
                    if (state.orders.isNotEmpty()) {
                        val total = state.orders.sumOf { it.totalAmount }
                        val outstanding = state.orders.sumOf { it.balanceDue }
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 20.dp)
                            ) {
                                Text("FINANCIAL SUMMARY", fontFamily = BarlowCondensed, fontSize = 10.sp,
                                    letterSpacing = 0.28.em, color = TMarkMuted,
                                    modifier = Modifier.padding(bottom = 12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryStat("Total Orders", state.orders.size.toString())
                                    SummaryStat("Total Value", "৳${"%,.0f".format(total)}")
                                    SummaryStat("Outstanding", "৳${"%,.0f".format(outstanding)}", danger = outstanding > 0)
                                }
                            }
                        }
                        item {
                            Text("ORDERS", fontFamily = BarlowCondensed, fontSize = 10.sp,
                                letterSpacing = 0.28.em, color = TMarkMuted,
                                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp))
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
}

@Composable
private fun SummaryStat(label: String, value: String, danger: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = BarlowCondensed, fontSize = 9.sp,
            letterSpacing = 0.15.em, color = TMarkMuted)
        Text(value, fontFamily = BebasNeue, fontSize = 20.sp,
            color = if (danger) TMarkRed else TMarkBlack)
    }
}

@Composable
private fun OrderRow(order: OrderSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
        // Amount + balance
        Column(horizontalAlignment = Alignment.End) {
            Text("৳${"%,.0f".format(order.totalAmount)}", fontFamily = BebasNeue,
                fontSize = 18.sp, color = TMarkBlack)
            val (balLabel, balColor) = if (order.balanceDue <= 0)
                "Paid" to Color(0xFF16A34A)
            else
                "Due ৳${"%,.0f".format(order.balanceDue)}" to TMarkRed
            Text(balLabel, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, color = balColor)
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Outlined.ChevronRight, null, tint = TMarkMuted, modifier = Modifier.size(18.dp))
    }
    TMarkDivider()
}
