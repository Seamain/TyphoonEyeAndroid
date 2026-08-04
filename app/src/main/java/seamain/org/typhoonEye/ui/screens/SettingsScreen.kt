package seamain.org.typhoonEye.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import seamain.org.typhoonEye.R
import seamain.org.typhoonEye.data.preferences.AppLanguage
import seamain.org.typhoonEye.data.preferences.ThemeMode
import seamain.org.typhoonEye.data.preferences.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    notificationsGranted: Boolean,
    locationPermissionGranted: Boolean,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAppLanguageChange: (AppLanguage) -> Unit,
    onLiveActivityChange: (Boolean) -> Unit,
    onEmergencyAlertsChange: (Boolean) -> Unit,
    onLocationAlertsChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenLicenses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionLabel = remember(settings.appLanguage) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            context.getString(R.string.version_label, info.versionName.orEmpty())
        }.getOrDefault(context.getString(R.string.app_name))
    }
    val needsNotificationPermission =
        (settings.liveActivityEnabled || settings.emergencyAlertsEnabled) && !notificationsGranted
    val needsLocationPermission =
        settings.locationAlertsEnabled &&
            settings.emergencyAlertsEnabled &&
            !locationPermissionGranted

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = context.getString(R.string.settings) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = context.getString(R.string.back)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = stringResource(R.string.section_language)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.language_title)) },
                    supportingContent = { Text(stringResource(R.string.language_subtitle)) },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                        .padding(bottom = 8.dp)
                ) {
                    AppLanguage.entries.forEachIndexed { index, language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.appLanguage == language,
                                    onClick = { onAppLanguageChange(language) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .semantics {
                                    contentDescription = context.getString(
                                        R.string.cd_language_option,
                                        language.nativeLabel
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.appLanguage == language,
                                onClick = null
                            )
                            Text(
                                text = language.nativeLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                        if (index < AppLanguage.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }

            SettingsSection(title = stringResource(R.string.section_notifications)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.live_status_title),
                    subtitle = when {
                        !settings.liveActivityEnabled ->
                            stringResource(R.string.live_status_off)
                        !notificationsGranted ->
                            stringResource(R.string.live_status_need_permission)
                        else ->
                            stringResource(R.string.live_status_on)
                    },
                    icon = Icons.Outlined.NotificationsActive,
                    checked = settings.liveActivityEnabled,
                    onCheckedChange = onLiveActivityChange,
                    switchDescription = stringResource(R.string.cd_live_status_switch),
                    showDivider = true
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.emergency_alerts_title),
                    subtitle = when {
                        !settings.emergencyAlertsEnabled ->
                            stringResource(R.string.emergency_alerts_off)
                        !notificationsGranted ->
                            stringResource(R.string.emergency_alerts_need_permission)
                        else ->
                            stringResource(R.string.emergency_alerts_on)
                    },
                    icon = Icons.Outlined.NotificationImportant,
                    checked = settings.emergencyAlertsEnabled,
                    onCheckedChange = onEmergencyAlertsChange,
                    switchDescription = stringResource(R.string.cd_emergency_alerts_switch),
                    showDivider = true
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.location_alerts_title),
                    subtitle = when {
                        !settings.locationAlertsEnabled ->
                            stringResource(R.string.location_alerts_off)
                        !locationPermissionGranted ->
                            stringResource(R.string.location_alerts_need_permission)
                        !settings.cachedUserLocation?.label.isNullOrBlank() ->
                            stringResource(
                                R.string.location_alerts_on_with_place,
                                settings.cachedUserLocation!!.label
                            )
                        else ->
                            stringResource(R.string.location_alerts_on)
                    },
                    icon = Icons.Outlined.MyLocation,
                    checked = settings.locationAlertsEnabled,
                    onCheckedChange = onLocationAlertsChange,
                    switchDescription = stringResource(R.string.cd_location_alerts_switch),
                    showDivider = false,
                    enabled = settings.emergencyAlertsEnabled
                )
                if (needsNotificationPermission || needsLocationPermission) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
                if (needsNotificationPermission) {
                    FilledTonalButton(
                        onClick = onRequestNotificationPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics {
                                contentDescription =
                                    context.getString(R.string.grant_notification_permission)
                            }
                    ) {
                        Text(stringResource(R.string.grant_notification_permission))
                    }
                }
                if (needsLocationPermission) {
                    FilledTonalButton(
                        onClick = onRequestLocationPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics {
                                contentDescription =
                                    context.getString(R.string.grant_location_permission)
                            }
                    ) {
                        Text(stringResource(R.string.grant_location_permission))
                    }
                }
            }

            SettingsSection(title = stringResource(R.string.section_appearance)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.dynamic_color_title),
                    subtitle = stringResource(R.string.dynamic_color_subtitle),
                    icon = Icons.Outlined.Palette,
                    checked = settings.dynamicColorEnabled,
                    onCheckedChange = onDynamicColorChange,
                    switchDescription = stringResource(R.string.cd_dynamic_color_switch),
                    showDivider = true
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                stringResource(R.string.theme_title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.theme_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            val label = when (mode) {
                                ThemeMode.System -> stringResource(R.string.theme_system)
                                ThemeMode.Light -> stringResource(R.string.theme_light)
                                ThemeMode.Dark -> stringResource(R.string.theme_dark)
                            }
                            val icon = when (mode) {
                                ThemeMode.System -> Icons.Outlined.Palette
                                ThemeMode.Light -> Icons.Outlined.WbSunny
                                ThemeMode.Dark -> Icons.Outlined.DarkMode
                            }
                            SegmentedButton(
                                selected = settings.themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = ThemeMode.entries.size
                                ),
                                icon = {
                                    SegmentedButtonDefaults.Icon(
                                        active = settings.themeMode == mode
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                label = { Text(label) },
                                modifier = Modifier.semantics {
                                    contentDescription = context.getString(
                                        R.string.cd_theme_option,
                                        label
                                    )
                                }
                            )
                        }
                    }
                }
            }

            SettingsSection(title = stringResource(R.string.section_about)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.app_name)) },
                    supportingContent = {
                        Text(
                            stringResource(
                                R.string.about_tagline,
                                versionLabel
                            )
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.open_source_licenses)) },
                    supportingContent = { Text(stringResource(R.string.open_source_licenses_subtitle)) },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .clickable(onClick = onOpenLicenses)
                        .semantics {
                            contentDescription = context.getString(R.string.open_source_licenses)
                        }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .semantics { contentDescription = title }
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchDescription: String,
    showDivider: Boolean,
    enabled: Boolean = true
) {
    Column {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    modifier = Modifier.semantics { contentDescription = switchDescription }
                )
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        )
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        }
    }
}
