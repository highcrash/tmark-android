package com.tmark.client.ui.screens.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
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
import com.tmark.client.data.model.RentalRequest
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun RequestsScreen(
    onNewRequest: () -> Unit,
    vm: RequestsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
        ScreenHeader(
            title = "My Requests",
            eyebrow = "Client Portal",
            action = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(TMarkRed)
                        .clickable(onClick = onNewRequest)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("+ NEW", fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold,
                        fontSize = 11.sp, letterSpacing = 0.2.em, color = Color.White)
                }
            }
        )

        when {
            state.loading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = vm::load)
            state.requests.isEmpty() -> EmptyRequests(onNewRequest)
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    items(state.requests) { req ->
                        RequestRow(
                            req = req,
                            cancelling = state.cancelling == req.id,
                            onCancel = { vm.cancel(req.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RequestRow(req: RentalRequest, cancelling: Boolean, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top: project name + badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = req.projectName,
                fontFamily = BebasNeue,
                fontSize = 16.sp,
                color = TMarkBlack,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            StatusBadge(req.status)
        }

        // Admin message
        if (!req.adminMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD97706).copy(alpha = 0.3f))
                    .background(Color(0xFFFFFBEB))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(req.adminMessage, fontFamily = Barlow, fontSize = 11.sp, color = Color(0xFF92400E))
            }
        }

        // Date + amount row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (req.requestedDates.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.CalendarToday, null,
                        modifier = Modifier.size(13.dp), tint = TMarkMuted)
                    val dateLabel = if (req.requestedDates.size == 1)
                        req.requestedDates.first()
                    else
                        "${req.requestedDates.first()} – ${req.requestedDates.last()} · ${req.requestedDates.size} days"
                    Text(dateLabel, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
                }
            }
            Text(
                text = "৳${"%,.0f".format(req.estimatedTotal)}",
                fontFamily = BebasNeue,
                fontSize = 18.sp,
                color = TMarkBlack
            )
        }

        // Bottom: submitted + cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Submitted ${req.createdAt.take(10)}",
                fontFamily = Barlow,
                fontSize = 10.sp,
                color = TMarkMuted
            )
            if (req.status == "pending") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .border(1.dp, TMarkRed.copy(alpha = 0.3f))
                        .clickable(enabled = !cancelling) { onCancel() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (cancelling) "…" else "Cancel",
                        fontFamily = BarlowCondensed,
                        fontSize = 9.sp,
                        color = TMarkRed
                    )
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
            fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        TMarkButton("+ NEW REQUEST", onClick = onNewRequest, modifier = Modifier.width(200.dp))
    }
}
