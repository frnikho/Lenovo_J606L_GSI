package dev.nico.overview.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.nico.overview.R
import dev.nico.overview.settings.Place
import dev.nico.overview.settings.PlaceIcon
import dev.nico.overview.settings.Settings
import dev.nico.overview.settings.SettingsStore
import dev.nico.overview.settings.geocode
import dev.nico.overview.theme.ThemeMode
import dev.nico.overview.wifi.HotspotSettings
import dev.nico.overview.wifi.WifiSecurity
import kotlinx.coroutines.launch

/** Réglages (à l'arrêt) : composants Material standard, pas le style « conduite ». */
@Composable
fun SettingsScreen(settings: Settings, store: SettingsStore, onClose: () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.padding(32.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Réglages", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = onClose) { Text("Fermer") }
                }
                PlacesSection(settings.places, store)
                HotspotSection(settings.hotspot, store)
                ThemeSection(settings.themeMode, store)
            }
        }
    }
}

@Composable
private fun PlacesSection(places: List<Place>, store: SettingsStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(PlaceIcon.STAR) }
    var error by remember { mutableStateOf<String?>(null) }

    Text("Lieux favoris", style = MaterialTheme.typography.titleLarge)
    places.forEach { place ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${place.name} (${"%.4f".format(place.position.lat)}, ${"%.4f".format(place.position.lon)})", Modifier.weight(1f))
            IconButton(onClick = { scope.launch { store.setPlaces(places - place) } }) {
                Icon(painterResource(R.drawable.ic_delete), "Supprimer")
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(name, { name = it }, label = { Text("Nom") }, modifier = Modifier.weight(1f))
        OutlinedTextField(address, { address = it }, label = { Text("Adresse") }, modifier = Modifier.weight(2f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlaceIcon.entries.forEach { option ->
            FilterChip(selected = icon == option, onClick = { icon = option }, label = { Text(option.label()) })
        }
    }
    Button(
        enabled = name.isNotBlank() && address.isNotBlank() && places.none { it.name == name.trim() },
        onClick = {
            scope.launch {
                val position = geocode(context, address)
                if (position == null) {
                    error = "Adresse introuvable"
                } else {
                    store.setPlaces(places + Place(name.trim(), position, icon))
                    name = ""; address = ""; error = null
                }
            }
        },
    ) { Text("Ajouter") }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun HotspotSection(hotspot: HotspotSettings, store: SettingsStore) {
    val scope = rememberCoroutineScope()
    var ssid by remember(hotspot) { mutableStateOf(hotspot.ssid) }
    var passphrase by remember(hotspot) { mutableStateOf(hotspot.passphrase) }
    var security by remember(hotspot) { mutableStateOf(hotspot.security) }

    Text("Point d'accès du téléphone", style = MaterialTheme.typography.titleLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(ssid, { ssid = it }, label = { Text("Nom (SSID)") }, modifier = Modifier.weight(1f))
        OutlinedTextField(
            passphrase, { passphrase = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WifiSecurity.entries.forEach { option ->
            FilterChip(selected = security == option, onClick = { security = option }, label = { Text(option.name) })
        }
    }
    Button(onClick = { scope.launch { store.setHotspot(HotspotSettings(ssid.trim(), security, passphrase)) } }) {
        Text("Enregistrer")
    }
}

@Composable
private fun ThemeSection(mode: ThemeMode, store: SettingsStore) {
    val scope = rememberCoroutineScope()
    Text("Thème", style = MaterialTheme.typography.titleLarge)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { option ->
            FilterChip(
                selected = mode == option,
                onClick = { scope.launch { store.setThemeMode(option) } },
                label = { Text(option.label()) },
            )
        }
    }
}

private fun PlaceIcon.label() = when (this) {
    PlaceIcon.HOME -> "Maison"
    PlaceIcon.WORK -> "Travail"
    PlaceIcon.STAR -> "Favori"
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.AUTO -> "Auto"
    ThemeMode.LIGHT -> "Clair"
    ThemeMode.DARK -> "Sombre"
}
