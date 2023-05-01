package com.ntduc.baseproject.ui.component.main.dialog

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_VIDEO
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.databinding.DialogImageMoreBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.loadImg
import com.orhanobut.hawk.Hawk
import java.io.File

class VideoMoreDialog : BaseDialogFragment<DialogImageMoreBinding>(contentLayoutId = R.layout.dialog_image_more, isCanceledOnTouchOutside = true) {

    companion object {
        private const val DATA = "DATA"

        fun newInstance(baseVideo: BaseVideo): VideoMoreDialog {
            val args = Bundle()
            args.putParcelable(DATA, baseVideo)

            val fragment = VideoMoreDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()

    private var baseVideo: BaseVideo? = null
    private var isFavorite = false

    override fun initView() {
        super.initView()

        baseVideo = requireArguments().getParcelable(DATA)

        isFavorite = false

        requireContext().loadImg(imgUrl = baseVideo!!.data, placeHolder = R.color.black, error = FileTypeExtension.getIconFile(baseVideo!!.data!!), view = binding.ic)

        binding.title.text = baseVideo!!.displayName
        binding.txtFavorite.text = getString(R.string.add_to_favorites)

        run breaking@{
            Hawk.get(FAVORITE_VIDEO, arrayListOf<String>()).forEach {
                if (it == baseVideo!!.data) {
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
            File(baseVideo!!.data!!).share(requireContext(), "${requireContext().packageName}.provider")
            dismiss()
        }

        binding.rename.setOnClickListener {
            onRenameListener?.let {
                it(baseVideo!!)
            }
            dismiss()
        }

        binding.delete.setOnClickListener {
            File(baseVideo!!.data!!).delete(requireContext())
            viewModel.requestAllVideos()
            dismiss()
        }

        binding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseVideo!!)
            } else {
                addFavorite(baseVideo!!)
            }
            viewModel.requestAllVideos()
            dismiss()
        }

        binding.info.setOnClickListener {
            onInfoListener?.let {
                it(baseVideo!!)
            }
            dismiss()
        }

        binding.moveToSafeFolder.setOnClickListener {
            onMoveSafeFolderListener?.let {
                it(baseVideo!!)
            }
            dismiss()
        }
    }

    private var onRenameListener: ((BaseVideo) -> Unit)? = null

    fun setOnRenameListener(listener: ((BaseVideo) -> Unit)) {
        onRenameListener = listener
    }

    private var onInfoListener: ((BaseVideo) -> Unit)? = null

    fun setOnInfoListener(listener: ((BaseVideo) -> Unit)) {
        onInfoListener = listener
    }

    private var onMoveSafeFolderListener: ((BaseVideo) -> Unit)? = null

    fun setOnMoveSafeFolderListener(listener: ((BaseVideo) -> Unit)) {
        onMoveSafeFolderListener = listener
    }

    private fun addFavorite(video: BaseVideo) {
        val favorites = Hawk.get(FAVORITE_VIDEO, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == video.data) newFavorites.remove(it)
        }

        newFavorites.add(video.data!!)

        Hawk.put(FAVORITE_VIDEO, newFavorites)
    }

    private fun removeFavorite(video: BaseVideo) {
        val favorites = Hawk.get(FAVORITE_VIDEO, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == video.data) newFavorites.remove(it)
        }

        Hawk.put(FAVORITE_VIDEO, newFavorites)
    }
}