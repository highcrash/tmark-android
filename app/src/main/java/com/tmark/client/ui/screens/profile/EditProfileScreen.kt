package com.tmark.client.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    // Pre-fill local fields from loaded profile
    var email   by remember(state.profile) { mutableStateOf(state.profile?.email   ?: "") }
    var address by remember(state.profile) { mutableStateOf(state.profile?.address ?: "") }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            vm.clearSaveSuccess()
            onSaved()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(TMarkOffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "Edit Profile", eyebrow = "Account", onBack = onBack, compact = true)

        Spacer(Modifier.height(24.dp))

        // Read-only info
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(Color.White)
        ) {
            ReadOnlyField("Full Name", state.profile?.name ?: "")
            TMarkDivider()
            ReadOnlyField("Phone", state.profile?.phone ?: "—")
            TMarkDivider()
            if (state.profile?.designation != null) {
                ReadOnlyField("Designation", state.profile!!.designation!!.name)
                TMarkDivider()
            }
        }

        Spacer(Modifier.height(16.dp))

        // Editable fields
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(Color.White)
        ) {
            EditField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "your@email.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            TMarkDivider()
            EditField(
                label = "Address",
                value = address,
                onValueChange = { address = it },
                placeholder = "Your address",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Words,
                onDone = { vm.update(email.trim().ifBlank { null }, address.trim().ifBlank { null }) }
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = state.error!!,
                fontFamily = Barlow,
                fontSize = 13.sp,
                color = TMarkRed,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(Modifier.padding(horizontal = 20.dp)) {
            TMarkButton(
                text = "SAVE CHANGES",
                onClick = { vm.update(email.trim().ifBlank { null }, address.trim().ifBlank { null }) },
                loading = state.saving
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
        Text(value, fontFamily = Barlow, fontSize = 12.sp, color = TMarkBlack)
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    onDone: () -> Unit = {}
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(label, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontFamily = Barlow, fontSize = 14.sp, color = TMarkMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction,
                capitalization = capitalization
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = TMarkRed,
                unfocusedBorderColor = TMarkBorder,
                focusedTextColor = TMarkBlack,
                unfocusedTextColor = TMarkBlack,
                cursorColor = TMarkRed
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 14.sp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
