package lunalauf.rms.utilities.logging

import org.apache.log4j.Level

enum class Lvl {
    INFO, WARN, ERROR, DEBUG
}

fun Lvl.toInternal(): Level {
    return when (this) {
        Lvl.INFO -> Level.INFO
        Lvl.WARN -> Level.WARN
        Lvl.ERROR -> Level.ERROR
        Lvl.DEBUG -> Level.DEBUG
    }
}

fun Level.toExternal(): Lvl {
    return when (this) {
        Level.INFO -> Lvl.INFO
        Level.WARN -> Lvl.WARN
        Level.ERROR -> Lvl.ERROR
        Level.DEBUG -> Lvl.DEBUG
        else -> Lvl.INFO
    }
}