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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun ChangePasswordScreen(
    hasPassword: Boolean,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: ChangePasswordViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(hasPassword) { vm.init(hasPassword) }
    LaunchedEffect(state.success) { if (state.success) onSuccess() }

    Column(
        Modifier
            .fillMaxSize()
            .background(TMarkOffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = if (hasPassword) "Change Password" else "Set Password",
            eyebrow = "Security",
            onBack = onBack,
            compact = true
        )

        Spacer(Modifier.height(24.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(Color.White)
        ) {
            if (hasPassword) {
                PasswordField(
                    value = state.currentPassword,
                    onValueChange = vm::onCurrentPassword,
                    label = "Current Password",
                    placeholder = "Enter current password",
                    imeAction = ImeAction.Next
                )
                TMarkDivider()
            }

            PasswordField(
                value = state.newPassword,
                onValueChange = vm::onNewPassword,
                label = "New Password",
                placeholder = "Minimum 8 characters",
                imeAction = ImeAction.Next
            )
            TMarkDivider()
            PasswordField(
                value = state.confirmPassword,
                onValueChange = vm::onConfirmPassword,
                label = "Confirm New Password",
                placeholder = "Re-enter new password",
                imeAction = ImeAction.Done,
                onDone = { vm.submit() }
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

        if (!hasPassword) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You haven't set a password yet. Setting one allows you to log in with email & password.",
                fontFamily = Barlow,
                fontSize = 12.sp,
                color = TMarkMuted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        Box(Modifier.padding(horizontal = 20.dp)) {
            TMarkButton(
                text = if (hasPassword) "UPDATE PASSWORD" else "SET PASSWORD",
                onClick = vm::submit,
                loading = state.saving
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    imeAction: ImeAction,
    onDone: () -> Unit = {}
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text(label, fontFamily = Barlow, fontSize = 11.sp, color = TMarkMuted)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontFamily = Barlow, fontSize = 14.sp, color = TMarkMuted) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
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
