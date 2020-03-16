package ru.rain.ifmo.ftask.presentation.fragment

import android.animation.LayoutTransition
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.vk.api.sdk.utils.VKUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import ru.rain.ifmo.ftask.R
import ru.rain.ifmo.ftask.convertPrefix
import ru.rain.ifmo.ftask.data.models.VKGroup
import ru.rain.ifmo.ftask.data.models.VKGroupInfo
import ru.rain.ifmo.ftask.domain.interactor.GroupsInteractor
import ru.rain.ifmo.ftask.domain.interactor.GroupsInteractorImpl
import ru.rain.ifmo.ftask.presentation.activity.AuthActivity
import ru.rain.ifmo.ftask.toVKInfo

class ExtraGroupInfoFragment : Fragment() {

    private lateinit var info: VKGroupInfo
    //Oops, no DI
    private val groupsInteractor: GroupsInteractor = GroupsInteractorImpl()

    private val disposable = CompositeDisposable()

    private lateinit var title: TextView

    private lateinit var group: VKGroup

    private lateinit var mainView: View

    private lateinit var subscribers: TextView

    private lateinit var description: TextView

    private lateinit var lastPost: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = extractBundle(arguments as Bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.extra_group_info_fragment, container, false)
        (mainView as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        initViews()
        title.text = group.name
        makeRequest()
        return mainView
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun makeRequest() {
        disposable.add(
            groupsInteractor.groupInfo(group.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::screenData) { e ->
                    Log.d("ExtraInfoTag", "Exception", e)
                    makeRequest()
                }
        )
    }

    private fun screenData(info: VKGroupInfo) {
        this.info = info
        subscribers.text = getString(
            R.string.subscribers,
            info.membersCount.convertPrefix(),
            info.friendsCount.convertPrefix(),
            resources.getQuantityString(R.plurals.friends, info.friendsCount)
        )
        description.text =
            if (info.description.isNotBlank())
                info.description
            else
                getString(R.string.no_description)
        lastPost.text = (info.lastPostDate.toLong() * 1000).toVKInfo(resources)
        mainView.visibility = View.VISIBLE
    }

    private fun initViews() {
        mainView.findViewById<ImageButton>(R.id.close_btn).setOnClickListener {
            (activity as AuthActivity).closeInfo()
        }
        mainView.findViewById<Button>(R.id.open_group).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, null).apply {
                if (VKUtils.isAppInstalled(context!!, VK_APP_PACKAGE))
                    setPackage(VK_APP_PACKAGE)
                data = Uri.parse("https://vk.com/${info.link}")
            }
            startActivity(intent)
        }
        title = mainView.findViewById(R.id.group_title)
        subscribers = mainView.findViewById(R.id.subscribers_text)
        description = mainView.findViewById(R.id.description_text)
        description.movementMethod = ScrollingMovementMethod()
        lastPost = mainView.findViewById(R.id.last_post)
    }

    companion object {
        fun createBundle(vkGroup: VKGroup): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(GROUP, vkGroup)
            return bundle
        }

        fun extractBundle(bundle: Bundle): VKGroup {
            return bundle.getParcelable<VKGroup>(GROUP) as VKGroup
        }

        private const val GROUP = "vk.group"

        const val VK_APP_PACKAGE = "com.vkontakte.android"
    }
}