package jp.panta.misskeyandroidclient.model.notes.draft

import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import java.io.Serializable

data class DraftPoll(
    var choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
): Serializable


fun Poll.toDraftPoll() : DraftPoll{
    return DraftPoll(
        choices = this.choices.map {
            it.text
        },
        multiple = this.multiple,
        expiresAt = this.expiresAt?.toEpochMilliseconds()
    )
}