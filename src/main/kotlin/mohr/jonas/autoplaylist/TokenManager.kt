package mohr.jonas.autoplaylist

import com.adamratzman.spotify.*
import com.adamratzman.spotify.models.Token
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.ApacheServer
import org.http4k.server.asServer
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object TokenManager {

    private val TOKEN_PATH = Path.of(System.getProperty("user.home"), ".autoplaylist", "token.secret")

    private fun loadToken(path: Path): Token {
        return Json.decodeFromString(path.readText())
    }

    private fun saveToken(path: Path, token: Token) {
        path.parent.createDirectories()
        path.writeText(Json.encodeToString(token))
    }

    private fun hasSavedToken(path: Path) = path.exists()

    private fun buildUrl(clientId: String, redirect: String): String = getSpotifyAuthorizationUrl(
        SpotifyScope.PlaylistReadPrivate,
        SpotifyScope.PlaylistModifyPrivate,
        SpotifyScope.PlaylistModifyPublic,
        SpotifyScope.UserTopRead,
        SpotifyScope.UserLibraryRead,
        clientId = clientId,
        redirectUri = URLEncoder.encode(redirect, Charset.defaultCharset())
    )

    private suspend fun buildApi(clientId: String, clientSecret: String, code: String): SpotifyClientApi = spotifyClientApi(
        clientId, clientSecret, URLEncoder.encode("http://127.0.0.1:16823", Charset.defaultCharset()), SpotifyUserAuthorization(authorizationCode = code)
    ).build()

    suspend fun buildApi(clientId: String, clientSecret: String, token: Token): SpotifyClientApi = spotifyClientApi(
        clientId, clientSecret, URLEncoder.encode("http://127.0.0.1:16823", Charset.defaultCharset()), SpotifyUserAuthorization(token = token)
    ).build()

    private fun fetchCode(port: Int): String {
        var code: String? = null
        val server = { req: Request ->
            Response(Status.OK).body("<h1>Done</h1>").also {
                code = req.query("code")
            }
        }.asServer(ApacheServer(port))
        server.start()
        while (code == null) Thread.sleep(100L)
        server.stop()
        return code!!
    }

    private fun openOrShowURL(uri: URI) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) Desktop.getDesktop().browse(uri)
        else println("Navigate to $uri in your browser")
    }

    suspend fun getToken(clientId: String, clientSecret: String): Token {
        if (hasSavedToken(TOKEN_PATH)) return loadToken(TOKEN_PATH)
        val url = buildUrl(clientId, "http://127.0.0.1:16823")
        openOrShowURL(URI.create(url))
        val autoCode = fetchCode(16823)
        val api = buildApi(clientId, clientSecret, autoCode)
        val token = api.token
        saveToken(TOKEN_PATH, token)
        return token
    }

}