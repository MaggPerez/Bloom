package com.example.bloom

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.graphics.Color
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel { SettingsViewModel(context) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it, context) }
    }

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Profile Section
            ProfileSection(
                viewModel = viewModel,
                onAvatarClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            HorizontalDivider()

            // Appearance Section
            AppearanceSection(
                viewModel = viewModel,
                onSignOut = {
                    coroutineScope.launch {
                        SupabaseClient.client.auth.signOut()
                        navController.navigate("main_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )

            // Loading indicator
            if (viewModel.isLoading || viewModel.isUploadingAvatar) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    viewModel: SettingsViewModel,
    onAvatarClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Profile")

        // Avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.userProfile?.avatar_url != null) {
                    AsyncImage(
                        model = viewModel.userProfile?.avatar_url,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Camera overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Avatar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Email (read-only)
        ProfileFieldReadOnly(
            label = "Email",
            value = viewModel.userProfile?.email ?: "Loading..."
        )

        // Full Name (editable)
        EditableProfileField(
            label = "Full Name",
            value = viewModel.userProfile?.full_name ?: "",
            isEditing = viewModel.isEditingFullName,
            editingValue = viewModel.editingFullName,
            onValueChange = { viewModel.editingFullName = it },
            onEditClick = { viewModel.startEditingFullName() },
            onSaveClick = { viewModel.saveFullName() },
            onCancelClick = { viewModel.cancelEditing() }
        )

        // Username (editable with validation)
        EditableUsernameField(
            label = "Username",
            value = viewModel.userProfile?.username ?: "",
            isEditing = viewModel.isEditingUsername,
            editingValue = viewModel.editingUsername,
            onValueChange = {
                viewModel.editingUsername = it
                viewModel.checkUsernameAvailability()
            },
            onEditClick = { viewModel.startEditingUsername() },
            onSaveClick = { viewModel.saveUsername() },
            onCancelClick = { viewModel.cancelEditing() },
            validationMessage = viewModel.usernameValidationMessage,
            isAvailable = viewModel.isUsernameAvailable
        )
    }
}

@Composable
fun AppearanceSection(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Appearance")

        ThemeSelector(
            selectedTheme = viewModel.themePreference,
            onThemeSelected = { viewModel.updateTheme(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Red
            ),
            border = BorderStroke(1.dp, Color.Red)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign Out",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Sign Out")
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ProfileFieldReadOnly(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun EditableProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    editingValue: String,
    onValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isEditing) {
            // Editing mode
            OutlinedTextField(
                value = editingValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = onSaveClick) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancelClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                singleLine = true
            )
        } else {
            // Display mode
            OutlinedTextField(
                value = value.ifEmpty { "Not set" },
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun EditableUsernameField(
    label: String,
    value: String,
    isEditing: Boolean,
    editingValue: String,
    onValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    validationMessage: String?,
    isAvailable: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isEditing) {
            // Editing mode with validation
            OutlinedTextField(
                value = editingValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = onSaveClick) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancelClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                singleLine = true,
                isError = validationMessage != null && !isAvailable,
                supportingText = {
                    validationMessage?.let {
                        Text(
                            text = it,
                            color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        } else {
            // Display mode
            OutlinedTextField(
                value = value.ifEmpty { "Not set" },
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeOption(
            title = "Light",
            description = "Always use light theme",
            icon = Icons.Default.LightMode,
            isSelected = selectedTheme == "light",
            onClick = { onThemeSelected("light") }
        )

        ThemeOption(
            title = "Dark",
            description = "Always use dark theme",
            icon = Icons.Default.DarkMode,
            isSelected = selectedTheme == "dark",
            onClick = { onThemeSelected("dark") }
        )

        ThemeOption(
            title = "System",
            description = "Follow system theme",
            icon = Icons.Default.SettingsBrightness,
            isSelected = selectedTheme == "system",
            onClick = { onThemeSelected("system") }
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreviewScreen() {
    BloomTheme {
        SettingsScreen(navController = rememberNavController())
    }
}