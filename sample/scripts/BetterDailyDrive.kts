@file:Suppress("SpellCheckingInspection")

import com.adamratzman.spotify.models.*
import kotlinx.coroutines.runBlocking
import mohr.jonas.autoplaylist.toDate
import java.time.LocalDate
import java.time.chrono.ChronoLocalDate
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

private val idMap = mapOf(
    "2019" to "37i9dQZF1Etm2efh72kanV",
    "2020" to "37i9dQZF1EM92WAiw6gS3A",
    "2021" to "37i9dQZF1EUMDoJuT8yJsl",
    "2022" to "37i9dQZF1F0sijgNaJdgit",
    "Americana" to "37i9dQZF1EIflC8jblmWYq",
    "Life" to "2u4mPiVwYrsxDS3kJLq5mR",
    "MorningWire" to "3YMFdNqoNtVcCBKSUvjr8n",
    "Tagesschau" to "4QwUbrMJZ27DpjuYmN4Tun",
    "PragerU" to "5UOavREmfonjD5r3Z1eOBD",
    "1000Antworten" to "2vO8svMNB409TKDMbHFrEn",
    "Bible" to "4Pppt42NPK2XzKwNIoW7BR"
)

runBlocking {
    println("Building playlist \"${playlist.name}\"")
    val allTracks = mutableSetOf<PlayableUri>()
    print("📝 Collection tracks... ")
    allTracks.addAll(
        listOf(
            fromPlaylist(idMap.getOrThrow("2019"), 2) orThrow emptyList(),
            fromPlaylist(idMap.getOrThrow("2020"), 2) orThrow emptyList(),
            fromPlaylist(idMap.getOrThrow("2021"), 2) orThrow emptyList(),
            fromPlaylist(idMap.getOrThrow("2022"), 2) orThrow emptyList(),
            fromShow(idMap.getOrThrow("MorningWire"), releaseRange = 0L..2L)
                    or fromShow(idMap.getOrThrow("Bible"), 1)
                    orThrow fromPlaylist(idMap.getOrThrow("Life"), 4),
            fromShow(idMap.getOrThrow("Tagesschau"), releaseRange = 0L..2L)
                    orThrow fromPlaylist(idMap.getOrThrow("Life"), 1),
            fromShow(idMap.getOrThrow("1000Antworten"))
                    or fromShow(idMap.getOrThrow("PragerU"))
                    orThrow fromPlaylist(idMap.getOrThrow("Life"), 2)
        ).flatten()
    )
    println("Done")
    println("⏱ Adding up to duration... ")
    allTracks.addUpToDuration(75.minutes.inWholeSeconds) { fromPlaylist(idMap.getOrThrow("Americana"), 1)!!.first() }
    println("Done")
    allTracks.forEach { println("✔ ${it.name()}") }
    print("🎲 Shuffling tracks... ")
    val shuffledTracks = allTracks.toList().shuffled()
    println("Done")
    print("📭 Adding tracks to playlist... ")
    api.playlists.addPlayablesToClientPlaylist(playlist.id, *shuffledTracks.toTypedArray())
    println("Done")
}

/**
 *  Get a certain amount of the latest, not yet fully heard episode of a show, optionally limiting the release range
 *  @param id The id of the show
 *  @param releaseRange LongRange, limiting the release date of the episode
 *  @sample 0..Long.MAX_VALUE -> Accepts release dates from (today - 0 days) to (today - Long.MAX_VALUE)
 * */
suspend fun fromShow(id: String, amount: Int = 1, releaseRange: LongRange = 0L..100L): List<PlayableUri>? {
    val now = LocalDate.now()
    return api.shows.getShowEpisodes(id).items
        .filter { it.releaseDate != null }
        .filter { it.isPlayable }
        .filter { it.resumePoint?.fullyPlayed?.not() ?: true }
        .map { Pair(it, it.releaseDate.toDate()) }
        .filter { it.second.isAfterOrOn(now.minusDays(releaseRange.last)) && it.second.isBeforeOrOn(now.minusDays(releaseRange.first)) }
        .sortedBy { it.second }
        .reversed()
        .take(amount)
        .map { it.first.uri as PlayableUri }
        .let { if (it.size == amount) it else null }
}

/**
 *  Get a certain amount of random songs from a playlist
 *  @param id The id of the playlist
 *  @param amount The amount of songs to get
 * */
suspend fun fromPlaylist(id: String, amount: Int): List<PlayableUri>? {
    if (api.playlists.getPlaylistTracks(id).items.isEmpty()) return null
    return api.playlists.getPlaylistTracks(id).items
        .filter { it.track != null }
        .shuffled()
        .reversed()
        .take(amount)
        .map { it.track!!.uri }
}

/**
 * Picks tracks provided by the adder until the playlist is at least as long as the requested duration
 * @param desiredDuration The requested duration of the playlist, in seconds
 * @param adder The function to provide tracks
 * */
suspend fun MutableSet<PlayableUri>.addUpToDuration(desiredDuration: Long, adder: (suspend () -> PlayableUri)) {
    while (duration() < desiredDuration) {
        val track = adder()
        if (!add(track)) println("🧹 Removing duplicate song ${api.tracks.getTrack(track.id)?.name ?: "?????"}")
    }
}

/*
* Get the duration of the playlist, in seconds
* */
suspend fun MutableSet<PlayableUri>.duration() = sumOf {
    when (it) {
        is EpisodeUri -> {
            api.episodes.getEpisode(it.id)?.durationMs ?: 0
        }

        is LocalTrackUri -> {
            0
        }

        is SpotifyTrackUri -> {
            api.tracks.getTrack(it.id)?.durationMs ?: 0
        }
    }
}.milliseconds.inWholeSeconds

suspend fun PlayableUri.name(): String {
    return when (this) {
        is EpisodeUri -> {
            api.episodes.getEpisode(this.id)?.name ?: "?????"
        }

        is LocalTrackUri -> {
            "?????"
        }

        is SpotifyTrackUri -> {
            api.tracks.getTrack(this.id)?.name ?: "?????"
        }
    }
}

infix fun <T : Any> T?.or(other: T?): T? {
    if (this != null) return this
    return other
}

infix fun <T : Any> T?.orThrow(other: T?): T {
    if (this != null) return this
    if (other != null) return other
    throw IllegalArgumentException()
}

fun <K, V> Map<K, V>.getOrThrow(key: K): V = get(key)!!

fun LocalDate.isBeforeOrOn(other: ChronoLocalDate) = isBefore(other) || isEqual(other)

fun LocalDate.isAfterOrOn(other: ChronoLocalDate) = isAfter(other) || isEqual(other)