package com.ntduc.baseproject.ui.component.main.dialog

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_IMAGE
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.DialogImageMoreBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.loadImg
import com.orhanobut.hawk.Hawk
import java.io.File

class ImageMoreDialog : BaseDialogFragment<DialogImageMoreBinding>(contentLayoutId = R.layout.dialog_image_more, isCanceledOnTouchOutside = true) {

    companion object {
        private const val DATA = "DATA"

        fun newInstance(baseImage: BaseImage): ImageMoreDialog {
            val args = Bundle()
            args.putParcelable(DATA, baseImage)

            val fragment = ImageMoreDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()

    private var baseImage: BaseImage? = null
    private var isFavorite = false

    override fun initView() {
        super.initView()

        baseImage = requireArguments().getParcelable(DATA)

        isFavorite = false

        requireContext().loadImg(imgUrl = baseImage!!.data, placeHolder = R.color.black, error = FileTypeExtension.getIconFile(baseImage!!.data!!), view = binding.ic)

        binding.title.text = baseImage!!.displayName
        binding.txtFavorite.text = getString(R.string.add_to_favorites)

        run breaking@{
            Hawk.get(FAVORITE_IMAGE, arrayListOf<String>()).forEach {
                if (it == baseImage!!.data) {
                    isFavorite = true
                    binding.txtFavorite.text = getString(R.string.remove_from_favorites)
                    return@breaking
                }
            }
        }
    }

    override fun addEvent() {
        super.addEvent()

        binding.share.setOnClickListener {
            File(baseImage!!.data!!).share(requireContext(), "${requireContext().packageName}.provider")
            dismiss()
        }

        binding.rename.setOnClickListener {
            onRenameListener?.let {
                it(baseImage!!)
            }
            dismiss()
        }

        binding.delete.setOnClickListener {
            File(baseImage!!.data!!).delete(requireContext())
            viewModel.requestAllImages()
            dismiss()
        }

        binding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseImage!!)
            } else {
                addFavorite(baseImage!!)
            }
            viewModel.requestAllImages()
            dismiss()
        }

        binding.info.setOnClickListener {
            onInfoListener?.let {
                it(baseImage!!)
            }
            dismiss()
        }
    }

    private var onRenameListener: ((BaseImage) -> Unit)? = null

    fun setOnRenameListener(listener: ((BaseImage) -> Unit)) {
        onRenameListener = listener
    }

    private var onInfoListener: ((BaseImage) -> Unit)? = null

    fun setOnInfoListener(listener: ((BaseImage) -> Unit)) {
        onInfoListener = listener
    }

    private fun addFavorite(image: BaseImage) {
        val favorites = Hawk.get(FAVORITE_IMAGE, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == image.data) newFavorites.remove(it)
        }

        newFavorites.add(image.data!!)

        Hawk.put(FAVORITE_IMAGE, newFavorites)
    }

    private fun removeFavorite(image: BaseImage) {
        val favorites = Hawk.get(FAVORITE_IMAGE, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == image.data) newFavorites.remove(it)
        }

        Hawk.put(FAVORITE_IMAGE, newFavorites)
    }
}