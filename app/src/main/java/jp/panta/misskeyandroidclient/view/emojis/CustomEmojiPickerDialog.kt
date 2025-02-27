package jp.panta.misskeyandroidclient.view.emojis

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogCustomEmojiPickerBinding
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.viewmodel.emojis.EmojiSelection
import jp.panta.misskeyandroidclient.viewmodel.emojis.EmojiSelectionViewModel
import jp.panta.misskeyandroidclient.viewmodel.emojis.Emojis

class CustomEmojiPickerDialog : BottomSheetDialogFragment(){

    private var mEmojisAdapter: EmojiListAdapter? = null
    private var mSelectionViewModel: EmojiSelectionViewModel? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)



        val emojis = (dialog.context.applicationContext as MiApplication).getCurrentInstanceMeta()?.emojis
        val binding = View.inflate(dialog.context, R.layout.dialog_custom_emoji_picker, null).let {
            dialog.setContentView(it)
            DialogCustomEmojiPickerBinding.bind(it)
        }
        binding.let{ view ->

            if(requireActivity() !is EmojiSelection){
                mSelectionViewModel = ViewModelProvider(requireActivity())[EmojiSelectionViewModel::class.java]
            }

            val adapter = EmojiListAdapter(EmojiSelectionListener(), requireActivity())
            view.emojisView.adapter = adapter
            val flexBoxLayoutManager = FlexboxLayoutManager(dialog.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            view.emojisView.layoutManager = flexBoxLayoutManager
            mEmojisAdapter = adapter
            Log.d("PickerDialog", "アダプターをセットアップしました")
            emojis?.let{
                mEmojisAdapter?.submitList(Emojis.categoryBy(it))
            }
            /*view.inputEmoji.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) = Unit
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = (s?: "").toString()
                    if(text.isBlank()){
                        emojis?.let{
                            mEmojisAdapter?.submitList(Emojis.categoryBy(it))
                        }
                    }else{
                        emojis?.filter{
                            it.name.contains(text)
                        }?.let{
                            mEmojisAdapter?.submitList(it.map{ emoji ->
                                Emojis.CustomEmoji(emoji)
                            })
                        }
                    }
                }
            })*/


        }
        return dialog
    }

    inner class EmojiSelectionListener : EmojiSelection{

        val activity = requireActivity()

        override fun onSelect(emoji: String) {
            if(activity is EmojiSelection){
                activity.onSelect(emoji)
            }else{
                mSelectionViewModel?.onSelect(emoji)
            }
            dismiss()
        }

        override fun onSelect(emoji: Emoji) {
            if(activity is EmojiSelection){
                activity.onSelect(emoji)
            }else{
                mSelectionViewModel?.onSelect(emoji)
            }
            dismiss()
        }
    }
}