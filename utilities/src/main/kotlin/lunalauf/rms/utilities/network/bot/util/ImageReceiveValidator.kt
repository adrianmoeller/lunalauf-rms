package lunalauf.rms.utilities.network.bot.util

import LunaLaufLanguage.Team

class ImageReceiveValidator(
    val accept: (Team) -> Boolean
)