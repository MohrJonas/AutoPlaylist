package mohr.jonas.autoplaylist.strategies

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.PlayableUri
import com.adamratzman.spotify.models.Playlist
import mohr.jonas.autoplaylist.TrackSource
import mohr.jonas.autoplaylist.randomSelection
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class RandomSelectionStrategy(private val minTimes: Int = 1, private val maxTimes: Int = 1, private val maxReleaseTimeAgo: Int = Int.MAX_VALUE) :
    SelectionStrategy() {

    override suspend fun invoke(playlist: Playlist, source: TrackSource, api: SpotifyClientApi) {
        when (source.trackType) {
            "show" -> {
                val episodes = getShowEpisodes(api, source.trackId, maxReleaseTimeAgo).randomSelection(minTimes, maxTimes)
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *episodes.map { it.uri as PlayableUri }.toTypedArray())
            }

            "playlist" -> {
                val tracks = getPlaylistTracks(api, source.trackId, maxReleaseTimeAgo).randomSelection(minTimes, maxTimes)
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *tracks.map { it.track!!.uri }.toTypedArray())
            }

            "artist" -> {
                val tracks = getArtistTracks(api, source.trackId, maxReleaseTimeAgo).randomSelection(minTimes, maxTimes)
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *tracks.map { it.uri as PlayableUri }.toTypedArray())
            }

            "song" -> {
                val songUri = api.tracks.getTrack(source.trackId)!!.uri
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *Array((minTimes..maxTimes).random()) { songUri })
            }

            else -> throw IllegalArgumentException("Unhandled track type ${source.trackType}")
        }
    }

    override fun toString(): String =
        ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false, SelectionStrategy::class.java)
}