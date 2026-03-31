package com.tmark.client.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.tmark.client.ui.components.*
import com.tmark.client.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onChangePassword: (Boolean) -> Unit = {},
    onEditProfile: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLogout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TMarkOffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // Combined header + avatar — single dark band
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TMarkBlack)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back nav / eyebrow row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    "ACCOUNT",
                    fontFamily = BarlowCondensed,
                    fontSize = 10.sp,
                    letterSpacing = 0.28.em,
                    color = TMarkMuted,
                    modifier = Modifier.weight(1f)
                )
            }
            Text("Profile", fontFamily = BebasNeue, fontSize = 24.sp, color = Color.White,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            // Avatar
            val initials = state.profile?.name
                ?.split(" ")
                ?.take(2)
                ?.mapNotNull { it.firstOrNull()?.toString() }
                ?.joinToString("") ?: "?"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp).background(TMarkRed, CircleShape)
            ) {
                Text(initials.uppercase(), fontFamily = BebasNeue, fontSize = 26.sp, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text(state.profile?.name ?: "", fontFamily = BebasNeue, fontSize = 22.sp, color = Color.White)
            Text(state.profile?.phone ?: "", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted)
            Spacer(Modifier.height(16.dp))
        }

        when {
            state.loading -> LoadingState()
            else -> {
                val p = state.profile

                Spacer(Modifier.height(20.dp))

                // Personal Information
                SectionBlock("Personal Information") {
                    p?.let {
                        ProfileField("Full Name", it.name)
                        ProfileField("Phone", it.phone ?: "—")
                        ProfileField("Email", it.email ?: "—")
                        ProfileField("Address", it.address ?: "—")
                        ProfileField("Member Since", it.memberSince.take(10))
                        if (it.designation != null) ProfileField("Designation", it.designation.name)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    TMarkOutlineButton("Edit Profile", onClick = onEditProfile)
                }

                Spacer(Modifier.height(20.dp))

                // Security
                SectionBlock("Security") {
                    ProfileActionRow("Change Password") {
                        onChangePassword(p?.hasPassword ?: true)
                    }
                    TMarkDivider()
                    ProfileActionRow("Two-Factor Auth") {}
                }

                Spacer(Modifier.height(20.dp))

                // App Settings
                SectionBlock("App Settings") {
                    ProfileActionRow("Push Notifications") {}
                    TMarkDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Language", fontFamily = Barlow, fontSize = 14.sp, color = TMarkBlack)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("English", fontFamily = Barlow, fontSize = 13.sp, color = TMarkMuted)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Outlined.ChevronRight, null, tint = TMarkMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Sign out
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, TMarkRed.copy(alpha = 0.4f))
                        .clickable { vm.logout() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SIGN OUT", fontFamily = BarlowCondensed, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, letterSpacing = 0.2.em, color = TMarkRed)
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SectionBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(title.uppercase(), fontFamily = BarlowCondensed, fontSize = 10.sp,
            letterSpacing = 0.28.em, color = TMarkMuted,
            modifier = Modifier.padding(bottom = 8.dp))
        Column(Modifier.fillMaxWidth().background(Color.White)) {
            content()
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = Barlow, fontSize = 12.sp, color = TMarkMuted)
        Text(value, fontFamily = Barlow, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TMarkBlack)
    }
    TMarkDivider()
}

@Composable
private fun ProfileActionRow(label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = Barlow, fontSize = 14.sp, color = TMarkBlack)
        Icon(Icons.Outlined.ChevronRight, null, tint = TMarkMuted, modifier = Modifier.size(18.dp))
    }
}
