package com.tmark.client.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.data.model.CatalogItem
import com.tmark.client.data.model.PackageSummary
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

/** Converts a raw type slug like "ARRI_ALEXA_MINI_PACKAGES" to "Arri Alexa Mini Packages". */
private fun String.toTypeDisplayName(): String =
    lowercase().split("_").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }

@Composable
fun CatalogScreen(
    onPackageClick: (String) -> Unit,
    onViewRequest: () -> Unit,
    vm: CatalogViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val selectedType by vm.selectedType.collectAsState()
    val packageTypes by vm.packageTypes.collectAsState()
    val filteredPackages by vm.filteredPackages.collectAsState()
    val cartCount by vm.cartCount.collectAsState()
    // Collect full cart map for reactive qty display
    val cartItems by vm.cartItems.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
        // Combined dark header block — title row + tabs + chips, no separate gaps
        Column(
            Modifier
                .fillMaxWidth()
                .background(TMarkBlack)
                .statusBarsPadding()
        ) {
            // Title row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse & Rent",
                    fontFamily = BebasNeue,
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (cartCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(TMarkRed)
                            .clickable(onClick = onViewRequest)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "BASKET ($cartCount)",
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.15.em,
                            color = Color.White
                        )
                    }
                }
            }

            // Tab switcher — flush, no extra vertical padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogTab("Packages", active = tab == 0) { tab = 0 }
                CatalogTab("Standalone Items", active = tab == 1) { tab = 1 }
            }

            // Type filter chips
            if (tab == 0 && packageTypes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161615))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypeChip("All", active = selectedType.isBlank()) { vm.setTypeFilter("") }
                    packageTypes.forEach { type ->
                        TypeChip(type.toTypeDisplayName(), active = selectedType == type) { vm.setTypeFilter(type) }
                    }
                } // end chips Row
            } // end if chips
        } // end dark header Column

        when {
            state.loading -> LoadingState()
            state.error != null -> ErrorState(state.error!!, onRetry = vm::load)
            else -> {
                if (tab == 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(TMarkOffWhite),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPackages) { pkg ->
                            PackageCard(
                                pkg = pkg,
                                cartQty = cartItems[pkg.id]?.quantity ?: 0,
                                onClick = { onPackageClick(pkg.id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(Color.White)
                    ) {
                        items(state.items) { item ->
                            ItemRow(
                                item = item,
                                cartQty = cartItems[item.id]?.quantity ?: 0,
                                onAdd = { vm.addToCart(item.id, "item", item.name, item.pricePerDay, item.quantity) },
                                onDecrement = { vm.decrementCart(item.id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogTab(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(if (active) TMarkRed else Color.Transparent)
            .border(1.dp, if (active) TMarkRed else Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(label, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp, letterSpacing = 0.1.em,
            color = if (active) Color.White else TMarkMuted)
    }
}

@Composable
private fun TypeChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(if (active) TMarkRed.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, if (active) TMarkRed else Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label.uppercase(), fontFamily = BarlowCondensed, fontSize = 9.sp,
            letterSpacing = 0.2.em,
            color = if (active) TMarkRed else TMarkMuted)
    }
}

@Composable
private fun PackageCard(pkg: PackageSummary, cartQty: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(Color.White)
            .border(1.dp, if (cartQty > 0) TMarkRed.copy(alpha = 0.4f) else TMarkBorder)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(pkg.type.uppercase(), fontFamily = BarlowCondensed, fontSize = 9.sp,
                letterSpacing = 0.28.em, color = TMarkMuted)
            if (cartQty > 0) {
                Box(Modifier.background(TMarkRed).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("IN BASKET ×$cartQty", fontFamily = BarlowCondensed, fontSize = 8.sp,
                        letterSpacing = 0.1.em, color = Color.White)
                }
            }
        }
        Text(pkg.name, fontFamily = BebasNeue, fontSize = 22.sp, color = TMarkBlack, lineHeight = 24.sp)
        Text("${pkg.itemCount} items included", fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
        TMarkDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("৳${"%,.0f".format(pkg.pricePerDay)}", fontFamily = BebasNeue,
                    fontSize = 24.sp, color = TMarkRed)
                Text("/ day", fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted,
                    modifier = Modifier.padding(bottom = 2.dp))
            }
            Text("View Details →", fontFamily = BarlowCondensed, fontWeight = FontWeight.Medium,
                fontSize = 11.sp, color = TMarkMuted)
        }
    }
}

@Composable
private fun ItemRow(item: CatalogItem, cartQty: Int, onAdd: () -> Unit, onDecrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier
                    .background(TMarkOffWhite)
                    .border(1.dp, TMarkBorder)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(item.category, fontFamily = BarlowCondensed, fontSize = 8.sp,
                    letterSpacing = 0.15.em, color = TMarkMuted)
            }
            Text(item.name, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, color = TMarkBlack)
            Text("৳${"%,.0f".format(item.pricePerDay)}/day", fontFamily = Barlow,
                fontSize = 11.sp, color = TMarkMuted)
        }
        // Show qty controls when item is in cart, otherwise just +Add
        if (cartQty > 0) {
            val atMax = cartQty >= item.quantity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).border(1.dp, TMarkBorder).clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Text("−", fontSize = 16.sp, color = TMarkBlack, textAlign = TextAlign.Center)
                }
                Text(cartQty.toString(), fontFamily = BebasNeue, fontSize = 18.sp,
                    modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, color = TMarkBlack)
                Box(
                    Modifier.size(34.dp).background(if (atMax) TMarkBorder else TMarkRed)
                        .clickable(enabled = !atMax, onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(TMarkOffWhite)
                    .border(1.dp, TMarkRed)
                    .clickable(onClick = onAdd)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text("+ Add", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp, color = TMarkRed)
            }
        }
    }
    TMarkDivider()
}
