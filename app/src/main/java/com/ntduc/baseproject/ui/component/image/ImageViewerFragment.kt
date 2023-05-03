package com.ntduc.baseproject.ui.component.image

import android.os.Bundle
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.FragmentImageViewerBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.utils.loadImg
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageViewerFragment : BaseFragment<FragmentImageViewerBinding>(R.layout.fragment_image_viewer) {

    companion object {
        private const val DATA = "DATA"

        fun newInstance(baseImage: BaseImage): ImageViewerFragment {
            val args = Bundle()
            args.putParcelable(DATA, baseImage)

            val fragment = ImageViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView() {
        super.initView()

        val image: BaseImage = requireArguments().getParcelable(DATA) ?: return

        requireContext().loadImg(imgUrl = image.data, view = binding.viewer, placeHolder = R.color.black, error = R.drawable.ic_image)
    }
}