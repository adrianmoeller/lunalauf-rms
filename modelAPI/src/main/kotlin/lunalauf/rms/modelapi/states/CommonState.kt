package lunalauf.rms.modelapi.states

data class CommonState(
    val year: Int,
    val sponsorPoolRounds: Int,
    val sponsorPoolAmount: Double,
    val additionalContribution: Double,
    val runDuration: Int
)
