package jp.panta.misskeyandroidclient.view.drive

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogCreateFolderBinding
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel

class CreateFolderDialog : AppCompatDialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_create_folder, null)
        val binding = DialogCreateFolderBinding.bind(view)
        dialog.setContentView(view)
        val directoryViewModel = ViewModelProvider(requireActivity())[DirectoryViewModel::class.java]
        binding.okButton.setOnClickListener {
            val name = binding.editFolderName.text.toString()
            if(name.isNotBlank()){
                directoryViewModel.createFolder(name)
                dismiss()

            }
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return dialog
    }
}