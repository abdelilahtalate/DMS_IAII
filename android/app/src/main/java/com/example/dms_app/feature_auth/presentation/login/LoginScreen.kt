package com.example.dms_app.feature_auth.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.InputField
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val vm: LoginViewModel = viewModel()
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(state.success) {
        if (state.success) {
            onLoginSuccess()
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }

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
                text = "Welcome Back",
                style = MaterialTheme.typography.displaySmall,
                color = colors.foreground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Sign in to continue monitoring",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Input
            InputField(
                value = state.email,
                onValueChange = vm::updateEmail,
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            InputField(
                value = state.password,
                onValueChange = vm::updatePassword,
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = colors.mutedForeground
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remember Me
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = { vm.toggleRememberMe() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.cyan,
                        uncheckedColor = colors.mutedForeground,
                        checkmarkColor = Color.Black
                    )
                )
                Text(
                    text = "Remember Me",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.foreground,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            PrimaryButton(
                text = "Login",
                onClick = { vm.login() },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = colors.redAlert,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signup Link (Placeholder)
            TextButton(onClick = onNavigateToSignup) {
                Text(
                    text = "Don't have an account? Sign up",
                    color = colors.cyan,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}