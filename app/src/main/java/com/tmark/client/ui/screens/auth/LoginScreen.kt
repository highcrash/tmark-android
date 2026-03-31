package com.tmark.client.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.tmark.client.SmsRetrieverReceiver
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tmark.client.R
import com.tmark.client.ui.components.TMarkButton
import com.tmark.client.ui.theme.*

private val BgDark   = Color(0xFF111110)
private val InputBg  = Color(0x0FFFFFFF)
private val InputBdr = Color(0x1AFFFFFF)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onLoginSuccess()
    }

    Box(Modifier.fillMaxSize().background(BgDark)) {
        // Radial glow
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(TMarkRed.copy(alpha = 0.08f), Color.Transparent),
                    radius = 600f
                )
            )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
        ) {
            Spacer(Modifier.weight(0.18f))

            // App icon logo
            Image(
                painter = painterResource(id = R.drawable.appicon_on_black),
                contentDescription = "T-mark",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "FILM EQUIPMENT RENTAL",
                fontFamily = BarlowCondensed,
                fontSize = 9.sp,
                letterSpacing = 0.35.em,
                color = TMarkRed
            )

            Spacer(Modifier.weight(0.1f))

            // Heading (changes by step)
            val eyebrow = when (state.step) {
                LoginStep.Register -> "NEW ACCOUNT"
                LoginStep.Email    -> "SIGN IN WITH EMAIL"
                else               -> "WELCOME BACK"
            }
            val heading = when (state.step) {
                LoginStep.Register -> "CREATE ACCOUNT"
                LoginStep.Email    -> "SIGN IN"
                else               -> "SIGN IN"
            }
            Text(eyebrow, fontFamily = BarlowCondensed, fontSize = 11.sp, letterSpacing = 0.3.em, color = TMarkMuted)
            Text(heading, fontFamily = BebasNeue, fontSize = 40.sp, color = Color.White, letterSpacing = 0.04.em)

            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                },
                label = "login_step"
            ) { step ->
                when (step) {
                    is LoginStep.Phone    -> PhoneStep(state, vm)
                    is LoginStep.Register -> RegisterStep(state, vm)
                    is LoginStep.Otp      -> OtpStep(state, vm)
                    is LoginStep.Email    -> EmailStep(state, vm)
                }
            }

            Spacer(Modifier.weight(0.2f))
        }
    }
}

@Composable
private fun PhoneStep(state: LoginUiState, vm: LoginViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LoginInput(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            placeholder = "Phone Number",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
            onDone = { vm.sendOtp() }
        )

        Spacer(Modifier.height(6.dp))
        if (state.error != null) {
            Text(state.error, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))
        TMarkButton(text = "SEND OTP", onClick = vm::sendOtp, loading = state.loading)

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Login with email and password instead",
            fontFamily = BarlowCondensed,
            fontSize = 12.sp,
            letterSpacing = 0.05.em,
            color = TMarkRed,
            modifier = Modifier.clickable { vm.switchToEmailLogin() }
        )
    }
}

@Composable
private fun RegisterStep(state: LoginUiState, vm: LoginViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Phone (read-only)
        LoginInput(
            value = state.phone,
            onValueChange = {},
            placeholder = "Phone Number",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            onDone = {},
            enabled = false
        )

        LoginInput(
            value = state.regName,
            onValueChange = vm::onRegName,
            placeholder = "Full Name",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            onDone = {}
        )

        LoginInput(
            value = state.regEmail,
            onValueChange = vm::onRegEmail,
            placeholder = "Email (optional)",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onDone = {}
        )

        LoginInput(
            value = state.regPassword,
            onValueChange = vm::onRegPassword,
            placeholder = "Password (min 8 characters)",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            onDone = {},
            isPassword = true
        )

        LoginInput(
            value = state.regConfirmPassword,
            onValueChange = vm::onRegConfirmPassword,
            placeholder = "Confirm Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onDone = { vm.register() },
            isPassword = true
        )

        if (state.error != null) {
            Text(state.error, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(4.dp))
        TMarkButton(text = "CREATE ACCOUNT", onClick = vm::register, loading = state.loading)

        Text(
            text = "← Back to phone",
            fontFamily = BarlowCondensed,
            fontSize = 13.sp,
            color = TMarkMuted,
            modifier = Modifier.clickable { vm.backToPhone() }
        )
    }
}

@Composable
private fun OtpStep(state: LoginUiState, vm: LoginViewModel) {
    val focusReq = remember { FocusRequester() }
    val context  = LocalContext.current
    LaunchedEffect(Unit) {
        focusReq.requestFocus()
        // Start SMS Retriever so OTP is auto-filled if the SMS contains the app hash
        SmsRetriever.getClient(context).startSmsRetriever()
    }

    // Register dynamic BroadcastReceiver for SMS Retriever result
    DisposableEffect(Unit) {
        val receiver = SmsRetrieverReceiver { otp -> vm.autoFillOtp(otp) }
        val filter = android.content.IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Enter the OTP sent to ${state.phone}",
            fontFamily = Barlow,
            fontSize = 13.sp,
            color = TMarkMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        LoginInput(
            value = state.otp,
            onValueChange = vm::onOtpChange,
            placeholder = "OTP Code",
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            onDone = { vm.verifyOtp() },
            modifier = Modifier.focusRequester(focusReq)
        )

        Spacer(Modifier.height(6.dp))
        if (state.error != null) {
            Text(state.error, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))
        TMarkButton(text = "VERIFY & SIGN IN", onClick = vm::verifyOtp, loading = state.loading)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "← Change number",
            fontFamily = BarlowCondensed,
            fontSize = 13.sp,
            color = TMarkMuted,
            modifier = Modifier.clickable { vm.backToPhone() }
        )
    }
}

@Composable
private fun EmailStep(state: LoginUiState, vm: LoginViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LoginInput(
            value = state.emailInput,
            onValueChange = vm::onEmailInput,
            placeholder = "Email Address",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onDone = {}
        )
        LoginInput(
            value = state.emailPassword,
            onValueChange = vm::onEmailPassword,
            placeholder = "Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onDone = { vm.loginWithEmail() },
            isPassword = true
        )

        if (state.error != null) {
            Text(state.error, color = TMarkRed, fontFamily = Barlow, fontSize = 12.sp)
        }

        Spacer(Modifier.height(4.dp))
        TMarkButton(text = "SIGN IN", onClick = vm::loginWithEmail, loading = state.loading)

        Spacer(Modifier.height(4.dp))
        Text(
            text = "← Use phone OTP instead",
            fontFamily = BarlowCondensed,
            fontSize = 13.sp,
            color = TMarkMuted,
            modifier = Modifier.clickable { vm.switchToPhoneLogin() }
        )
    }
}

@Composable
private fun LoginInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, fontFamily = Barlow, fontSize = 15.sp, color = TMarkMuted) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }, onNext = { /* handled by focus */ }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            disabledContainerColor = Color(0x08FFFFFF),
            focusedBorderColor = TMarkRed,
            unfocusedBorderColor = InputBdr,
            disabledBorderColor = InputBdr,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = TMarkMuted,
            cursorColor = TMarkRed
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Barlow, fontSize = 15.sp, color = Color.White),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        modifier = modifier.fillMaxWidth()
    )
}
