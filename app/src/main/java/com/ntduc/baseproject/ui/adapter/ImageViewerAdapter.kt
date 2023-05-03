package com.ntduc.baseproject.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.ui.component.image.ImageViewerFragment

class ImageViewerAdapter(
    fa: FragmentActivity,
    var listFragment: ArrayList<ImageViewerFragment> = arrayListOf()
) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return listFragment[position]
    }

    override fun getItemCount(): Int {
        return listFragment.size
    }

    fun updateData(newImage: ArrayList<BaseImage>){
        listFragment = arrayListOf()
        newImage.forEach {
            listFragment.add(ImageViewerFragment.newInstance(it))
        }
        notifyDataSetChanged()
    }
}