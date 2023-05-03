package com.ntduc.baseproject.ui.component.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.ActivityImageViewerBinding
import com.ntduc.baseproject.ui.adapter.ImageViewerAdapter
import com.ntduc.baseproject.ui.base.BaseActivity
import com.ntduc.baseproject.utils.file.share
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ImageViewerActivity : BaseActivity<ActivityImageViewerBinding>(R.layout.activity_image_viewer) {

    private lateinit var imageViewerAdapter: ImageViewerAdapter

    private var listImage: ArrayList<BaseImage> = arrayListOf()
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ImageViewerScreen)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        super.initView()

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        binding.toolbar.setBackgroundResource(android.R.color.transparent)
        binding.toolbar.setNavigationIcon(com.wxiwei.office.officereader.R.drawable.ic_back_24)
        binding.toolbar.setTitleTextAppearance(this, R.style.TitleToolBar)

        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        imageViewerAdapter = ImageViewerAdapter(this@ImageViewerActivity)
        binding.vp.adapter = imageViewerAdapter
    }

    override fun initData() {
        super.initData()

        listImage = intent.getParcelableArrayListExtra(LIST_IMAGE) ?: return
        imageViewerAdapter.updateData(listImage)

        currentPosition = intent.getIntExtra(CURRENT_POSITION_IMAGE, 0)
        supportActionBar?.title = listImage[currentPosition].displayName
        binding.vp.currentItem = currentPosition
    }

    override fun addEvent() {
        super.addEvent()

        binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                supportActionBar?.title = listImage[currentPosition].displayName
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.wxiwei.office.officereader.R.menu.menu_app_with_jump, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            onBackPressed()
            return false
        } else if (itemId == com.wxiwei.office.officereader.R.id.share_file) {
            File(listImage[currentPosition].data!!).share(this@ImageViewerActivity, "$packageName.provider")
            return false
        }
        return true
    }

    companion object {
        const val LIST_IMAGE = "EXTRA_FILE_PATH"
        const val CURRENT_POSITION_IMAGE = "EXTRA_BACK_TO_MAIN_APP"

        fun openFile(context: Context, list: ArrayList<BaseImage>, currentPosition: Int) {
            context.startActivity(Intent(context, ImageViewerActivity::class.java).apply {
                putParcelableArrayListExtra(LIST_IMAGE, list)
                putExtra(CURRENT_POSITION_IMAGE, currentPosition)
            })
        }
    }
}