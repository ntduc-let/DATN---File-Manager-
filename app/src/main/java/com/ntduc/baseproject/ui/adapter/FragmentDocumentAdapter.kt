package com.ntduc.baseproject.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.baseproject.ui.component.main.fragment.home.document.ListDocumentFragment

class FragmentDocumentAdapter(
    fa: FragmentActivity,
) : FragmentStateAdapter(fa) {
    private val listFragment = listOf(
        ListDocumentFragment.newInstance(ListDocumentFragment.TYPE_ALL),
        ListDocumentFragment.newInstance(ListDocumentFragment.TYPE_PDF),
        ListDocumentFragment.newInstance(ListDocumentFragment.TYPE_TXT),
        ListDocumentFragment.newInstance(ListDocumentFragment.TYPE_DOC),
        ListDocumentFragment.newInstance(ListDocumentFragment.TYPE_XLS),
        ListDocumentFragment.newInstance(ListDocumentFragment.TYPE_PPT)
    )

    override fun createFragment(position: Int): Fragment {
        if (position < listFragment.size) {
            return listFragment[position]
        }
        return listFragment[0]
    }

    override fun getItemCount(): Int {
        return listFragment.size
    }
}