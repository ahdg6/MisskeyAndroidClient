package jp.panta.misskeyandroidclient.model.messaging.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

interface MessageDataSource {
    suspend fun add(message: Message): AddResult

    suspend fun addAll(messages: List<Message>): List<AddResult>

    suspend fun delete(messageId: Message.Id): Boolean

    suspend fun find(messageId: Message.Id): Message?
}

class InMemoryMessageDataSource(
    private val accountRepository: AccountRepository
) : MessageDataSource, UnReadMessages {

    private val messageIdAndMessage = mutableMapOf<Message.Id, Message>()
    private val messagesState = MutableStateFlow<List<Message>>(
        synchronized(messageIdAndMessage){
            messageIdAndMessage.values.toList()
        }
    )

    override suspend fun add(message: Message): AddResult {
        return createOrUpdate(message).also {
            updateState()
        }
    }

    override suspend fun addAll(messages: List<Message>): List<AddResult> {
        return messages.map {
            add(it)
        }
    }

    override suspend fun delete(messageId: Message.Id): Boolean {
        return remove(messageId).also {
            updateState()
        }
    }

    override suspend fun find(messageId: Message.Id): Message? {
        return get(messageId)
    }

    override fun findByMessagingId(messagingId: MessagingId): Flow<List<Message>> {
        return messagesState.map {
            it.filter { msg ->
                val ac = accountRepository.get(msg.id.accountId)
                messagingId == msg.messagingId(ac)
            }
        }
    }

    override fun findByAccountId(accountId: Long): Flow<List<Message>> {
        return messagesState.map {
            it.filter { msg ->
                msg.id.accountId == accountId
            }
        }
    }

    override fun findAll(): Flow<List<Message>> {
        return messagesState
    }

    private fun createOrUpdate(message: Message): AddResult {
        synchronized(messageIdAndMessage) {
            messageIdAndMessage[message.id]?.let{
                messageIdAndMessage[message.id] = message
                return AddResult.UPDATED
            }
            messageIdAndMessage[message.id] = message
            return AddResult.CREATED
        }
    }

    private fun get(messageId: Message.Id): Message? {
        synchronized(messageIdAndMessage) {
            return messageIdAndMessage[messageId]
        }
    }

    private fun remove(messageId: Message.Id): Boolean {
        synchronized(messageIdAndMessage) {
            return messageIdAndMessage.remove(messageId) != null
        }
    }

    private fun updateState() {
        synchronized(messageIdAndMessage) {
            this.messagesState.value = messageIdAndMessage.values.toList()
        }
    }

}