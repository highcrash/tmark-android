package com.tmark.client.ui.screens.requests

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.data.model.BootstrapDesignation
import com.tmark.client.data.model.BootstrapProductionHouse
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun NewRequestScreen(
    preSelectedEntityId: String = "",
    preSelectedEntityType: String = "",
    onCancel: () -> Unit,
    onSubmitted: () -> Unit,
    vm: NewRequestViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(preSelectedEntityId, state.packages, state.items) {
        if (preSelectedEntityId.isNotBlank() && (state.packages.isNotEmpty() || state.items.isNotEmpty())) {
            vm.preSelect(preSelectedEntityId, preSelectedEntityType)
        }
    }

    LaunchedEffect(state.submitted) {
        if (state.submitted) { delay(1500); onSubmitted() }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(TMarkOffWhite)) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth().background(TMarkBlack)
                    .drawBehind {
                        val step = 32.dp.toPx(); val lc = Color.White.copy(alpha = 0.025f)
                        var y = 0f; while (y <= size.height) { drawLine(lc, Offset(0f, y), Offset(size.width, y), 1f); y += step }
                        var x = 0f; while (x <= size.width) { drawLine(lc, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
                    }
            ) {
                Box(Modifier.size(180.dp).offset((-30).dp, (-30).dp)
                    .background(Brush.radialGradient(listOf(TMarkRed.copy(alpha = 0.15f), Color.Transparent))))
                Column(Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("CLIENT PORTAL", fontFamily = BarlowCondensed, fontSize = 10.sp, letterSpacing = 0.28.em, color = TMarkMuted)
                            Text("New Request", fontFamily = BebasNeue, fontSize = 28.sp, color = Color.White)
                        }
                        Text("CANCEL", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp, letterSpacing = 0.15.em, color = TMarkMuted,
                            modifier = Modifier.clickable(onClick = onCancel))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        repeat(6) { i ->
                            Box(Modifier.weight(1f).height(3.dp)
                                .background(if (i < state.step) TMarkRed else Color.White.copy(alpha = 0.15f)))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("STEP ${state.step} OF 6", fontFamily = BarlowCondensed, fontSize = 10.sp,
                        letterSpacing = 0.2.em, color = TMarkMuted)
                }
            }

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    if (targetState > initialState) slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    else slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                },
                label = "request_step",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    1 -> Step1Details(state, vm)
                    2 -> Step2Equipment(state, vm)
                    3 -> Step3Dates(state, vm)
                    4 -> Step4Contacts(state, vm)
                    5 -> Step5ProductionHouse(state, vm)
                    6 -> Step6Review(state, vm)
                    else -> {}
                }
            }
        }

        // Success overlay
        AnimatedVisibility(visible = state.submitted, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(TMarkBlack.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(72.dp))
                    Text("REQUEST SUBMITTED", fontFamily = BebasNeue, fontSize = 32.sp, color = Color.White)
                    Text("We'll review and confirm shortly.", fontFamily = Barlow, fontSize = 14.sp, color = TMarkMuted)
                }
            }
        }
    }
}

// ── Step 1: Your Details ──────────────────────────────────────────────────────

@Composable
private fun Step1Details(state: NewRequestUiState, vm: NewRequestViewModel) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.imePadding()) {
        // Pre-filled contact card
        if (state.clientName.isNotBlank() || state.clientPhone.isNotBlank()) {
            item {
                Column(Modifier.fillMaxWidth().background(Color.White)) {
                    Text("YOUR DETAILS", fontFamily = BarlowCondensed, fontSize = 9.sp,
                        letterSpacing = 0.28.em, color = TMarkMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                    TMarkDivider()
                    if (state.clientName.isNotBlank()) { ReadOnlyField("Name", state.clientName); TMarkDivider() }
                    if (state.clientPhone.isNotBlank()) { ReadOnlyField("Phone", state.clientPhone) }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PROJECT DETAILS", fontFamily = BarlowCondensed, fontSize = 9.sp,
                    letterSpacing = 0.28.em, color = TMarkMuted)
                StepTextField("Project Name *", state.projectName, vm::onProjectName)
                StepTextField("Project Type  (e.g. TVC, Film, Music Video)", state.projectType, vm::onProjectType)
                StepTextField("Shoot Location", state.projectLocation, vm::onProjectLocation)
            }
        }
        item { state.error?.let { Text(it, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp) } }
        item { TMarkButton("NEXT: SELECT EQUIPMENT →", onClick = vm::nextStep) }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

// ── Step 2: Equipment (grouped by package type) ───────────────────────────────

@Composable
private fun Step2Equipment(state: NewRequestUiState, vm: NewRequestViewModel) {
    // Group packages by type, sorted alphabetically
    val packagesByType = remember(state.packages) {
        state.packages.groupBy { it.type }.entries.sortedBy { it.key }
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.loadingCatalog) {
            item { LoadingState() }
        } else {
            if (state.packages.isNotEmpty()) {
                packagesByType.forEach { (type, pkgs) ->
                    item { SectionLbl(type.uppercase()) }
                    items(pkgs) { pkg ->
                        EquipmentRow(
                            name = pkg.name,
                            detail = "৳${"%,.0f".format(pkg.pricePerDay)}/day · max ${pkg.maxQtyPerDay}",
                            quantity = state.selected[pkg.id]?.quantity ?: 0,
                            maxQuantity = pkg.maxQtyPerDay,
                            onIncrement = { vm.incrementItem(pkg.id, "package", pkg.name, pkg.pricePerDay, pkg.maxQtyPerDay) },
                            onDecrement = { vm.decrementItem(pkg.id) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
            if (state.items.isNotEmpty()) {
                item { SectionLbl("INDIVIDUAL ITEMS") }
                items(state.items) { item ->
                    EquipmentRow(
                        name = item.name,
                        detail = "${item.category} · ৳${"%,.0f".format(item.pricePerDay)}/day",
                        quantity = state.selected[item.id]?.quantity ?: 0,
                        maxQuantity = item.maxQtyPerDay,
                        onIncrement = { vm.incrementItem(item.id, "item", item.name, item.pricePerDay, item.maxQtyPerDay) },
                        onDecrement = { vm.decrementItem(item.id) }
                    )
                }
            }
        }
        item { state.error?.let { Text(it, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp) } }
        item { NavRow(vm::prevStep, vm::nextStep, "NEXT: DATES →") }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

// ── Step 3: Dates ─────────────────────────────────────────────────────────────

@Composable
private fun Step3Dates(state: NewRequestUiState, vm: NewRequestViewModel) {
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.of(today.year, today.monthValue)) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionLbl("SELECT SHOOT DATES") }
        item {
            Column(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("←", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier.clickable { if (month > YearMonth.now()) month = month.minusMonths(1) }.padding(8.dp))
                    Text("${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                        fontFamily = BebasNeue, fontSize = 22.sp, color = TMarkBlack,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("→", color = TMarkRed, fontSize = 20.sp,
                        modifier = Modifier.clickable { month = month.plusMonths(1) }.padding(8.dp))
                }
                Spacer(Modifier.height(8.dp))
                CalendarGrid(month, state.selectedDates, today) { day ->
                    vm.toggleDate("%04d-%02d-%02d".format(month.year, month.monthValue, day))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(TMarkRed, "Selected"); LegendDot(TMarkRed.copy(alpha = 0.15f), "Today")
            }
        }
        item {
            Text(if (state.selectedDates.isEmpty()) "No dates selected" else "${state.selectedDates.size} day(s) selected",
                fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                color = if (state.selectedDates.isEmpty()) TMarkMuted else TMarkBlack)
        }
        item { state.error?.let { Text(it, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp) } }
        item { NavRow(vm::prevStep, vm::nextStep, "NEXT: CONTACTS →") }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

// ── Step 4: Project Contacts ──────────────────────────────────────────────────

@Composable
private fun Step4Contacts(state: NewRequestUiState, vm: NewRequestViewModel) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.imePadding()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionLbl("PROJECT CONTACTS")
                Text("Add the crew T-mark will be working with. Enter a phone to auto-fill their details.",
                    fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
            }
        }
        itemsIndexed(state.contacts) { index, contact ->
            ContactCard(
                index = index,
                contact = contact,
                designations = state.designations,
                designationsLoading = state.loadingCatalog,
                canRemove = state.contacts.size > 1,
                onPhoneChange = { vm.updateContactPhone(index, it) },
                onLookup = { vm.lookupContactPhone(index) },
                onNameChange = { vm.updateContactName(index, it) },
                onDesignationChange = { id, name -> vm.updateContactDesignation(index, id, name) },
                onEmailChange = { vm.updateContactEmail(index, it) },
                onRemove = { vm.removeContact(index) }
            )
        }
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, TMarkBorder)
                    .clickable { vm.addContact() }
                    .padding(vertical = 14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("+", fontFamily = BebasNeue, fontSize = 20.sp, color = TMarkMuted)
                    Text("ADD ANOTHER CONTACT", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                }
            }
        }
        item { NavRow(vm::prevStep, vm::nextStep, "NEXT: PROD. HOUSE →") }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun ContactCard(
    index: Int,
    contact: ProjectContact,
    designations: List<BootstrapDesignation>,
    designationsLoading: Boolean,
    canRemove: Boolean,
    onPhoneChange: (String) -> Unit,
    onLookup: () -> Unit,
    onNameChange: (String) -> Unit,
    onDesignationChange: (String, String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var showDesigDropdown by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().background(Color.White)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Contact ${index + 1}", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp, letterSpacing = 0.1.em, color = TMarkBlack)
                if (contact.lookedUp) {
                    Box(Modifier.border(1.dp, Color(0xFF16A34A)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("LOOKED UP", fontFamily = BarlowCondensed, fontSize = 8.sp,
                            letterSpacing = 0.15.em, color = Color(0xFF16A34A))
                    }
                }
            }
            if (canRemove) {
                Icon(Icons.Outlined.Close, "Remove", tint = TMarkMuted, modifier = Modifier.size(16.dp).clickable { onRemove() })
            }
        }
        TMarkDivider()

        // Determine whether to show name/designation/email fields
    val showDetailFields = contact.lookedUp || contact.lookupError == "Not found in system"

    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Phone with lookup — always shown
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PHONE NUMBER", fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = contact.phone,
                        onValueChange = onPhoneChange,
                        singleLine = true,
                        placeholder = { Text("01XXXXXXXXX", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onLookup() }),
                        colors = contactFieldColors(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.weight(1f),
                        trailingIcon = if (contact.lookedUp) ({
                            Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                        }) else null
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(48.dp).background(TMarkBlack).clickable(enabled = !contact.lookingUp) { onLookup() }
                    ) {
                        if (contact.lookingUp) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.Search, "Lookup", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                // Lookup status messages
                when {
                    contact.lookedUp -> Text("✓ Details auto-filled from system", fontFamily = Barlow,
                        fontSize = 11.sp, color = Color(0xFF16A34A))
                    contact.lookupError == "Not found in system" -> Text("Not found — enter details manually",
                        fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
                    contact.lookupError != null -> Text(contact.lookupError, fontFamily = Barlow,
                        fontSize = 11.sp, color = TMarkRed)
                    else -> Text("Enter phone and tap search to auto-fill",
                        fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
                }
            }

            // Detail fields — only shown after lookup attempt
            if (showDetailFields) {
                // Full name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("FULL NAME", fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                    OutlinedTextField(
                        value = contact.name,
                        onValueChange = onNameChange,
                        singleLine = true,
                        placeholder = { Text("Full name", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                        colors = contactFieldColors(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Designation
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("DESIGNATION / ROLE", fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                    when {
                        designationsLoading && designations.isEmpty() -> {
                            Box(Modifier.fillMaxWidth().border(1.dp, TMarkBorder).padding(14.dp)) {
                                Text("Loading roles…", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted)
                            }
                        }
                        !designationsLoading && designations.isEmpty() -> {
                            // Fallback: free text entry when no designations are configured
                            OutlinedTextField(
                                value = contact.designationName,
                                onValueChange = { onDesignationChange(it, it) },
                                singleLine = true,
                                placeholder = { Text("Enter role / designation", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                                colors = contactFieldColors(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> {
                            Box {
                            OutlinedTextField(
                                value = contact.designationName,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                placeholder = { Text("Select role…", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                                colors = contactFieldColors(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier.fillMaxWidth().clickable { showDesigDropdown = true }
                            )
                            DropdownMenu(expanded = showDesigDropdown, onDismissRequest = { showDesigDropdown = false }) {
                                designations.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.name, fontFamily = Barlow, fontSize = 14.sp) },
                                        onClick = { onDesignationChange(d.id, d.name); showDesigDropdown = false }
                                    )
                                }
                            }
                        }
                    }
                } // end Designation Column

                // Email
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("EMAIL (OPTIONAL)", fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                    OutlinedTextField(
                        value = contact.email,
                        onValueChange = onEmailChange,
                        singleLine = true,
                        placeholder = { Text("contact@email.com", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = contactFieldColors(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
}

// ── Step 5: Production House ──────────────────────────────────────────────────

@Composable
private fun Step5ProductionHouse(state: NewRequestUiState, vm: NewRequestViewModel) {
    val form = state.productionHouseForm
    val filtered = remember(form.searchQuery, state.productionHouses) {
        if (form.searchQuery.isBlank()) state.productionHouses
        else state.productionHouses.filter { it.name.contains(form.searchQuery, ignoreCase = true) }
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.imePadding()) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionLbl("PRODUCTION HOUSE")
                Text("Optional — select an existing production house or create a new one.",
                    fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
            }
        }

        if (!form.isCreatingNew) {
            // Search
            item {
                Column(Modifier.fillMaxWidth().background(Color.White)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Search, null, tint = TMarkMuted, modifier = Modifier.size(16.dp))
                        OutlinedTextField(
                            value = form.searchQuery,
                            onValueChange = vm::updatePHSearch,
                            singleLine = true,
                            placeholder = { Text("Search production houses…", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TMarkBlack,
                                unfocusedTextColor = TMarkBlack
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 13.sp),
                            modifier = Modifier.weight(1f)
                        )
                        if (form.selectedId != null) {
                            Icon(Icons.Outlined.Close, "Clear", tint = TMarkMuted, modifier = Modifier.size(16.dp).clickable { vm.clearProductionHouse() })
                        }
                    }
                    TMarkDivider()
                    filtered.forEach { ph ->
                        val isSelected = form.selectedId == ph.id
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(if (isSelected) TMarkRed.copy(alpha = 0.04f) else Color.Transparent)
                                .let { if (isSelected) it.border(width = 0.dp, color = Color.Transparent).then(Modifier) else it }
                                .clickable { vm.selectProductionHouse(ph.id, ph.name) }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier
                                .then(if (isSelected) Modifier.border(androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)) else Modifier)
                            ) {
                                // left red accent line for selected
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Box(Modifier.width(2.dp).height(36.dp).background(TMarkRed))
                                        Spacer(Modifier.width(10.dp))
                                    }
                                    Column {
                                        Text(ph.name, fontFamily = Barlow, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TMarkBlack)
                                        val sub = listOfNotNull(ph.contactPerson, ph.phone).joinToString(" · ")
                                        if (sub.isNotBlank()) Text(sub, fontFamily = BarlowCondensed, fontSize = 10.sp, letterSpacing = 0.1.em, color = TMarkMuted)
                                    }
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            }
                        }
                        TMarkDivider()
                    }
                }
            }

            // Or create new
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f).height(1.dp).background(TMarkBorder))
                    Text("or create new", fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
                    Box(Modifier.weight(1f).height(1.dp).background(TMarkBorder))
                }
            }
            item {
                Box(Modifier.fillMaxWidth().border(1.dp, TMarkBorder).clickable { vm.toggleCreatingNewPH(true) }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center) {
                    Text("+ CREATE NEW PRODUCTION HOUSE", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp, letterSpacing = 0.2.em, color = TMarkBlack)
                }
            }
        } else {
            // New production house form
            item {
                Column(Modifier.fillMaxWidth().background(Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("NEW PRODUCTION HOUSE", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp, letterSpacing = 0.1.em, color = TMarkBlack)
                        Text("← SEARCH EXISTING", fontFamily = BarlowCondensed, fontSize = 10.sp, color = TMarkRed,
                            modifier = Modifier.clickable { vm.toggleCreatingNewPH(false) })
                    }
                    TMarkDivider()
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PHField("Production House Name *", form.newName, vm::updateNewPHName)
                        PHField("Contact Person", form.newContactPerson, vm::updateNewPHContactPerson)
                        PHField("Phone", form.newPhone, vm::updateNewPHPhone, KeyboardType.Phone)
                        PHField("Address", form.newAddress, vm::updateNewPHAddress)
                    }
                }
            }
        }

        item { NavRow(vm::prevStep, vm::nextStep, "NEXT: REVIEW →") }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

// ── Step 6: Review & Submit ───────────────────────────────────────────────────

@Composable
private fun Step6Review(state: NewRequestUiState, vm: NewRequestViewModel) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.imePadding()) {
        item { SectionLbl("REVIEW YOUR REQUEST") }

        item {
            ReviewSection("PROJECT") {
                ReviewRow("Name", state.projectName)
                if (state.projectType.isNotBlank()) ReviewRow("Type", state.projectType)
                if (state.projectLocation.isNotBlank()) ReviewRow("Location", state.projectLocation)
            }
        }

        if (state.selected.isNotEmpty()) {
            item {
                ReviewSection("EQUIPMENT") {
                    state.selected.values.forEachIndexed { i, eq ->
                        EquipmentReviewRow(eq.name, eq.quantity, eq.pricePerDay)
                        if (i < state.selected.size - 1) TMarkDivider()
                    }
                }
            }
        }

        if (state.selectedDates.isNotEmpty()) {
            item {
                ReviewSection("DATES") {
                    val label = if (state.selectedDates.size == 1) state.selectedDates.first()
                    else "${state.selectedDates.first()} – ${state.selectedDates.last()} · ${state.selectedDates.size} days"
                    ReviewRow("Dates", label)
                }
            }
        }

        val validContacts = state.contacts.filter { it.name.isNotBlank() }
        if (validContacts.isNotEmpty()) {
            item {
                ReviewSection("CONTACTS") {
                    validContacts.forEachIndexed { i, c ->
                        ReviewRow(c.name, c.designationName.ifBlank { c.phone })
                        if (i < validContacts.size - 1) TMarkDivider()
                    }
                }
            }
        }

        val ph = state.productionHouseForm
        val phName = when {
            ph.selectedName.isNotBlank() -> ph.selectedName
            ph.isCreatingNew && ph.newName.isNotBlank() -> "${ph.newName} (new)"
            else -> null
        }
        if (phName != null) {
            item { ReviewSection("PRODUCTION HOUSE") { ReviewRow("Company", phName) } }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("NOTES (OPTIONAL)", fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.28.em, color = TMarkMuted)
                OutlinedTextField(
                    value = state.notes, onValueChange = vm::onNotes,
                    placeholder = { Text("Any special requirements…", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted) },
                    minLines = 3, maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = TMarkRed, unfocusedBorderColor = TMarkBorder,
                        focusedTextColor = TMarkBlack, unfocusedTextColor = TMarkBlack
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
                    shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Box(Modifier.fillMaxWidth().background(TMarkBlack).padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ESTIMATED TOTAL", fontFamily = BarlowCondensed, fontSize = 11.sp, letterSpacing = 0.2.em, color = TMarkMuted)
                    Text("৳${"%,.0f".format(vm.estimatedTotal)}", fontFamily = BebasNeue, fontSize = 32.sp, color = TMarkRed)
                }
            }
        }

        item { state.error?.let { Text(it, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp) } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TMarkOutlineButton("← BACK", onClick = vm::prevStep, modifier = Modifier.weight(1f), color = TMarkBlack)
                TMarkButton("SUBMIT REQUEST", onClick = vm::submit, loading = state.submitting, modifier = Modifier.weight(2f))
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

// ── Shared Helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionLbl(text: String) {
    Text(text, fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.28.em, color = TMarkMuted)
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted)
        Text(value, fontFamily = Barlow, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TMarkBlack)
    }
}

@Composable
private fun NavRow(onBack: () -> Unit, onNext: () -> Unit, nextLabel: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TMarkOutlineButton("← BACK", onClick = onBack, modifier = Modifier.weight(1f), color = TMarkBlack)
        TMarkButton(nextLabel, onClick = onNext, modifier = Modifier.weight(2f))
    }
}

@Composable
private fun EquipmentRow(name: String, detail: String, quantity: Int, maxQuantity: Int = Int.MAX_VALUE, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    val atMax = quantity >= maxQuantity
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TMarkBlack)
            Text(detail, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
        }
        Spacer(Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).border(1.dp, TMarkBorder).clickable(onClick = onDecrement), contentAlignment = Alignment.Center) {
                Text("−", fontSize = 16.sp, color = TMarkBlack, textAlign = TextAlign.Center)
            }
            Text(quantity.toString(), fontFamily = BebasNeue, fontSize = 18.sp,
                modifier = Modifier.width(32.dp), textAlign = TextAlign.Center,
                color = if (quantity > 0) TMarkBlack else TMarkMuted)
            Box(
                Modifier.size(32.dp).background(when { atMax -> TMarkBorder; quantity > 0 -> TMarkRed; else -> TMarkBorder })
                    .clickable(enabled = !atMax, onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center)
            }
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
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                            .background(when { isSelected -> TMarkRed; isToday -> TMarkRed.copy(alpha = 0.15f); else -> Color.Transparent })
                            .clickable(enabled = !isPast) { onDayClick(day) },
                            contentAlignment = Alignment.Center) {
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

@Composable
private fun ReviewSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.28.em, color = TMarkMuted)
        Column(Modifier.fillMaxWidth().background(Color.White)) { content() }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted,
            modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(value, fontFamily = Barlow, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TMarkBlack,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EquipmentReviewRow(name: String, quantity: Int, pricePerDay: Double) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(name, fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, color = TMarkBlack)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("×$quantity", fontFamily = BarlowCondensed, fontSize = 12.sp, color = TMarkMuted)
            Text("·", fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
            Text("৳${"%,.0f".format(pricePerDay)}/day", fontFamily = BarlowCondensed,
                fontSize = 12.sp, color = TMarkRed)
        }
    }
}

@Composable
private fun StepTextField(label: String, value: String, onValueChange: (String) -> Unit, minLines: Int = 1) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontFamily = BarlowCondensed, fontSize = 10.sp, letterSpacing = 0.15.em, color = TMarkMuted)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            singleLine = minLines == 1, minLines = minLines, maxLines = if (minLines > 1) 6 else 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = TMarkRed, unfocusedBorderColor = TMarkBorder,
                focusedTextColor = TMarkBlack, unfocusedTextColor = TMarkBlack
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
            shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PHField(label: String, value: String, onChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontFamily = BarlowCondensed, fontSize = 9.sp, letterSpacing = 0.2.em, color = TMarkMuted)
        OutlinedTextField(
            value = value, onValueChange = onChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = contactFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
            shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun contactFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedBorderColor = TMarkRed, unfocusedBorderColor = TMarkBorder,
    focusedTextColor = TMarkBlack, unfocusedTextColor = TMarkBlack
)
