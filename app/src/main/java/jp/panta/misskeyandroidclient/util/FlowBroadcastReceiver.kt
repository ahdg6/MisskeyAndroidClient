package jp.panta.misskeyandroidclient.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow


@ExperimentalCoroutinesApi
fun Context.receive(filter: IntentFilter): Flow<Intent?> {
    return channelFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                offer(intent)
            }
        }

        registerReceiver(receiver, filter)
        awaitClose {
            unregisterReceiver(receiver)
        }
    }
}