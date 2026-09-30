package dev.nico.overview.settings

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object PlacesCodec {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(Place.serializer())

    fun encode(places: List<Place>): String = json.encodeToString(serializer, places)

    fun decode(raw: String?): List<Place> =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }.orEmpty()
}
