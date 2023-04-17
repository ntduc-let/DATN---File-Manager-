package com.ntduc.baseproject.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.baseproject.ui.component.main.fragment.home.video.ListFolderVideoFragment
import com.ntduc.baseproject.ui.component.main.fragment.home.video.ListVideoFragment

class FragmentVideoAdapter(
    fa: FragmentActivity,
    isFavorite: Boolean,
) : FragmentStateAdapter(fa) {
    private val listFragment = listOf(ListVideoFragment.newInstance(isFavorite), ListFolderVideoFragment.newInstance(isFavorite))

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