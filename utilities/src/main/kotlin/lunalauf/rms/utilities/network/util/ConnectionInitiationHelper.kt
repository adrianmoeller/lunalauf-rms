package lunalauf.rms.utilities.network.util

import java.util.*
import kotlin.math.abs

object ConnectionInitiationHelper {
    private val random = Random()
    val synMessage: String
        get() = random.nextLong().toString()

    @Throws(NumberFormatException::class)
    fun getAckMessage(synMessage: String): String {
        val number = synMessage.toLong()
        return (abs(number) - 11).toString()
    }
}
