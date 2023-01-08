package mohr.jonas.autoplaylist.strategies

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.PlayableUri
import com.adamratzman.spotify.models.Playlist
import mohr.jonas.autoplaylist.TrackSource
import mohr.jonas.autoplaylist.toDate
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.time.LocalDateTime

class LatestSelectionStrategy(private val amount: Int = 1) :
    SelectionStrategy() {

    override suspend fun invoke(playlist: Playlist, source: TrackSource, api: SpotifyClientApi) {
        when (source.trackType) {
            "show" -> {
                val episodes = api.shows.getShowEpisodes(source.trackId).items.sortedBy { it.releaseDate.toDate() }.reversed().take(amount)
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *episodes.map { it.uri as PlayableUri }.toTypedArray())
            }

            "playlist" -> {
                val tracks = api.playlists.getPlaylistTracks(source.trackId).items
                    .sortedBy { LocalDateTime.parse(it.addedAt ?: "1970-01-01T00:00:00Z", addedAtFormatter) }.reversed().take(amount)
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *tracks.map { it.track!!.uri }.toTypedArray())
            }

            "artist" -> {
                val tracks = api.artists.getArtistAlbums(source.trackId).items
                    .sortedBy { it.releaseDate.toDate() }
                    .reversed()
                    .map { api.albums.getAlbumTracks(it.id).items }.flatten()
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *tracks.map { it.uri as PlayableUri }.toTypedArray())
            }

            else -> throw IllegalArgumentException("Unsupported track type ${source.trackType}")
        }
    }

    override fun toString(): String =
        ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false, SelectionStrategy::class.java)
}