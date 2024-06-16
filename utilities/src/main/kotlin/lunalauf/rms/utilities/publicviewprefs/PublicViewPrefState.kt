package lunalauf.rms.utilities.publicviewprefs

data class PublicViewPrefState(
    val cmn_teamsHeight: Float = 0.6f,
    val cmn_poolSponsorWidth: Float = 0.1f,
    val cmn_borderBrightness: Float = 0.7f,
    val tms_fontScale: Float = 1.0f,
    val tms_colWidth_placement: Float = 1f,
    val tms_colWidth_rounds: Float = 1f,
    val tms_colWidth_funfactors: Float = 1f,
    val tms_colWidth_sum: Float = 1f,
    val tms_colWidth_contribution: Float = 1f,
    val rns_fontScale: Float = 1f,
    val rns_numOfRows: Int = 5,
    val rns_colWidth_rounds: Float = 1f,
    val rns_colWidth_contribution: Float = 1f,
    val ps_fontScale: Float = 1f
) {
    fun toPersistenceContainer() = PublicViewPrefContainer().also {
        it.cmn_teamsHeight = cmn_teamsHeight
        it.cmn_poolSponsorWidth = cmn_poolSponsorWidth
        it.cmn_borderBrightness = cmn_borderBrightness
        it.tms_fontScale = tms_fontScale
        it.tms_colWidth_placement = tms_colWidth_placement
        it.tms_colWidth_rounds = tms_colWidth_rounds
        it.tms_colWidth_funfactors = tms_colWidth_funfactors
        it.tms_colWidth_sum = tms_colWidth_sum
        it.tms_colWidth_contribution = tms_colWidth_contribution
        it.rns_fontScale = rns_fontScale
        it.rns_numOfRows = rns_numOfRows
        it.rns_colWidth_rounds = rns_colWidth_rounds
        it.rns_colWidth_contribution = rns_colWidth_contribution
        it.ps_fontScale = ps_fontScale
    }
}

fun PublicViewPrefContainer.toState() = PublicViewPrefState(
    cmn_teamsHeight = cmn_teamsHeight,
    cmn_poolSponsorWidth = cmn_poolSponsorWidth,
    cmn_borderBrightness = cmn_borderBrightness,
    tms_fontScale = tms_fontScale,
    tms_colWidth_placement = tms_colWidth_placement,
    tms_colWidth_rounds = tms_colWidth_rounds,
    tms_colWidth_funfactors = tms_colWidth_funfactors,
    tms_colWidth_sum = tms_colWidth_sum,
    tms_colWidth_contribution = tms_colWidth_contribution,
    rns_fontScale = rns_fontScale,
    rns_numOfRows = rns_numOfRows,
    rns_colWidth_rounds = rns_colWidth_rounds,
    rns_colWidth_contribution = rns_colWidth_contribution,
    ps_fontScale = ps_fontScale
)