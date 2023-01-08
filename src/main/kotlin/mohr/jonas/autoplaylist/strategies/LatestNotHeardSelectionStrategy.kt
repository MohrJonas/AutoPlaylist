package mohr.jonas.autoplaylist.strategies

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.PlayableUri
import com.adamratzman.spotify.models.Playlist
import mohr.jonas.autoplaylist.TrackSource
import mohr.jonas.autoplaylist.toDate
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class LatestNotHeardSelectionStrategy(private val amount: Int) : SelectionStrategy() {

    override suspend fun invoke(playlist: Playlist, source: TrackSource, api: SpotifyClientApi) {
        when (source.trackType) {
            "show" -> {
                val episodes = api.shows.getShowEpisodes(source.trackId).items
                    .sortedBy { it.releaseDate.toDate() }
                    .reversed()
                    .filter { !(it.resumePoint?.fullyPlayed ?: false) }
                    .take(amount)
                api.playlists.addPlayablesToClientPlaylist(playlist.id, *episodes.map { it.uri as PlayableUri }.toTypedArray())
            }

            else -> throw IllegalArgumentException("Unhandled track type ${source.trackType}")
        }
    }

    override fun toString(): String =
        ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false, SelectionStrategy::class.java)
}