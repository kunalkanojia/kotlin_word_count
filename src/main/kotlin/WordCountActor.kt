
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.UntypedActor
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object WordCountActor {

    class WordCountMaster : UntypedActor() {

        internal var files: List<String> = ArrayList()
        internal var wordCount: MutableMap<String, Int> = HashMap()
        internal var requester: ActorRef = sender

        override fun onReceive(message: Any) = when (message) {
            is StartCounting -> {
                requester = sender
                val workers = createWorkers(message.numActors)
                files = getFilesList(message.docRoot)
                beginSorting(files, workers)
            }
            is WordCount -> {
                wordCount.put(message.fileName, message.count)
                if (wordCount.size == files.size) {
                    requester.tell(WordCountSuccess(wordCount), self())
                } else { }
            }
            else -> println("Received unknown message")
        }

        private fun createWorkers(numActors: Int): List<ActorRef> {
            val actorRefs = ArrayList<ActorRef>()
            for (i in 1..numActors) {
                actorRefs.add(context().actorOf(Props.create(WordCountWorker::class.java), "wc-worker-" + i))
            }
            return actorRefs
        }

        private fun getFilesList(rootPath: String): List<String> {
            val results = ArrayList<String>()
            val files = File(rootPath).listFiles()
            for (file in files ?: arrayOfNulls<File>(0)) {
                if (file!!.isFile) {
                    results.add(file.path)
                }
            }
            return results
        }

        private fun beginSorting(fileNames: List<String>, workers: List<ActorRef>) {
            for (i in fileNames.indices) {
                workers[i % workers.size].tell(FileToCount(fileNames[i]), self())
            }
        }
    }

    class WordCountWorker : UntypedActor() {

        override fun onReceive(message: Any) {
            if (message is FileToCount) {
                val count = getWordCount(message.fileName)
                sender().tell(WordCount(message.fileName, count), self())
            }

        }

        private fun getWordCount(fileName: String): Int {
            val filePath = Paths.get(fileName)
            var count = 0
            try {
                Files.newBufferedReader(filePath, StandardCharsets.UTF_8).use {
                    reader ->
                    count = reader.lines().filter { l -> !l.trim { it <= ' ' }.isEmpty() }.mapToInt { l -> l.split(" ").dropLastWhile { it.isEmpty() }.size }.sum()
                }
            } catch (e: IOException) {
                println(e.message)
            } catch (e: UncheckedIOException) {
                println(e.message)
            }

            return count
        }
    }

    //Event Classes
    data class FileToCount(val fileName: String) : Serializable

    data class WordCount(val fileName: String, val count: Int) : Serializable

    data class StartCounting(val docRoot: String, val numActors: Int) : Serializable

    data class WordCountSuccess(val result: Map<String, Int>) : Serializable
}