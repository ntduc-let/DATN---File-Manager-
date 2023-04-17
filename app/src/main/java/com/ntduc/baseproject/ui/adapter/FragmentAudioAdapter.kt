package com.ntduc.baseproject.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.baseproject.ui.component.main.fragment.home.audio.ListAudioFragment
import com.ntduc.baseproject.ui.component.main.fragment.home.audio.ListFolderAudioFragment
import com.ntduc.baseproject.ui.component.main.fragment.home.audio.PlaylistAudioFragment

class FragmentAudioAdapter(
    fa: FragmentActivity,
    isFavorite: Boolean,
) : FragmentStateAdapter(fa) {
    private val listFragment = listOf(
        ListAudioFragment.newInstance(isFavorite),
        ListFolderAudioFragment.newInstance(isFavorite),
//        PlaylistAudioFragment()
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