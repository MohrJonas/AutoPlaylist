package mohr.jonas.autoplaylist

import com.adamratzman.spotify.SpotifyClientApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.commons.text.StringSubstitutor
import java.time.LocalDateTime

@Serializable
data class Playlist(
    @SerialName("time") val cronPattern: String? = null,
    @SerialName("name") val playlistName: String,
    @SerialName("sources") val trackSources: Array<TrackSource> = emptyArray()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Playlist
        if (cronPattern != other.cronPattern) return false
        if (playlistName != other.playlistName) return false
        if (!trackSources.contentEquals(other.trackSources)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cronPattern.hashCode()
        result = 31 * result + playlistName.hashCode()
        result = 31 * result + trackSources.contentHashCode()
        return result
    }

    suspend fun build(api: SpotifyClientApi) {
        val now = LocalDateTime.now()
        val parameters = mapOf(
            Pair("year", now.year),
            Pair("month", now.monthValue),
            Pair("day", now.dayOfMonth)
        )
        val substitutor = StringSubstitutor(parameters)
        val interpolatedPlaylistName = substitutor.replace(playlistName)
        val playlist = api.playlists.getPlaylistByName(interpolatedPlaylistName) ?: api.playlists.createClientPlaylist(interpolatedPlaylistName, public = false)
        api.playlists.removeAllClientPlaylistPlayables(playlist.id)
        trackSources.forEach { source ->
            source.selectionStrategy(playlist, source, api)
        }
    }
}
