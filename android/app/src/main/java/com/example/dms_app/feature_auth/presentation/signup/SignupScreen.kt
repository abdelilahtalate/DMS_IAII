package com.example.dms_app.feature_auth.presentation.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.InputField
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun SignupScreen(
    vm: SignupViewModel = viewModel(),
    onSignupSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            onSignupSuccess()
        }
    }

    DriveMindBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displaySmall,
                color = colors.foreground,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Join the future of safe driving",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs
            InputField(
                value = state.fullName,
                onValueChange = vm::updateName,
                placeholder = "Full Name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputField(
                value = state.email,
                onValueChange = vm::updateEmail,
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputField(
                value = state.password,
                onValueChange = vm::updatePassword,
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    InputField(
                        value = state.gender,
                        onValueChange = vm::updateGender,
                        placeholder = "Gender",
                        leadingIcon = Icons.Default.Face
                    )
                }
                Box(modifier = Modifier.weight(0.6f)) {
                    InputField(
                        value = state.age,
                        onValueChange = vm::updateAge,
                        placeholder = "Age",
                        leadingIcon = Icons.Default.CalendarToday
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            PrimaryButton(
                text = "Sign Up",
                onClick = { vm.signup() },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Feedback Messages
            state.successMessage?.let {
                Text(text = it, color = colors.success, style = MaterialTheme.typography.bodySmall)
            }

            state.errorMessage?.let {
                Text(text = it, color = colors.redAlert, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Already have an account? Sign in",
                    color = colors.cyan,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}