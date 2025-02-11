package lunalauf.rms.utilities.network.bot.util

import lunalauf.rms.model.internal.Team

class ImageReceiveValidator(
    val accept: (Team) -> Boolean
)