package jp.panta.misskeyandroidclient.view.notification

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionViewHelper
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData

object NotificationStatusIconHelper {

    @JvmStatic
    @BindingAdapter("notificationStatusView", "notificationReactionImageView", "notificationReactionStringView", "notificationType", "notificationReaction", "notification")
    fun LinearLayout.setStatusIcon(notificationStatusView: ImageView, notificationReactionImageView: ImageView, notificationReactionStringView: TextView, notificationType: String, notificationReaction: String?, notification: NotificationViewData){
        when(notificationType){
            "reaction" -> {
                //context: Context, reactionTextTypeView: TextView, reactionImageTypeView: ImageView,reaction: String, note: PlaneNoteViewData)
                if(notificationReaction != null && notification.noteViewData != null) {
                    ReactionViewHelper.setReactionCount(this.context, notificationReactionStringView, notificationReactionImageView,  notificationReaction, notification.noteViewData)
                }
                notificationStatusView.visibility = View.GONE

            }
            else -> {
                notificationReactionImageView.visibility = View.GONE
                notificationReactionStringView.visibility = View.GONE
                setStatusView(notificationStatusView, notificationType)
            }
        }
    }

    fun setStatusView(statusView: ImageView, type: String){
        statusView.visibility = View.VISIBLE
        when(type){
            "follow" -> statusView.setImageResource(R.drawable.ic_follow)
            "mention" -> statusView.setImageResource(R.drawable.ic_mention)
            "reply" -> statusView.setImageResource(R.drawable.ic_reply_black_24dp)
            "renote" -> statusView.setImageResource(R.drawable.ic_re_note)
            "quote" -> statusView.setImageResource(R.drawable.ic_format_quote_black_24dp)
            "pollVote" -> statusView.setImageResource(R.drawable.ic_poll_black_24dp)
            "receiveFollowRequest" -> statusView.setImageResource(R.drawable.ic_supervisor_account_black_24dp)
            "followRequestAccepted" -> statusView.setImageResource(R.drawable.ic_done_black_24dp)
            "unknown" -> statusView.setImageResource(R.drawable.ic_baseline_report_problem_24)
        }
    }
}