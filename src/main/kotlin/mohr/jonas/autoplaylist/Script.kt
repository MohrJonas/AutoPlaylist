package mohr.jonas.autoplaylist

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.Playlist
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(compilationConfiguration = ScriptConfig::class)
abstract class Script

object ScriptConfig : ScriptCompilationConfiguration({
    defaultImports(SpotifyClientApi::class, Playlist::class)
    defaultImports("kotlinx.coroutines.runBlocking")
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
    providedProperties(
        mapOf(
            "api" to KotlinType(SpotifyClientApi::class),
            "playlist" to KotlinType(Playlist::class)
        )
    )
})