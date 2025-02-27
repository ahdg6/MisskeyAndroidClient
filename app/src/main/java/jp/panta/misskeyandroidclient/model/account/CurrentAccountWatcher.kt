package jp.panta.misskeyandroidclient.model.account

import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * アカウントを指定するとそのアカウントの状態を監視＆イベントを伝えます。
 * アカウントを指定しなかった場合現在のアカウントを返すようになります。
 * @param currentAccountId 監視するアカウントを固定する場合はここに対象のアカウントのIdを指定します。
 */
class CurrentAccountWatcher(
    private val currentAccountId: Long?,
    val accountRepository: AccountRepository
) {
    @ExperimentalCoroutinesApi
    val account = currentAccountId?.let {
        accountRepository.watchAccount(it)
    }?: accountRepository.watchCurrentAccount()

    suspend fun getAccount() : Account {
        return currentAccountId?.let {
            accountRepository.get(it)
        }?: accountRepository.getCurrentAccount()
    }
}

fun MiCore.watchAccount(currentAccountId: Long? = null) : CurrentAccountWatcher{
    return CurrentAccountWatcher(currentAccountId, this.getAccountRepository())
}