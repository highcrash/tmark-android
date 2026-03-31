package com.tmark.client.ui.screens.cart

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

@Composable
fun CartCalendarScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    vm: CartViewModel = hiltViewModel()
) {
    val items by vm.items.collectAsState()
    val savedDates by vm.selectedDates.collectAsState()

    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.of(today.year, today.monthValue)) }
    var selectedDates by remember(savedDates) { mutableStateOf(savedDates.toMutableList()) }

    val totalPerDay = items.values.sumOf { it.pricePerDay * it.quantity }
    val estimatedTotal = totalPerDay * selectedDates.size

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
                Text("REQUEST BASKET", fontFamily = BarlowCondensed, fontSize = 10.sp,
                    letterSpacing = 0.28.em, color = TMarkMuted)
                Text("Select Shoot Dates", fontFamily = BebasNeue, fontSize = 28.sp, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Pick the dates you need the equipment.",
                    fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar card
            Column(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                // Month navigation
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("←", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier
                            .clickable(enabled = month > YearMonth.now()) { month = month.minusMonths(1) }
                            .padding(8.dp))
                    Text(
                        "${month.month.getDisplayName(JTextStyle.FULL, Locale.getDefault())} ${month.year}",
                        fontFamily = BebasNeue, fontSize = 22.sp, color = TMarkBlack,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                    )
                    Text("→", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier.clickable { month = month.plusMonths(1) }.padding(8.dp))
                }
                Spacer(Modifier.height(8.dp))
                CalendarGrid(
                    yearMonth = month,
                    selectedDates = selectedDates,
                    today = today,
                    onDayClick = { day ->
                        val dateStr = "%04d-%02d-%02d".format(month.year, month.monthValue, day)
                        val updated = selectedDates.toMutableList()
                        if (updated.contains(dateStr)) updated.remove(dateStr) else updated.add(dateStr)
                        selectedDates = updated.sorted().toMutableList()
                    }
                )
            }

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(TMarkRed, "Selected")
                LegendDot(TMarkRed.copy(alpha = 0.15f), "Today")
            }

            // Selection summary
            Text(
                text = if (selectedDates.isEmpty()) "No dates selected"
                       else "${selectedDates.size} day(s) selected",
                fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = if (selectedDates.isEmpty()) TMarkMuted else TMarkBlack
            )

            Spacer(Modifier.height(100.dp))
        }

        // Sticky bottom
        Column(
            Modifier.fillMaxWidth().background(Color.White)
                .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Est. total bar
            Row(
                Modifier.fillMaxWidth().background(TMarkBlack).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ESTIMATED TOTAL", fontFamily = BarlowCondensed, fontSize = 9.sp,
                        letterSpacing = 0.2.em, color = TMarkMuted)
                    if (selectedDates.isNotEmpty()) {
                        Text("${selectedDates.size} days × ৳${"%,.0f".format(totalPerDay)}/day",
                            fontFamily = Barlow, fontSize = 10.sp, color = TMarkMuted)
                    }
                }
                Text(
                    text = if (selectedDates.isEmpty()) "—" else "৳${"%,.0f".format(estimatedTotal)}",
                    fontFamily = BebasNeue, fontSize = 28.sp, color = TMarkRed
                )
            }
            TMarkButton(
                text = "CONTINUE TO REQUEST →",
                onClick = {
                    vm.setDates(selectedDates)
                    onContinue()
                },
                enabled = selectedDates.isNotEmpty()
            )
        }
    }
}

@Composable
private fun CalendarGrid(yearMonth: YearMonth, selectedDates: List<String>, today: LocalDate, onDayClick: (Int) -> Unit) {
    val firstDay = yearMonth.atDay(1).dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { d ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(d, fontFamily = BarlowCondensed, fontSize = 10.sp, color = TMarkMuted)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        var dayCount = 1
        val rows = (firstDay + daysInMonth + 6) / 7
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    if (cellIndex < firstDay || dayCount > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = dayCount
                        val dateStr = "%04d-%02d-%02d".format(yearMonth.year, yearMonth.monthValue, day)
                        val isSelected = selectedDates.contains(dateStr)
                        val isToday = yearMonth.year == today.year && yearMonth.monthValue == today.monthValue && day == today.dayOfMonth
                        val isPast = yearMonth.atDay(day).isBefore(today)
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                .background(when { isSelected -> TMarkRed; isToday -> TMarkRed.copy(alpha = 0.15f); else -> Color.Transparent })
                                .clickable(enabled = !isPast) { onDayClick(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.toString(), fontFamily = Barlow, fontSize = 13.sp,
                                color = when { isSelected -> Color.White; isPast -> TMarkMuted.copy(alpha = 0.4f); else -> TMarkBlack })
                        }
                        dayCount++
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color))
        Text(label, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
    }
}
