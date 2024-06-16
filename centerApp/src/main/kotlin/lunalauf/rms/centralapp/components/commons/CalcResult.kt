package lunalauf.rms.centralapp.components.commons

sealed class CalcResult<T> {
    class Loading<T> : CalcResult<T>()
    data class Available<T>(val result: T): CalcResult<T>()
}