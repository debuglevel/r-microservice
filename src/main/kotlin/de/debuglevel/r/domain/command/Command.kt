package de.debuglevel.r.domain.command

import mu.KotlinLogging
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


class Command(
    val command: String,
    val workingDirectory: Path
) {
    private val logger = KotlinLogging.logger {}

    fun run(): CommandResult {
        logger.debug { "Running command '$command'..." }

        val parts = command.split("\\s".toRegex())
        val processBuilder = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDirectory.toFile())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            //.redirectError(ProcessBuilder.Redirect.PIPE)
            .redirectErrorStream(true)

        val startTime = System.currentTimeMillis()
        val process = processBuilder.start()

        var output = ""
        val streamGobbler = StreamGobbler(process.inputStream, Consumer<String> { output += "$it\n" })
        val streamGobblerFuture = Executors.newSingleThreadExecutor().submit(streamGobbler)
        val timedOut = process.waitFor(300, TimeUnit.SECONDS)
        streamGobblerFuture.get() // join streamGobbler thread

        val durationMilliseconds = System.currentTimeMillis() - startTime

        val exitValue = try {
            process.exitValue()
        } catch (e: Exception) {
            logger.warn(e) { "Could not retrieve exit value of process." }
            -1
        }

        val commandResult = CommandResult(
            this,
            exitValue,
            durationMilliseconds,
            process.inputStream.bufferedReader().readText()
        )

        if (timedOut) {
            logger.debug { "Running command '$command' timeouted: $commandResult" }
        }else{
            logger.debug { "Running command '$command' finished: $commandResult" }
        }

        return commandResult
    }
}