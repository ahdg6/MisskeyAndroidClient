package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.databinding.ActivityListListBinding
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.view.list.ListListAdapter
import jp.panta.misskeyandroidclient.view.list.UserListEditorDialog
import jp.panta.misskeyandroidclient.viewmodel.list.ListListViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListPullPushUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ListListActivity : AppCompatActivity(), ListListAdapter.OnTryToEditCallback, UserListEditorDialog.OnSubmittedListener{

    companion object{

        private const val EXTRA_ADD_USER_ID = "jp.panta.misskeyandroidclient.extra.ADD_USER_ID"

        private const val USER_LIST_ACTIVITY_RESULT_CODE = 12

        fun newInstance(context: Context, addUserId: User.Id?): Intent {
            return Intent(context, ListListActivity::class.java).apply {
                addUserId?.let {
                    putExtra(EXTRA_ADD_USER_ID, addUserId)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private var mListListViewModel: ListListViewModel? = null

    private var mPullPushUserViewModelEventDisposable: Disposable? = null

    private lateinit var mBinding: ActivityListListBinding

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list_list)

        val addUserId = intent.getSerializableExtra(EXTRA_ADD_USER_ID) as? User.Id

        val miCore = application as MiCore

        val layoutManager = LinearLayoutManager(this)
        mListListViewModel = ViewModelProvider(this, ListListViewModel.Factory(miCore))[ListListViewModel::class.java]

        val listAdapter =
        if(addUserId == null){
            ListListAdapter(
                mListListViewModel!!,
                this,
                this
            )
        }else{
            val pullPushUserViewModel = ViewModelProvider(this, UserListPullPushUserViewModel.Factory(miCore))[UserListPullPushUserViewModel::class.java]

            miCore.getCurrentAccount().filterNotNull().onEach{
                pullPushUserViewModel.account.value = it
            }.launchIn(lifecycleScope)

            if(mPullPushUserViewModelEventDisposable?.isDisposed == true){
                mPullPushUserViewModelEventDisposable = pullPushUserViewModel.pullPushEvent.subscribe {
                    mListListViewModel?.fetch()
                }
            }

            ListListAdapter(
                mListListViewModel!!,
                this,
                this,
                addUserId,
                pullPushUserViewModel
            )
        }


        mBinding.contentListList.listListView.adapter = listAdapter
        mBinding.contentListList.listListView.layoutManager = layoutManager
        mListListViewModel?.userListList?.observe(this, { userListList ->
            listAdapter.submitList(userListList)
        })


        setUpObservers()
        mBinding.addListButton.setOnClickListener {
            val dialog = UserListEditorDialog.newInstance()
            dialog.show(supportFragmentManager, "")
        }
    }



    @ExperimentalCoroutinesApi
    private fun setUpObservers(){
        mListListViewModel?.showUserDetailEvent?.removeObserver(showUserListDetail)
        mListListViewModel?.showUserDetailEvent?.observe(this, showUserListDetail)

    }

    private val showUserListDetail = Observer<UserList>{ ul ->
        val intent = UserListDetailActivity.newIntent(this, ul.id)
        startActivityForResult(intent, USER_LIST_ACTIVITY_RESULT_CODE)
    }



    override fun onEdit(userList: UserList?) {
        userList?: return

        val intent = UserListDetailActivity.newIntent(this, userList.id)
        intent.action = UserListDetailActivity.ACTION_EDIT_NAME
        startActivityForResult(intent, USER_LIST_ACTIVITY_RESULT_CODE)
    }


    @ExperimentalCoroutinesApi
    override fun onSubmit(name: String) {
        mListListViewModel?.createUserList(name)
    }

    @ExperimentalCoroutinesApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            USER_LIST_ACTIVITY_RESULT_CODE ->{
                if(resultCode == RESULT_OK){
                    val updated = data?.getSerializableExtra(UserListDetailActivity.EXTRA_UPDATED_USER_LIST) as? UserList
                    if(updated != null){
                        mListListViewModel?.onUserListUpdated(updated)
                    }

                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        mPullPushUserViewModelEventDisposable?.dispose()
        mPullPushUserViewModelEventDisposable = null
    }
}
