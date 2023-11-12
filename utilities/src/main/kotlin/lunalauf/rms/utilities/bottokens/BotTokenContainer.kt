package lunalauf.rms.utilities.bottokens

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.persistence.PersistenceContainer

class BotTokenContainer: PersistenceContainer {
    override val fileName: String
        get() = "bottokens"

    @SerializedName("roundCounter")
    var roundCounter = ""

    @SerializedName("runnerInfo")
    var runnerInfo = ""
}