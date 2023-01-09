package mohr.jonas.autoplaylist

import kotlinx.serialization.Serializable

@Serializable
data class Instructions(
    val scriptDirectory: String,
    val playlists: Array<Playlist> = emptyArray()
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Instructions

        if (!playlists.contentEquals(other.playlists)) return false

        return true
    }

    override fun hashCode(): Int {
        return playlists.contentHashCode()
    }

}
