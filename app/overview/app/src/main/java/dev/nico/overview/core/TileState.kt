package dev.nico.overview.core

sealed interface TileState<out T> {
    data object Loading : TileState<Nothing>
    data class Ready<T>(val data: T, val updatedAtMs: Long) : TileState<T>
    data class Stale<T>(val data: T, val updatedAtMs: Long) : TileState<T>
    data class Error(val message: String) : TileState<Nothing>
}

/** Après un échec : on garde la dernière donnée (affichée comme ancienne) plutôt qu'une tuile vide. */
fun <T> TileState<T>.afterFailure(message: String): TileState<T> = when (this) {
    is TileState.Ready -> TileState.Stale(data, updatedAtMs)
    is TileState.Stale -> this
    else -> TileState.Error(message)
}

fun <T> TileState<T>.dataOrNull(): T? = when (this) {
    is TileState.Ready -> data
    is TileState.Stale -> data
    else -> null
}
