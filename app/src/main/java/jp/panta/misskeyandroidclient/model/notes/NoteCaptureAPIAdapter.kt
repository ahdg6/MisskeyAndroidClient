package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * model層とNoteCaptureAPIをいい感じに接続する
 */
class NoteCaptureAPIAdapter(
    private val accountRepository: AccountRepository,
    private val noteDataSource: NoteDataSource,
    private val noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
    loggerFactory: Logger.Factory,
    cs: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NoteDataSource.Listener {

    private val logger = loggerFactory.create("NoteCaptureAPIAdapter")

    private val coroutineScope = CoroutineScope(cs.coroutineContext + dispatcher)


    init {
        noteDataSource.addEventListener(this)
    }

    private val noteIdWithListeners = mutableMapOf<Note.Id, MutableSet<(NoteDataSource.Event)->Unit>>()

    private val noteIdWithJob = mutableMapOf<Note.Id, Job>()

    private val noteUpdatedDispatcher = MutableSharedFlow<Pair<Account, NoteUpdated.Body>>()

    init{
        coroutineScope.launch(dispatcher) {
            noteUpdatedDispatcher.collect {
                handleRemoteEvent(it.first, it.second)
            }
        }
    }

    override fun on(e: NoteDataSource.Event) {

        synchronized(noteIdWithListeners) {
            noteIdWithListeners[e.noteId]?.forEach { callback ->
                coroutineScope.launch {
                    callback.invoke(e)
                }
            }

        }

    }

    @ExperimentalCoroutinesApi
    fun capture(id: Note.Id) : Flow<NoteDataSource.Event> = channelFlow {
        val account = accountRepository.get(id.accountId)

        val repositoryEventListener: (NoteDataSource.Event)->Unit = { ev ->
            trySend(ev)
        }

        synchronized(noteIdWithJob) {
            if(addRepositoryEventListener(id, repositoryEventListener)){
                logger.debug("未登録だったのでRemoteに対して購読を開始する")
                val job = noteCaptureAPIWithAccountProvider.get(account)
                    .capture(id.noteId)
                    .onEach {
                        noteUpdatedDispatcher.emit(account to it)
                    }.launchIn(coroutineScope)
                noteIdWithJob[id] = job
            }
        }

        awaitClose {
            // NoteCaptureの購読を解除する
            synchronized(noteIdWithJob) {
                // リスナーを解除する
                if(removeRepositoryEventListener(id, repositoryEventListener)){

                    // すべてのリスナーが解除されていればRemoteへの購読も解除する
                    noteIdWithJob.remove(id)?.cancel()?: run{
                        logger.warning("購読解除しようとしたところすでに解除されていた")
                    }
                }
            }
        }
    }.shareIn(coroutineScope, replay = 1, started = SharingStarted.Lazily)


    /**
     * @return Note.Idが初めてListenerに登録されるとtrueが返されます。
     */
    private fun addRepositoryEventListener(noteId: Note.Id, listener: (NoteDataSource.Event)-> Unit): Boolean {
        synchronized(noteIdWithListeners) {
            val listeners = noteIdWithListeners[noteId]
            return if(listeners.isNullOrEmpty()) {
                noteIdWithListeners[noteId] = mutableSetOf(listener)
                true
            }else{
                listeners.add(listener)
                noteIdWithListeners[noteId] = listeners
                false
            }
        }

    }

    /**
     * @return Note.Idに関連するListenerすべてが解除されるとfalseが返されます。
     */
    private fun removeRepositoryEventListener(noteId: Note.Id, listener: (NoteDataSource.Event)-> Unit): Boolean {

        synchronized(noteIdWithListeners) {
            val listeners: MutableSet<(NoteDataSource.Event) -> Unit> =
                noteIdWithListeners[noteId] ?: return false

            if(!listeners.remove(listener)){
                logger.warning("リスナーの削除に失敗しました。")
                return false
            }

            if(listeners.isEmpty()) {
                return true
            }
            return false
        }

    }

    /**
     * リポジトリを更新する
     */
    private suspend fun handleRemoteEvent(account: Account, e: NoteUpdated.Body) {
        val noteId = Note.Id(account.accountId, e.id)
        try{
            val note = noteDataSource.get(noteId)
            when(e) {
                is NoteUpdated.Body.Deleted -> {
                    noteDataSource.remove(noteId)
                }
                is NoteUpdated.Body.Reacted-> {
                    noteDataSource.add(note.onReacted(account, e))
                }
                is NoteUpdated.Body.Unreacted -> {
                    noteDataSource.add(note.onUnReacted(account, e))
                }
                is NoteUpdated.Body.PollVoted -> {
                    noteDataSource.add(note.onPollVoted(account, e))
                }

            }
        }catch(e: Exception){
            logger.warning("更新対称のノートが存在しませんでした:$noteId", e = e)
        }


    }


}