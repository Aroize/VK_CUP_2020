package ru.rain.ifmo.btask.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.vk.api.sdk.VK
import com.vk.api.sdk.utils.VKUtils
import ru.rain.ifmo.btask.R
import ru.rain.ifmo.btask.clustering.GroupClusterItem
import ru.rain.ifmo.btask.data.models.VKGroup

class ExtraInfoFragment : Fragment() {

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.extra_info_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        val group = item.item as VKGroup
        view.findViewById<ImageButton>(R.id.close_btn).setOnClickListener {
            (requireActivity() as MapsActivity).closeAdditionalInfo()
        }
        view.findViewById<TextView>(R.id.group_title).text = group.name
        view.findViewById<TextView>(R.id.address).text = group.addresses[item.index].address
        view.findViewById<TextView>(R.id.description_text).text = group.description
        view.findViewById<Button>(R.id.open_group).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, null).apply {
                if (VKUtils.isAppInstalled(requireContext(), VK_APP_PACKAGE_ID))
                    setPackage(VK_APP_PACKAGE_ID)
                data = Uri.parse("https://vk.com/${group.screenName}")
            }
            startActivity(intent)
        }
    }

    companion object {
        var item = GroupClusterItem(VKGroup(), -1)

        const val VK_APP_PACKAGE_ID = "com.vkontakte.android"
    }
}