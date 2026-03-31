package com.tmark.client.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

private fun String.toTypeLabel(): String =
    lowercase().split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

@Composable
fun PackageDetailScreen(
    packageId: String,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
    vm: CatalogViewModel = hiltViewModel()
) {
    val state by vm.detail.collectAsState()
    val cartItems by vm.cartItems.collectAsState()
    var qty by remember { mutableIntStateOf(1) }

    LaunchedEffect(packageId) { vm.loadDetail(packageId) }

    Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
        when {
            state.loading -> {
                // Minimal back nav while loading
                Box(Modifier.fillMaxWidth().background(TMarkBlack).statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text("←", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier.clickable(onClick = onBack))
                }
                LoadingState()
            }
            state.error != null -> {
                Box(Modifier.fillMaxWidth().background(TMarkBlack).statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text("←", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier.clickable(onClick = onBack))
                }
                ErrorState(state.error!!, onRetry = { vm.loadDetail(packageId) })
            }
            else -> {
                val pkg = state.pkg!!
                val maxQty = pkg.maxQtyPerDay.coerceAtLeast(1)
                val cartQty = cartItems[pkg.id]?.quantity ?: 0

                LaunchedEffect(pkg.id) { qty = 1 }

                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 130.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Hero header — single dark band with back nav embedded
                        item {
                            Column(
                                Modifier.fillMaxWidth().background(TMarkBlack)
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp)
                            ) {
                                // Back nav row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    Text("←", color = TMarkRed, fontSize = 18.sp,
                                        modifier = Modifier.clickable(onClick = onBack).padding(end = 8.dp))
                                    Text("BROWSE CATALOG", fontFamily = BarlowCondensed,
                                        fontSize = 10.sp, letterSpacing = 0.28.em, color = TMarkMuted)
                                }
                                // Package info
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Text(pkg.type.toTypeLabel(), fontFamily = BarlowCondensed,
                                        fontSize = 10.sp, letterSpacing = 0.3.em, color = TMarkMuted)
                                    Text(pkg.name, fontFamily = BebasNeue, fontSize = 32.sp,
                                        color = Color.White, lineHeight = 34.sp)
                                    Text("৳${"%,.0f".format(pkg.pricePerDay)} / day",
                                        fontFamily = BebasNeue, fontSize = 22.sp, color = TMarkRed)
                                    if (!pkg.description.isNullOrBlank()) {
                                        Text(pkg.description, fontFamily = Barlow,
                                            fontSize = 13.sp, color = TMarkMuted, lineHeight = 19.sp)
                                    }
                                    if (cartQty > 0) {
                                        Box(Modifier.background(TMarkRed).padding(horizontal = 10.dp, vertical = 3.dp)) {
                                            Text("IN BASKET ×$cartQty", fontFamily = BarlowCondensed,
                                                fontSize = 9.sp, letterSpacing = 0.1.em, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // What's Included header
                        item {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.width(3.dp).height(14.dp).background(TMarkRed))
                                    Spacer(Modifier.width(8.dp))
                                    Text("What's Included", fontFamily = BarlowCondensed,
                                        fontSize = 11.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("FULL EQUIPMENT LIST", fontFamily = BebasNeue,
                                    fontSize = 22.sp, color = TMarkBlack)
                            }
                        }

                        // Combo package: show each sub-package as a section
                        if (pkg.subPackages.isNotEmpty()) {
                            pkg.subPackages.forEach { sub ->
                                item(key = "sub_header_${sub.id}") {
                                    SubPackageHeader(sub.name)
                                }
                                item(key = "sub_items_${sub.id}") {
                                    EquipmentListByCategory(sub.includes)
                                }
                            }
                            // Additional equipment (direct items on combo package)
                            if (pkg.includes.isNotEmpty()) {
                                item(key = "additional_header") {
                                    SubPackageHeader("Additional Equipment")
                                }
                                item(key = "additional_items") {
                                    EquipmentListByCategory(pkg.includes)
                                }
                            }
                        } else {
                            // Standard package: just show items grouped by category
                            item(key = "items") {
                                EquipmentListByCategory(pkg.includes)
                            }
                        }

                        // Availability info
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MAX AVAILABLE PER DAY", fontFamily = BarlowCondensed,
                                    fontSize = 10.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                                Text("$maxQty unit${if (maxQty != 1) "s" else ""}",
                                    fontFamily = BebasNeue, fontSize = 18.sp, color = TMarkBlack)
                            }
                        }
                    }

                    // Sticky bottom CTA — overlaid at the bottom
                    Column(
                        Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                            .background(Color.White).navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Price + qty row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("৳${"%,.0f".format(pkg.pricePerDay)}",
                                    fontFamily = BebasNeue, fontSize = 28.sp, color = TMarkBlack)
                                Text("per day", fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
                            }
                            // Qty controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("QTY", fontFamily = BarlowCondensed, fontSize = 9.sp,
                                    letterSpacing = 0.2.em, color = TMarkMuted,
                                    modifier = Modifier.padding(end = 10.dp))
                                Box(
                                    Modifier.size(36.dp).border(1.dp, TMarkBorder)
                                        .clickable(enabled = qty > 1) { qty-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("−", fontSize = 18.sp,
                                        color = if (qty > 1) TMarkBlack else TMarkMuted,
                                        textAlign = TextAlign.Center)
                                }
                                Text(qty.toString(), fontFamily = BebasNeue, fontSize = 22.sp,
                                    color = TMarkBlack, textAlign = TextAlign.Center,
                                    modifier = Modifier.width(40.dp))
                                Box(
                                    Modifier.size(36.dp)
                                        .background(if (qty < maxQty) TMarkRed else TMarkBorder)
                                        .clickable(enabled = qty < maxQty) { qty++ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 18.sp, color = Color.White,
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                        // Add to Request button
                        TMarkButton(
                            text = if (cartQty > 0) "ADD MORE TO BASKET (+$qty)" else "ADD TO BASKET",
                            onClick = {
                                vm.addToCart(pkg.id, "package", pkg.name, pkg.pricePerDay, maxQty, qty)
                                onAddedToCart()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubPackageHeader(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(28.dp)
                .background(TMarkRed)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "Included Package",
                fontFamily = BarlowCondensed,
                fontSize = 9.sp,
                letterSpacing = 0.2.em,
                color = TMarkMuted
            )
            Text(
                name,
                fontFamily = BebasNeue,
                fontSize = 22.sp,
                color = TMarkBlack
            )
        }
    }
}

@Composable
private fun EquipmentListByCategory(items: List<com.tmark.client.data.model.PackageIncludedItem>) {
    val grouped = items.groupBy { it.category }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.forEach { (category, categoryItems) ->
            // Category card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Category header
                Text(
                    category.uppercase(),
                    fontFamily = BarlowCondensed,
                    fontSize = 11.sp,
                    letterSpacing = 0.2.em,
                    color = TMarkRed,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TMarkBorder)
                )
                Spacer(Modifier.height(10.dp))
                // Items in category
                categoryItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            Modifier
                                .padding(top = 6.dp)
                                .size(4.dp)
                                .background(TMarkRed)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.name,
                                fontFamily = Barlow,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TMarkBlack
                            )
                        }
                    }
                }
            }
        }
    }
}
