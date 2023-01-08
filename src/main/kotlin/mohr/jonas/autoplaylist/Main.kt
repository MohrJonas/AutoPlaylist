package mohr.jonas.autoplaylist

import it.sauronsoftware.cron4j.Predictor
import it.sauronsoftware.cron4j.Scheduler
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

fun main(args: Array<String>): Unit = runBlocking {
    val parser = ArgParser("autoplaylist")
    val watch by parser.option(ArgType.Boolean, "watch", shortName = "w", description = "Run in watch mode, triggering cron patterns").default(false)
    val playlist by parser.option(ArgType.String, "playlist", "p", description = "The playlist to now modify")
    val configFile by parser.option(ArgType.String, "config", "c", description = "The config file to use")
    val clientId by parser.option(ArgType.String, "clientId", "id", description = "The client id")
    val clientSecret by parser.option(ArgType.String, "clientSecret", "sec", description = "The client secret")
    parser.parse(args)
    if (!watch && playlist == null) throw IllegalArgumentException("Either watch (-w) or playlist (-p) have to be specified")
    val configPath = Path.of(configFile ?: System.getenv("CONFIG_FILE"))
    if (!Files.exists(configPath)) throw IllegalArgumentException("Config ${configPath.absolutePathString()} doesn't exist")
    val token = TokenManager.getToken(clientId ?: System.getenv("CLIENT_ID"), clientSecret ?: System.getenv("CLIENT_SECRET"))
    val api = TokenManager.buildApi(clientId ?: System.getenv("CLIENT_ID"), clientSecret ?: System.getenv("CLIENT_SECRET"), token)
    val instructions = Json.decodeFromString<Instructions>(withContext(Dispatchers.IO) {
        Files.readString(configPath)
    })
    if (watch) {
        println("====Schedule====")
        println("Current time: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        instructions.playlists.forEach {
            val predictor = Predictor(it.cronPattern)
            val diff = predictor.nextMatchingTime()- System.currentTimeMillis()
            println("${it.playlistName}\t[${it.cronPattern}]\tin ${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes (${TimeUnit.MILLISECONDS.toHours(diff)} hours)")
        }
        println("================")
        val scheduler = Scheduler()
        instructions.playlists.forEach {
            scheduler.schedule(it.cronPattern) {
                runBlocking {
                    it.build(api)
                }
            }
        }
        scheduler.isDaemon = false
        scheduler.start()
    } else {
        instructions.playlists.find { it.playlistName == playlist }.orElseThrow(NullPointerException("Unable to find playlist with name $playlist")).build(api)
    }
}