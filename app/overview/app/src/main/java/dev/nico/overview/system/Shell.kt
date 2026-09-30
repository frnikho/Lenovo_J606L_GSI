package dev.nico.overview.system

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ShellResult(val exitCode: Int, val output: String)

fun interface Shell {
    suspend fun run(command: String): ShellResult
}

/** Commande passée sur l'entrée de `su` (pas dans ses arguments, visibles dans la liste des processus). */
class SuShell : Shell {
    override suspend fun run(command: String): ShellResult = withContext(Dispatchers.IO) {
        val process = ProcessBuilder("su").redirectErrorStream(true).start()
        process.outputStream.bufferedWriter().use { input ->
            input.write(command)
            input.newLine()
            input.write("exit")
            input.newLine()
        }
        val output = process.inputStream.bufferedReader().readText()
        ShellResult(process.waitFor(), output)
    }
}

fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"
