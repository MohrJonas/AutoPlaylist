package mohr.jonas.autoplaylist

import com.adamratzman.spotify.SpotifyClientApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.commons.text.StringSubstitutor
import java.io.FileNotFoundException
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

@Serializable
data class Playlist(
    @SerialName("time") val cronPattern: String? = null,
    @SerialName("name") val playlistName: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Playlist
        if (cronPattern != other.cronPattern) return false
        if (playlistName != other.playlistName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cronPattern.hashCode()
        result = 31 * result + playlistName.hashCode()
        return result
    }

    suspend fun build(api: SpotifyClientApi, scriptDirectoryName: String, configPath: Path) {
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
        val scriptPath = configPath.parent.resolve(scriptDirectoryName).resolve("$playlistName.kts")
        if (!scriptPath.exists()) throw FileNotFoundException("Script with path ${scriptPath.absolutePathString()} doesn't exist")
        val compileConfiguration = createJvmCompilationConfigurationFromTemplate<Script>()
        val evaluationConfiguration = ScriptEvaluationConfiguration {
            providedProperties(
                mapOf(
                    "api" to api,
                    "playlist" to playlist
                )
            )
        }
        println(BasicJvmScriptingHost().eval(scriptPath.toFile().toScriptSource(), compileConfiguration, evaluationConfiguration))
    }
}
