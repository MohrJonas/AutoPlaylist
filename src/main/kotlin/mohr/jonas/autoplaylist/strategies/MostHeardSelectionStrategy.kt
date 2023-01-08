package mohr.jonas.autoplaylist.strategies

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.Playlist
import mohr.jonas.autoplaylist.TrackSource
import mohr.jonas.autoplaylist.randomSelection
import org.apache.commons.lang3.builder.ReflectionToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class MostHeardSelectionStrategy(private val minAmount: Int, private val maxAmount: Int) : SelectionStrategy() {

    override suspend fun invoke(playlist: Playlist, source: TrackSource, api: SpotifyClientApi) {
        val tracks = api.personalization.getTopTracks(maxAmount).items.randomSelection(minAmount, maxAmount)
        api.playlists.addPlayablesToClientPlaylist(playlist.id, *tracks.map { it.uri }.toTypedArray())
    }

    override fun toString(): String =
        ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false, SelectionStrategy::class.java)
}