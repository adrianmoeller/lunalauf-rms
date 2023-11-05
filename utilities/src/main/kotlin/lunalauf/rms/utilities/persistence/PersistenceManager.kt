package lunalauf.rms.utilities.persistence

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class PersistenceManager {
    companion object {
        private val logger = LoggerFactory.getLogger(PersistenceManager::class.java)
        private const val FILE_ENDING = ".llconf"

        @Throws(PersistenceException::class)
        private fun <T : PersistenceContainer> getInstance(typeOfContent: Class<T>): T {
            return try {
                val constructor = typeOfContent.getDeclaredConstructor()
                constructor.newInstance()
            } catch (e: Exception) {
                val message = "An implemented PreferenceHolder must have a public no-argument constructor."
                logger.error(message, e)
                throw PersistenceException(message, e)
            }
        }
    }

    private val gson: Gson = Gson()

    @Throws(PersistenceException::class)
    fun <T : PersistenceContainer> load(typeOfContent: Class<T>): T {
        val instance = getInstance(typeOfContent)
        val filePath = instance.fileName + FILE_ENDING
        val file = File(filePath)
        try {
            if (file.createNewFile()) {
                FileWriter(file).use { writer -> gson.toJson(instance, writer) }
                logger.info("New file created and loaded: {}", file.path)
                return instance
            } else {
                FileReader(file).use { reader ->
                    val loadedInstance = gson.fromJson(reader, typeOfContent)
                    logger.info("File loaded: {}", file.path)
                    return loadedInstance
                }
            }
        } catch (e: IOException) {
            val message = "Could not load file."
            logger.error(message, e)
            throw PersistenceException(message, e)
        }
    }

    @Throws(PersistenceException::class)
    fun <T : PersistenceContainer> save(content: T) {
        val filePath = content.fileName + FILE_ENDING
        val file = File(filePath)
        try {
            file.createNewFile()
            FileWriter(file).use { writer -> gson.toJson(content, writer) }
            logger.info("File saved: {}", file.path)
        } catch (e: IOException) {
            val message = "Could not save file."
            logger.error(message, e)
            throw PersistenceException(message, e)
        }
    }

    class PersistenceException(
        override val message: String,
        override val cause: Throwable
    ) : Exception()
}
