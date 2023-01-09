import com.adamratzman.spotify.models.PlayableUri
import com.adamratzman.spotify.models.PlaylistTrack
import com.adamratzman.spotify.models.SimpleEpisode
import kotlinx.coroutines.runBlocking
import mohr.jonas.autoplaylist.toDate

runBlocking {
    val allTracks = mutableListOf<PlayableUri>()
    allTracks.addAll(playlist("37i9dQZF1Etm2efh72kanV", 2).map { it.track!!.uri })
    allTracks.addAll(playlist("37i9dQZF1EM92WAiw6gS3A", 2).map { it.track!!.uri })
    allTracks.addAll(playlist("37i9dQZF1EUMDoJuT8yJsl", 2).map { it.track!!.uri })
    allTracks.addAll(playlist("37i9dQZF1F0sijgNaJdgit", 2).map { it.track!!.uri })
    allTracks.addAll(playlist("37i9dQZF1EIflC8jblmWYq", 2).map { it.track!!.uri })
    allTracks.addAll(playlist("2koRC8UzIVD14JqkCvzOTC", 2).map { it.track!!.uri })
    show("3YMFdNqoNtVcCBKSUvjr8n")?.let { allTracks.add(it.uri as PlayableUri) }
    show("4QwUbrMJZ27DpjuYmN4Tun")?.let { allTracks.add(it.uri as PlayableUri) }
    show("2vO8svMNB409TKDMbHFrEn")?.let { allTracks.add(it.uri as PlayableUri) }
    println("🎲 Shuffling tracks")
    allTracks.shuffle()
    api.playlists.addPlayablesToClientPlaylist(playlist.id, *allTracks.toTypedArray())
}

suspend fun show(id: String): SimpleEpisode? {
    val episodes = api.shows.getShowEpisodes(id).items
        .filter { it.resumePoint?.fullyPlayed?.not() ?: true }
        .filter { it.releaseDate != null }
        .filter { it.isPlayable }
        .sortedBy { it.releaseDate!!.toDate() }
    return episodes.lastOrNull().also {
        if (it != null) println("✔ Added MorningWire episode ${it.name}")
        else println("❌ Not adding any new MorningWire episode")
    }
}

suspend fun playlist(id: String, amount: Int): List<PlaylistTrack> {
    return api.playlists.getPlaylistTracks(id).items
        .filter { it.track != null }
        .shuffled()
        .take(amount)
        .also {
            it.forEach {
                it.track?.asTrack?.let { println("✔ Adding track ${it.name}") }
            }
        }
}