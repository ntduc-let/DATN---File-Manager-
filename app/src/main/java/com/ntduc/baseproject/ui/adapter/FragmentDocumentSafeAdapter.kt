package com.ntduc.baseproject.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.baseproject.ui.component.main.fragment.home.document.ListDocumentFragment
import com.ntduc.baseproject.ui.component.main.fragment.security.list.document.ListDocumentSafeFragment

class FragmentDocumentSafeAdapter(
    fa: FragmentActivity
) : FragmentStateAdapter(fa) {
    private val listFragment = listOf(
        ListDocumentSafeFragment.newInstance(ListDocumentFragment.TYPE_ALL),
        ListDocumentSafeFragment.newInstance(ListDocumentFragment.TYPE_PDF),
        ListDocumentSafeFragment.newInstance(ListDocumentFragment.TYPE_TXT),
        ListDocumentSafeFragment.newInstance(ListDocumentFragment.TYPE_DOC),
        ListDocumentSafeFragment.newInstance(ListDocumentFragment.TYPE_XLS),
        ListDocumentSafeFragment.newInstance(ListDocumentFragment.TYPE_PPT)
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