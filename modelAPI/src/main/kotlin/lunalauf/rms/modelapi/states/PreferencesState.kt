package lunalauf.rms.modelapi.states

data class PreferencesState(
    val autoSaveActive: Boolean = false,
    val autoSaveInterval: Float = 30f,
    val roundThreshold: Float,
    val saveConnectionsActive: Boolean = false
)
