package lunalauf.rms.utilities.logging

import org.apache.log4j.Level

enum class Lvl {
    ALL, DEBUG, INFO, WARN, ERROR, FATAL
}

fun Lvl.toInternal(): Level {
    return when (this) {
        Lvl.ALL -> Level.ALL
        Lvl.DEBUG -> Level.DEBUG
        Lvl.INFO -> Level.INFO
        Lvl.WARN -> Level.WARN
        Lvl.ERROR -> Level.ERROR
        Lvl.FATAL -> Level.FATAL
    }
}

fun Level.toExternal(): Lvl {
    return when (this) {
        Level.ALL -> Lvl.ALL
        Level.DEBUG -> Lvl.DEBUG
        Level.INFO -> Lvl.INFO
        Level.WARN -> Lvl.WARN
        Level.ERROR -> Lvl.ERROR
        Level.FATAL -> Lvl.FATAL
        else -> Lvl.ALL
    }
}