package com.ntduc.baseproject.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.baseproject.ui.component.main.fragment.home.app.ListApkFragment
import com.ntduc.baseproject.ui.component.main.fragment.home.app.ListAppFragment

class FragmentAppAdapter(
    fa: FragmentActivity,
    isFavorite: Boolean,
) : FragmentStateAdapter(fa) {
    private val listFragment = listOf(ListAppFragment.newInstance(isFavorite), ListApkFragment.newInstance(isFavorite))

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