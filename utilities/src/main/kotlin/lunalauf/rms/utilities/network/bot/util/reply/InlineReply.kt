package lunalauf.rms.utilities.network.bot.util.reply

import org.telegram.telegrambots.meta.api.objects.Message

abstract class InlineReply(
    chatId: Long,
    wrapper: ReplyContainer,
    val message: Message
) : Reply(chatId, wrapper)
