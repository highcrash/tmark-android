package com.tmark.client.ui.screens.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tmark.client.data.model.InvoiceSummary
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun InvoicesScreen(
    onBack: () -> Unit,
    vm: InvoicesViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
        ScreenHeader(title = "Invoices", eyebrow = "Client Portal", onBack = onBack)

        when {
            state.loading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = vm::load)
            else -> {
                // Outstanding balance banner
                if (state.outstandingBalance > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A0A09))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OUTSTANDING BALANCE",
                                fontFamily = BarlowCondensed,
                                fontSize = 10.sp,
                                letterSpacing = 0.2.em,
                                color = TMarkMuted
                            )
                            Text(
                                text = "৳${"%,.0f".format(state.outstandingBalance)}",
                                fontFamily = BebasNeue,
                                fontSize = 24.sp,
                                color = TMarkRed
                            )
                        }
                    }
                }

                if (state.invoices.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("NO INVOICES YET", fontFamily = BebasNeue, fontSize = 28.sp, color = TMarkMuted)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.invoices) { inv ->
                            InvoiceCard(inv)
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceCard(inv: InvoiceSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inv.invoiceCode,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TMarkBlack
                )
                StatusBadge(inv.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = inv.order.projectName,
                fontFamily = Barlow,
                fontSize = 12.sp,
                color = TMarkMuted
            )
            Spacer(Modifier.height(8.dp))
            TMarkDivider()
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "৳${"%,.0f".format(inv.totalAmount)}",
                        fontFamily = BebasNeue,
                        fontSize = 22.sp,
                        color = TMarkBlack
                    )
                    Text(
                        text = "Issued: ${inv.issueDate.take(10)}",
                        fontFamily = Barlow,
                        fontSize = 11.sp,
                        color = TMarkMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val balText = if (inv.balanceDue <= 0) "PAID" else "Due: ৳${"%,.0f".format(inv.balanceDue)}"
                    Text(
                        text = balText,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (inv.balanceDue <= 0) Color(0xFF16A34A) else TMarkRed
                    )
                    if (inv.dueDate != null && inv.balanceDue > 0) {
                        Text(
                            text = "Due: ${inv.dueDate.take(10)}",
                            fontFamily = Barlow,
                            fontSize = 11.sp,
                            color = TMarkMuted
                        )
                    }
                }
            }
        }
    }
}
