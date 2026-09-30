package dev.nico.overview.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class NowPlaying(
    val title: String,
    val artist: String,
    val art: Bitmap?,
    val isPlaying: Boolean,
    val packageName: String,
)

/** Toutes les sessions média, sans écouteur de notifications : `MEDIA_CONTENT_CONTROL` (priv-app). */
class MediaSource(private val context: Context) {
    @Volatile private var controller: MediaController? = null

    fun nowPlaying(): Flow<NowPlaying?> = callbackFlow {
        val manager = context.getSystemService(MediaSessionManager::class.java)
        val handler = Handler(Looper.getMainLooper())
        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) { trySend(controller?.toNowPlaying()) }
            override fun onPlaybackStateChanged(state: PlaybackState?) { trySend(controller?.toNowPlaying()) }
        }
        fun select(sessions: List<MediaController>?) {
            controller?.unregisterCallback(callback)
            controller = sessions?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: sessions?.firstOrNull()
            controller?.registerCallback(callback, handler)
            trySend(controller?.toNowPlaying())
        }
        val listener = MediaSessionManager.OnActiveSessionsChangedListener { select(it) }
        manager.addOnActiveSessionsChangedListener(listener, null, handler)
        select(manager.getActiveSessions(null))
        awaitClose {
            manager.removeOnActiveSessionsChangedListener(listener)
            controller?.unregisterCallback(callback)
        }
    }

    fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause() else c.transportControls.play()
    }

    fun next() { controller?.transportControls?.skipToNext() }

    fun previous() { controller?.transportControls?.skipToPrevious() }

    private fun MediaController.toNowPlaying(): NowPlaying? {
        val meta = metadata ?: return null
        return NowPlaying(
            title = meta.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
            artist = meta.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            art = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: meta.getBitmap(MediaMetadata.METADATA_KEY_ART),
            isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING,
            packageName = packageName,
        )
    }
}
