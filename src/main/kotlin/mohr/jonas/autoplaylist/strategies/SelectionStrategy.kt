package mohr.jonas.autoplaylist.strategies

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.*
import kotlinx.serialization.Serializable
import mohr.jonas.autoplaylist.TrackSource
import mohr.jonas.autoplaylist.noLongerAgoThan
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable(with = SelectionStrategySerializer::class)
abstract class SelectionStrategy {
    abstract suspend operator fun invoke(playlist: Playlist, source: TrackSource, api: SpotifyClientApi)

    companion object {

        val addedAtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

        suspend fun getShowEpisodes(api: SpotifyClientApi, id: String, maxReleaseTimeAgo: Int): Array<SimpleEpisode> {
            return api.shows.getShowEpisodes(id).items
                .filter { it.releaseDate.noLongerAgoThan(maxReleaseTimeAgo) }
                .toTypedArray()
        }

        suspend fun getPlaylistTracks(api: SpotifyClientApi, id: String, maxReleaseTimeAgo: Int): Array<PlaylistTrack> {
            return api.playlists.getPlaylistTracks(id).items
                .filter {
                    val time = LocalDateTime.parse(it.addedAt ?: "1970-01-01T00:00:00Z", addedAtFormatter)
                    ReleaseDate(time.year, time.monthValue, time.dayOfMonth).noLongerAgoThan(maxReleaseTimeAgo)
                }.toTypedArray()
        }

        suspend fun getArtistTracks(api: SpotifyClientApi, id: String, maxReleaseTimeAgo: Int): Array<SimpleTrack> {
            return api.artists.getArtistAlbums(id).items
                .filter { it.albumType == AlbumResultType.Album }
                .filter { it.releaseDate.noLongerAgoThan(maxReleaseTimeAgo) }
                .map { api.albums.getAlbumTracks(it.id).items }
                .flatten().toTypedArray()
        }
    }
}
