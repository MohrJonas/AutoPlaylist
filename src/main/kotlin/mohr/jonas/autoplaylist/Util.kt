package mohr.jonas.autoplaylist

import com.adamratzman.spotify.endpoints.client.ClientPlaylistApi
import com.adamratzman.spotify.models.Playlist
import com.adamratzman.spotify.models.ReleaseDate
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.Period

suspend fun ClientPlaylistApi.getPlaylistByName(name: String): Playlist? {
    return getClientPlaylists().items.firstOrNull { it.name == name }?.toFullPlaylist()
}

inline fun <reified T> Collection<T>.randomSelection(min: Int, max: Int): List<T> {
    return this.shuffled().take((min..max).random())
}

inline fun <reified T> Array<T>.randomSelection(min: Int, max: Int): List<T> {
    return this.toList().randomSelection(min, max)
}

@Suppress("Deprecation")
fun ReleaseDate?.noLongerAgoThan(daysBack: Int): Boolean {
    if(this == null) return true
    return Period.between(LocalDate.of(year, month ?: 6, day ?: 15), LocalDate.now()).days < daysBack
}

inline operator fun <reified T> Array<T>.times(amount: Int) = Array(amount) { this }.flatten().toTypedArray()

fun ReleaseDate?.toDate(): LocalDate = (this ?: ReleaseDate(1970, 1, 1)).let {
    LocalDate.of(it.year, it.month ?: 0, it.day ?: 0)
}

inline fun <reified T> T?.orElseThrow(exception: RuntimeException): T {
    if(this == null) throw exception
    return this
}