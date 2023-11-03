package lunalauf.rms.utilities.publicviewprefs

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.persistence.PersistenceContainer

class PublicViewPrefContainer : PersistenceContainer {
    override val fileName: String
        get() = "publicview"

    //// COMMON ////
    @SerializedName("cmn.teamsHeight")
    var cmn_teamsHeight = 0.6f

    @SerializedName("cmn.poolSponsorWidth")
    var cmn_poolSponsorWidth = 0.1f

    @SerializedName("cmn.borderBrightness")
    var cmn_borderBrightness = 0.7f

    //// TEAMS ////
    @SerializedName("tms.fontScale")
    var tms_fontScale = 1.0f

    @SerializedName("tms.colWidth.placement")
    var tms_colWidth_placement = 1f

    @SerializedName("tms.colWidth.rounds")
    var tms_colWidth_rounds = 1f

    @SerializedName("tms.colWidth.funfactors")
    var tms_colWidth_funfactors = 1f

    @SerializedName("tms.colWidth.sum")
    var tms_colWidth_sum = 1f

    @SerializedName("tms.colWidth.contribution")
    var tms_colWidth_contribution = 1f

    //// RUNNERS ////
    @SerializedName("rns.fontScale")
    var rns_fontScale = 1f

    @SerializedName("rns.numOfRows")
    var rns_numOfRows = 5

    @SerializedName("rns.colWidth.rounds")
    var rns_colWidth_rounds = 1f

    @SerializedName("rns.colWidth.contribution")
    var rns_colWidth_contribution = 1f

    //// POOL SPONSOR ////
    @SerializedName("ps.fontScale")
    var ps_fontScale = 1f
}
