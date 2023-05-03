package com.ntduc.baseproject.ui.component.office

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.ui.base.BaseActivity
import com.ntduc.baseproject.ui.component.main.MainActivity
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.toast.shortToast
import com.wxiwei.office.common.IOfficeToPicture
import com.wxiwei.office.constant.EventConstant
import com.wxiwei.office.constant.MainConstant
import com.wxiwei.office.constant.wp.WPViewConstant
import com.wxiwei.office.officereader.AppFrame
import com.wxiwei.office.officereader.FindToolBar
import com.wxiwei.office.officereader.beans.AImageButton
import com.wxiwei.office.officereader.beans.AImageCheckButton
import com.wxiwei.office.officereader.beans.AToolsbar
import com.wxiwei.office.officereader.beans.CalloutToolsbar
import com.wxiwei.office.officereader.beans.PDFToolsbar
import com.wxiwei.office.officereader.beans.PGToolsbar
import com.wxiwei.office.officereader.beans.SSToolsbar
import com.wxiwei.office.officereader.beans.WPToolsbar
import com.wxiwei.office.officereader.database.DBService
import com.wxiwei.office.officereader.databinding.ActivityOfficeDetailBinding
import com.wxiwei.office.res.ResKit
import com.wxiwei.office.ss.sheetbar.SheetBar
import com.wxiwei.office.system.FileKit
import com.wxiwei.office.system.IMainFrame
import com.wxiwei.office.system.MainControl
import com.wxiwei.office.system.beans.pagelist.IPageListViewListener
import com.wxiwei.office.system.dialog.ColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Locale
import com.wxiwei.office.officereader.R as ROffice

@AndroidEntryPoint
class OfficeReaderActivity : BaseActivity<ActivityOfficeDetailBinding>(ROffice.layout.activity_office_detail), IMainFrame {
    private lateinit var documentType: String

    private fun changeStatusBarColor(colorResourceId: Int) {
        window.statusBarColor = ContextCompat.getColor(this, colorResourceId)
    }

    private fun ignoreType(): Boolean = false

    /**
     * 构造器
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        if (!ignoreType()) {
            documentType = intent.getStringExtra("type") ?: FileTypeExtension.DOC
            setTheme(
                when (documentType) {
                    FileTypeExtension.XLS -> R.style.XLS
                    FileTypeExtension.DOC -> R.style.DOC
                    FileTypeExtension.PPT -> R.style.PPT
                    FileTypeExtension.PDF -> R.style.PDF
                    FileTypeExtension.TXT -> R.style.TXT
                    else -> R.style.MainFullScreen
                }
            )
        }

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        super.initView()

        control = MainControl(this)
        appFrame = AppFrame(applicationContext)
        control!!.setOffictToPicture(object : IOfficeToPicture {
            private var bitmap: Bitmap? = null
            override fun getBitmap(componentWidth: Int, componentHeight: Int): Bitmap? {
                if (componentWidth == 0 || componentHeight == 0) {
                    return null
                }
                if ((bitmap == null) || (bitmap!!.width != componentWidth) || (bitmap!!.height != componentHeight)) {
                    // custom picture size
                    if (bitmap != null) {
                        bitmap!!.recycle()
                    }
                    bitmap = Bitmap.createBitmap(componentWidth, componentHeight, Bitmap.Config.ARGB_8888)
                }
                return (bitmap)!!
            }

            override fun callBack(bitmap: Bitmap) {}
            override fun setModeType(modeType: Byte) {}
            override fun getModeType(): Byte = IOfficeToPicture.VIEW_CHANGE_END
            override fun isZoom(): Boolean = false
            override fun dispose() {}
        })

        changeToolbarColorWithExtension()

        binding.toolbarOffice.setNavigationIcon(ROffice.drawable.ic_back_24)
        binding.toolbarOffice.setTitleTextAppearance(this, ROffice.style.TitleToolBar2)

        setSupportActionBar(binding.toolbarOffice)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        binding.viewerOffice.removeAllViews()
        binding.viewerOffice.addView(appFrame)
    }

    override fun initData() {
        super.initData()

        binding.viewerOffice.post {
            val extraFilePath = intent.getStringExtra(EXTRA_FILE_PATH)
            val tmp: Boolean = checkFileExist(extraFilePath)

            if (extraFilePath != null && tmp) {
                filePath = extraFilePath
                init()
            } else {
                shortToast("File not exist")
                onBackPressed()
            }
        }
    }

    private fun checkFileExist(path: String?): Boolean {
        if (path == null) return false;
        val file = File(path)

        return file.exists() && file.length() > 0;
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(ROffice.menu.menu_app_with_jump, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            onBackPressed()
            return false
        } else if (itemId == ROffice.id.share_file) {
            filePath?.apply {
                File(this).share(this@OfficeReaderActivity, "$packageName.provider")
            }
            return false
        }
        return true
    }

    private fun setFullScreen(isFullScreen: Boolean) {
        fullscreenByMenu = isFullScreen
        if (fullscreenByMenu) {
            //hide title and tool bar
            toolsbar!!.visibility = View.GONE
            gapView!!.visibility = View.GONE
            binding.toolbarOffice.visibility = View.GONE
        } else {
            toolsbar!!.visibility = View.VISIBLE
            gapView!!.visibility = View.VISIBLE
            binding.toolbarOffice.visibility = View.VISIBLE
        }
    }

    fun setButtonEnabled(enabled: Boolean) {
        if (fullscreen) {
            pageUp!!.isEnabled = enabled
            pageDown!!.isEnabled = enabled
            penButton!!.isEnabled = enabled
            eraserButton!!.isEnabled = enabled
            settingsButton!!.isEnabled = enabled
        }
    }

    override fun onPause() {
        super.onPause()
        val obj = control!!.getActionValue(EventConstant.PG_SLIDESHOW, null)
        if (obj != null && obj as Boolean) {
            wm!!.removeView(pageUp)
            wm!!.removeView(pageDown)
            wm!!.removeView(penButton)
            wm!!.removeView(eraserButton)
            wm!!.removeView(settingsButton)
        }
    }

    override fun onResume() {
        super.onResume()
        val obj = control!!.getActionValue(EventConstant.PG_SLIDESHOW, null)
        if (obj != null && obj as Boolean) {
            wmParams!!.gravity = Gravity.END or Gravity.TOP
            wmParams!!.x = MainConstant.GAP
            wm!!.addView(penButton, wmParams)
            wmParams!!.gravity = Gravity.END or Gravity.TOP
            wmParams!!.x = MainConstant.GAP
            wmParams!!.y = wmParams!!.height
            wm!!.addView(eraserButton, wmParams)
            wmParams!!.gravity = Gravity.END or Gravity.TOP
            wmParams!!.x = MainConstant.GAP
            wmParams!!.y = wmParams!!.height * 2
            wm!!.addView(settingsButton, wmParams)
            wmParams!!.gravity = Gravity.START or Gravity.CENTER
            wmParams!!.x = MainConstant.GAP
            wmParams!!.y = 0
            wm!!.addView(pageUp, wmParams)
            wmParams!!.gravity = Gravity.START or Gravity.CENTER
            wm!!.addView(pageDown, wmParams)
        }
    }

    /**
     *
     */
    override fun onBackPressed() {
        if (isSearchbarActive) {
            showSearchBar(false)
            updateToolsbarStatus()
        } else {
            val obj = control!!.getActionValue(EventConstant.PG_SLIDESHOW, null)
            if (obj != null && obj as Boolean) {
                fullScreen(false)
                //
                control!!.actionEvent(EventConstant.PG_SLIDESHOW_END, null)
            } else if (fullscreenByMenu) {
                setFullScreen(false)
            } else {
                if (control!!.reader != null) {
                    control!!.reader.abortReader()
                }
                if (dbService != null) {
                    if (marked != dbService!!.queryItem(MainConstant.TABLE_STAR, filePath)) {
                        if (!marked) {
                            dbService!!.deleteItem(MainConstant.TABLE_STAR, filePath)
                        } else {
                            dbService!!.insertStarFiles(MainConstant.TABLE_STAR, filePath)
                        }
                        val intent = Intent()
                        intent.putExtra(MainConstant.INTENT_FILED_MARK_STATUS, marked)
                        setResult(RESULT_OK, intent)
                    }
                }
                backToMainOrListFile()
            }
        }
        if (intent.getBooleanExtra(EXTRA_BACK_TO_MAIN_APP, false)) {
            startActivity(
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }

    private fun backToMainOrListFile() {
        super.onBackPressed()
    }

    /**
     *
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isSearchbarActive) {
            searchBar!!.onConfigurationChanged(newConfig)
        }
    }

    /**
     *
     */
    override fun onDestroy() {
        dispose()
        clearTempFolder(this)
        super.onDestroy()
    }

    private fun clearTempFolder(context: Context) {
        val rootDir = context.filesDir
        val containTempFileDir = File(rootDir, "Temp_folder_123123")
        deleteRecursive(containTempFileDir)
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        try {
            if (fileOrDirectory.isDirectory) {
                fileOrDirectory.listFiles()?.forEach { child ->
                    deleteRecursive(child)
                }
            }
            fileOrDirectory.delete()
        } catch (ignored: Exception) {
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see IMainFrame.showProgressBar
     */
    override fun showProgressBar(visible: Boolean) {
        setProgressBarIndeterminateVisibility(visible)
    }

    private fun changeToolbarColorWithExtension() {
        val colorResourceId: Int = when (documentType) {
            FileTypeExtension.DOC -> R.color.doc_color
            FileTypeExtension.XLS -> R.color.xls_color
            FileTypeExtension.PPT -> R.color.ppt_color
            FileTypeExtension.PDF -> R.color.pdf_color
            FileTypeExtension.TXT -> R.color.txt_color
            else -> R.color.blue_main
        }
        changeStatusBarColor(colorResourceId)
        binding.toolbarOffice.setBackgroundResource(colorResourceId)
    }

    private fun init() {
        dbService = DBService(applicationContext)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // 显示打开文件名称
        val index = filePath!!.lastIndexOf(File.separator)
        title = if (index > 0) {
            filePath!!.substring(index + 1)
        } else {
            filePath
        }
        val isSupport = FileKit.instance().isSupport(filePath)
        //写入本地数据库
        if (isSupport && dbService != null) {
            dbService!!.insertRecentFiles(MainConstant.TABLE_RECENT, filePath)
        }
        // create view
        createView()
        openDocumentFile()
    }

    private fun openDocumentFile() {
        // open file
        control!!.openFile(filePath)
        // initialization marked
        initMarked()
    }

    /**
     * true: show message when zooming
     * false: not show message when zooming
     */
    override fun isShowZoomingMsg(): Boolean {
        return false
    }

    /**
     * true: pop up dialog when throw err
     * false: not pop up dialog when throw err
     */
    override fun isPopUpErrorDlg(): Boolean {
        return true
    }

    override fun newFatalOccurs(fatalDetail: String) {

    }

    override fun outOfMemoryOccurs() {
        runOnUiThread {}
    }

    /**
     *
     */
    private fun createView() {
        // word
        val file = filePath!!.lowercase(Locale.getDefault())
        if ((file.endsWith(MainConstant.FILE_TYPE_DOC) || file.endsWith(MainConstant.FILE_TYPE_DOCX) || file.endsWith(MainConstant.FILE_TYPE_TXT) || file.endsWith(MainConstant.FILE_TYPE_DOT) || file.endsWith(MainConstant.FILE_TYPE_DOTX) || file.endsWith(MainConstant.FILE_TYPE_DOTM))) {
            applicationType = MainConstant.APPLICATION_TYPE_WP.toInt()
            toolsbar = WPToolsbar(applicationContext, control)
        } else if ((file.endsWith(MainConstant.FILE_TYPE_XLS) || file.endsWith(MainConstant.FILE_TYPE_XLSX) || file.endsWith(MainConstant.FILE_TYPE_XLT) || file.endsWith(MainConstant.FILE_TYPE_XLTX) || file.endsWith(MainConstant.FILE_TYPE_XLTM) || file.endsWith(MainConstant.FILE_TYPE_XLSM))) {
            applicationType = MainConstant.APPLICATION_TYPE_SS.toInt()
            toolsbar = SSToolsbar(applicationContext, control)
        } else if ((file.endsWith(MainConstant.FILE_TYPE_PPT) || file.endsWith(MainConstant.FILE_TYPE_PPTX) || file.endsWith(MainConstant.FILE_TYPE_POT) || file.endsWith(MainConstant.FILE_TYPE_PPTM) || file.endsWith(MainConstant.FILE_TYPE_POTX) || file.endsWith(MainConstant.FILE_TYPE_POTM))) {
            applicationType = MainConstant.APPLICATION_TYPE_PPT.toInt()
            toolsbar = PGToolsbar(applicationContext, control)
        } else if (file.endsWith(MainConstant.FILE_TYPE_PDF)) {
            applicationType = MainConstant.APPLICATION_TYPE_PDF.toInt()
            toolsbar = PDFToolsbar(applicationContext, control)
        } else {
            applicationType = MainConstant.APPLICATION_TYPE_WP.toInt()
            toolsbar = WPToolsbar(applicationContext, control)
        }
        // 添加tool bar
        //appFrame.addView(toolsbar);
    }

    /**
     *
     */
    private val isSearchbarActive: Boolean
        get() {
            if (appFrame == null || isDispose) {
                return false
            }
            val count = appFrame!!.childCount
            for (i in 0 until count) {
                val v = appFrame!!.getChildAt(i)
                if (v is FindToolBar) {
                    return v.getVisibility() == View.VISIBLE
                }
            }
            return false
        }

    /**
     * show toolbar or search bar
     */
    private fun showSearchBar(show: Boolean) {
        //show search bar
        if (show) {
            if (searchBar == null) {
                searchBar = FindToolBar(this, control)
                appFrame!!.addView(searchBar, 0)
            }
            searchBar!!.visibility = View.VISIBLE
            toolsbar!!.visibility = View.GONE
        } else {
            if (searchBar != null) {
                searchBar!!.visibility = View.GONE
            }
            toolsbar!!.visibility = View.VISIBLE
        }
    }

    /**
     * show toolbar or search bar
     */
    private fun showCalloutToolsBar(show: Boolean) {
        //show callout bar
        if (show) {
            if (calloutBar == null) {
                calloutBar = CalloutToolsbar(applicationContext, control)
                appFrame!!.addView(calloutBar, 0)
            }
            calloutBar!!.setCheckState(EventConstant.APP_PEN_ID, AImageCheckButton.CHECK)
            calloutBar!!.setCheckState(EventConstant.APP_ERASER_ID, AImageCheckButton.UNCHECK)
            calloutBar!!.visibility = View.VISIBLE
            toolsbar!!.visibility = View.GONE
        } else {
            if (calloutBar != null) {
                calloutBar!!.visibility = View.GONE
            }
            toolsbar!!.visibility = View.VISIBLE
        }
    }

    private fun setPenUnChecked() {
        if (fullscreen) {
            penButton!!.state = AImageCheckButton.UNCHECK
            penButton!!.postInvalidate()
        } else {
            calloutBar!!.setCheckState(EventConstant.APP_PEN_ID, AImageCheckButton.UNCHECK)
            calloutBar!!.postInvalidate()
        }
    }

    private fun setEraserUnChecked() {
        if (fullscreen) {
            eraserButton!!.state = AImageCheckButton.UNCHECK
            eraserButton!!.postInvalidate()
        } else {
            calloutBar!!.setCheckState(EventConstant.APP_ERASER_ID, AImageCheckButton.UNCHECK)
            calloutBar!!.postInvalidate()
        }
    }

    /**
     * set the find back button and find forward button state
     */
    override fun setFindBackForwardState(state: Boolean) {
        if (isSearchbarActive) {
            searchBar!!.setEnabled(EventConstant.APP_FIND_BACKWARD, state)
            searchBar!!.setEnabled(EventConstant.APP_FIND_FORWARD, state)
        }
    }

    /**
     * 发送邮件
     */
    private fun fileShare() {
        if (filePath == null) return
        val list = ArrayList<Uri>()
        val file = File(filePath)
        list.add(Uri.fromFile(file))
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(Intent.EXTRA_STREAM, list)
        intent.type = "application/octet-stream"
        startActivity(Intent.createChooser(intent, resources.getText(ROffice.string.sys_share_title)))
    }

    /**
     *
     */
    private fun initMarked() {
        if (dbService != null) {
            marked = dbService!!.queryItem(MainConstant.TABLE_STAR, filePath)
            if (marked) {
                toolsbar!!.setCheckState(EventConstant.FILE_MARK_STAR_ID, AImageCheckButton.CHECK)
            } else {
                toolsbar!!.setCheckState(EventConstant.FILE_MARK_STAR_ID, AImageCheckButton.UNCHECK)
            }
        }
    }

    /**
     *
     */
    private fun markFile() {
        marked = !marked
    }

    /**
     *
     */
    public override fun onCreateDialog(id: Int): Dialog {
        return control!!.getDialog(this, id)
    }

    /**
     * 更新工具条的状态
     */
    override fun updateToolsbarStatus() {
        if (appFrame == null || isDispose) {
            return
        }
        val count = appFrame!!.childCount
        for (i in 0 until count) {
            val v = appFrame!!.getChildAt(i)
            if (v is AToolsbar) {
                v.updateStatus()
            }
        }
    }

    /**
     *
     */
    override fun getActivity(): Activity {
        return this
    }

    /**
     * do action，this is method don't call `control.actionEvent` method, Easily lead to infinite loop
     *
     * @param actionID action ID
     * @param obj      acValue
     * @return True if the listener has consumed the event, false otherwise.
     */
    override fun doActionEvent(actionID: Int, obj: Any?): Boolean {
        try {
            when (actionID) {
                EventConstant.SYS_RESET_TITLE_ID -> title = obj as String?
                EventConstant.SYS_ONBACK_ID -> onBackPressed()
                EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS -> updateToolsbarStatus()
                EventConstant.SYS_HELP_ID -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(ROffice.string.sys_url_wxiwei)))
                    startActivity(intent)
                }

                EventConstant.APP_FIND_ID -> showSearchBar(true)
                EventConstant.APP_SHARE_ID -> fileShare()
                EventConstant.FILE_MARK_STAR_ID -> markFile()
                EventConstant.APP_FINDING -> {
                    val content = (obj as String?)?.trim { it <= ' ' }
                    if (!content.isNullOrEmpty() && control!!.find.find(content)) {
                        setFindBackForwardState(true)
                    } else {
                        setFindBackForwardState(false)
                        shortToast(getLocalString("DIALOG_FIND_NOT_FOUND"))
                    }
                }

                EventConstant.APP_FIND_BACKWARD -> if (!control!!.find.findBackward()) {
                    searchBar!!.setEnabled(EventConstant.APP_FIND_BACKWARD, false)
                    shortToast(getLocalString("DIALOG_FIND_TO_BEGIN"))
                } else {
                    searchBar!!.setEnabled(EventConstant.APP_FIND_FORWARD, true)
                }

                EventConstant.APP_FIND_FORWARD -> if (!control!!.find.findForward()) {
                    searchBar!!.setEnabled(EventConstant.APP_FIND_FORWARD, false)
                    shortToast(getLocalString("DIALOG_FIND_TO_END"))
                } else {
                    searchBar!!.setEnabled(EventConstant.APP_FIND_BACKWARD, true)
                }

                EventConstant.SS_CHANGE_SHEET -> bottomBar!!.setFocusSheetButton((obj as Int? ?: 0))
                EventConstant.APP_DRAW_ID -> {
                    showCalloutToolsBar(true)
                    control!!.getSysKit().calloutManager.drawingMode = MainConstant.DRAWMODE_CALLOUTDRAW
                    appFrame!!.post {
                        // TODO Auto-generated method stub
                        control!!.actionEvent(EventConstant.APP_INIT_CALLOUTVIEW_ID, null)
                    }
                }

                EventConstant.APP_BACK_ID -> {
                    showCalloutToolsBar(false)
                    control!!.getSysKit().calloutManager.drawingMode = MainConstant.DRAWMODE_NORMAL
                }

                EventConstant.APP_PEN_ID -> if (obj as Boolean? == true) {
                    control!!.getSysKit().calloutManager.drawingMode = MainConstant.DRAWMODE_CALLOUTDRAW
                    setEraserUnChecked()
                    appFrame!!.post {
                        // TODO Auto-generated method stub
                        control!!.actionEvent(EventConstant.APP_INIT_CALLOUTVIEW_ID, null)
                    }
                } else {
                    control!!.getSysKit().calloutManager.drawingMode = MainConstant.DRAWMODE_NORMAL
                }

                EventConstant.APP_ERASER_ID -> if (obj as Boolean? == true) {
                    control!!.getSysKit().calloutManager.drawingMode = MainConstant.DRAWMODE_CALLOUTERASE
                    setPenUnChecked()
                } else {
                    control!!.getSysKit().calloutManager.drawingMode = MainConstant.DRAWMODE_NORMAL
                }

                EventConstant.APP_COLOR_ID -> {
                    val dlg = ColorPickerDialog(this, control)
                    dlg.show()
                    dlg.setOnDismissListener { setButtonEnabled(true) }
                    setButtonEnabled(false)
                }

                else -> return false
            }
        } catch (e: Exception) {
            control!!.getSysKit().errorKit.writerLog(e)
        }
        return true
    }

    /**
     *
     */
    override fun openFileFinish() {
        // 加一条与应用视图分隔的灰色线
        gapView = View(applicationContext)
        gapView!!.setBackgroundColor(Color.GRAY)
        appFrame!!.addView(gapView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1))
        //
        val app = control!!.view
        appFrame!!.addView(
            app, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        )
        //
        /*if (applicationType == MainConstant.APPLICATION_TYPE_SS)
        {
            bottomBar = new SheetBar(getApplicationContext(), control, getResources().getDisplayMetrics().widthPixels);
            appFrame.addView(bottomBar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }*/
    }

    /**
     *
     */
    override fun getBottomBarHeight(): Int {
        return if (bottomBar != null) {
            bottomBar!!.sheetbarHeight
        } else 0
    }

    /**
     *
     */
    override fun getTopBarHeight(): Int {
        return 0
    }

    /**
     * event method, office engine dispatch
     *
     * @param v      event source
     * @param e1     MotionEvent instance
     * @param e2     MotionEvent instance
     * @param xValue eventNethodType is ON_SCROLL, this is value distanceX
     * eventNethodType is ON_FLING, this is value velocityY
     * eventNethodType is other type, this is value -1
     * @param yValue eventNethodType is ON_SCROLL, this is value distanceY
     * eventNethodType is ON_FLING, this is value velocityY
     * eventNethodType is other type, this is value -1
     * @see IMainFrame.ON_CLICK
     *
     * @see IMainFrame.ON_DOUBLE_TAP
     *
     * @see IMainFrame.ON_DOUBLE_TAP_EVENT
     *
     * @see IMainFrame.ON_DOWN
     *
     * @see IMainFrame.ON_FLING
     *
     * @see IMainFrame.ON_LONG_PRESS
     *
     * @see IMainFrame.ON_SCROLL
     *
     * @see IMainFrame.ON_SHOW_PRESS
     *
     * @see IMainFrame.ON_SINGLE_TAP_CONFIRMED
     *
     * @see IMainFrame.ON_SINGLE_TAP_UP
     *
     * @see IMainFrame.ON_TOUCH
     */
    override fun onEventMethod(
        v: View?, e1: MotionEvent?, e2: MotionEvent?, xValue: Float, yValue: Float, eventMethodType: Byte
    ): Boolean {
        return false
    }

    override fun changePage() {}

    /**
     *
     */
    override fun getAppName(): String {
        return getString(R.string.app_name)
    }

    /**
     * 是否绘制页码
     */
    override fun isDrawPageNumber(): Boolean {
        return true
    }

    /**
     * 是否支持zoom in / zoom out
     */
    override fun isTouchZoom(): Boolean {
        return true
    }

    /**
     * Word application 默认视图(Normal or Page)
     *
     * @return WPViewConstant.PAGE_ROOT or WPViewConstant.NORMAL_ROOT
     */
    override fun getWordDefaultView(): Byte {
        return WPViewConstant.PAGE_ROOT.toByte()
        //return WPViewConstant.NORMAL_ROOT;
    }

    /**
     * normal view, changed after zoom bend, you need to re-layout
     *
     * @return true   re-layout
     * false  don't re-layout
     */
    override fun isZoomAfterLayoutForWord(): Boolean {
        return true
    }

    /**
     * init float button, for slideshow pageup/pagedown
     */
    private fun initFloatButton() {
        //icon width and height
//        BitmapFactory.Options opts = new BitmapFactory.Options();
//        opts.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), R.drawable.file_slideshow_left, opts);
//
//        //load page up button
//        Resources res = getResources();
//        pageUp = new AImageButton(this, control, res.getString(R.string.pg_slideshow_pageup), -1,
//                -1, EventConstant.APP_PAGE_UP_ID);
//        pageUp.setNormalBgResID(R.drawable.file_slideshow_left);
//        pageUp.setPushBgResID(R.drawable.file_slideshow_left_push);
//        pageUp.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));
//
//        //load page down button
//        pageDown = new AImageButton(this, control, res.getString(R.string.pg_slideshow_pagedown),
//                -1, -1, EventConstant.APP_PAGE_DOWN_ID);
//        pageDown.setNormalBgResID(R.drawable.file_slideshow_right);
//        pageDown.setPushBgResID(R.drawable.file_slideshow_right_push);
//        pageDown.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));
//
//        BitmapFactory.decodeResource(getResources(), R.drawable.file_slideshow_pen_normal, opts);
//        // load pen button
//        penButton = new AImageCheckButton(this, control,
//                res.getString(R.string.app_toolsbar_pen_check), res.getString(R.string.app_toolsbar_pen),
//                R.drawable.file_slideshow_pen_check, R.drawable.file_slideshow_pen_normal,
//                R.drawable.file_slideshow_pen_normal, EventConstant.APP_PEN_ID);
//        penButton.setNormalBgResID(R.drawable.file_slideshow_pen_normal);
//        penButton.setPushBgResID(R.drawable.file_slideshow_pen_push);
//        penButton.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));
//
//        // load eraser button
//        eraserButton = new AImageCheckButton(this, control,
//                res.getString(R.string.app_toolsbar_eraser_check), res.getString(R.string.app_toolsbar_eraser),
//                R.drawable.file_slideshow_eraser_check, R.drawable.file_slideshow_eraser_normal,
//                R.drawable.file_slideshow_eraser_normal, EventConstant.APP_ERASER_ID);
//        eraserButton.setNormalBgResID(R.drawable.file_slideshow_eraser_normal);
//        eraserButton.setPushBgResID(R.drawable.file_slideshow_eraser_push);
//        eraserButton.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));
//
//        // load settings button
//        settingsButton = new AImageButton(this, control, res.getString(R.string.app_toolsbar_color),
//                -1, -1, EventConstant.APP_COLOR_ID);
//        settingsButton.setNormalBgResID(R.drawable.file_slideshow_settings_normal);
//        settingsButton.setPushBgResID(R.drawable.file_slideshow_settings_push);
//        settingsButton.setLayoutParams(new LayoutParams(opts.outWidth, opts.outHeight));
//
//        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        wmParams = new WindowManager.LayoutParams();
//
//        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        wmParams.format = PixelFormat.RGBA_8888;
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        wmParams.width = opts.outWidth;
//        wmParams.height = opts.outHeight;
    }

    /**
     * full screen, not show top tool bar
     */
    override fun fullScreen(fullscreen: Boolean) {
        this.fullscreen = fullscreen
        if (fullscreen) {
            if (wm == null || wmParams == null) {
                initFloatButton()
            }
            wmParams!!.gravity = Gravity.END or Gravity.TOP
            wmParams!!.x = MainConstant.GAP
            wm!!.addView(penButton, wmParams)
            wmParams!!.gravity = Gravity.END or Gravity.TOP
            wmParams!!.x = MainConstant.GAP
            wmParams!!.y = wmParams!!.height
            wm!!.addView(eraserButton, wmParams)
            wmParams!!.gravity = Gravity.END or Gravity.TOP
            wmParams!!.x = MainConstant.GAP
            wmParams!!.y = wmParams!!.height * 2
            wm!!.addView(settingsButton, wmParams)
            wmParams!!.gravity = Gravity.START or Gravity.CENTER
            wmParams!!.x = MainConstant.GAP
            wmParams!!.y = 0
            wm!!.addView(pageUp, wmParams)
            wmParams!!.gravity = Gravity.START or Gravity.CENTER
            wm!!.addView(pageDown, wmParams)

            //hide title and tool bar
            (window.findViewById<View>(android.R.id.title).parent as View).visibility = View.GONE
            //hide status bar
            toolsbar!!.visibility = View.GONE
            //
            gapView!!.visibility = View.GONE
            penButton!!.state = AImageCheckButton.UNCHECK
            eraserButton!!.state = AImageCheckButton.UNCHECK
            val params = window.attributes
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        } else {
            wm!!.removeView(pageUp)
            wm!!.removeView(pageDown)
            wm!!.removeView(penButton)
            wm!!.removeView(eraserButton)
            wm!!.removeView(settingsButton)
            //show title and tool bar
            (window.findViewById<View>(android.R.id.title).parent as View).visibility = View.VISIBLE
            toolsbar!!.visibility = View.VISIBLE
            gapView!!.visibility = View.VISIBLE

            //show status bar
            val params = window.attributes
            params.flags = params.flags and (WindowManager.LayoutParams.FLAG_FULLSCREEN.inv())
            window.attributes = params
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    /**
     *
     */
    override fun changeZoom() {}

    /**
     *
     */
    override fun error(errorCode: Int) {}

    /**
     * get Internationalization resource
     *
     * @param resName Internationalization resource name
     */
    override fun getLocalString(resName: String): String {
        return ResKit.instance().getLocalString(resName)
    }

    override fun isShowPasswordDlg(): Boolean {
        return true
    }

    override fun isShowProgressBar(): Boolean {
        return true
    }

    override fun isShowFindDlg(): Boolean {
        return true
    }

    override fun isShowTXTEncodeDlg(): Boolean {
        return true
    }

    /**
     * get txt default encode when not showing txt encode dialog
     *
     * @return null if showing txt encode dialog
     */
    override fun getTXTDefaultEncode(): String {
        return "GBK"
    }

    override fun completeLayout() {
        // TODO Auto-generated method stub
    }

    override fun isChangePage(): Boolean {
        // TODO Auto-generated method stub
        return true
    }

    /**
     *
     */
    override fun setWriteLog(saveLog: Boolean) {
        writeLog = saveLog
    }

    /**
     *
     */
    override fun isWriteLog(): Boolean {
        return writeLog
    }

    /**
     *
     */
    override fun setThumbnail(isThumbnail: Boolean) {
        this.isThumbnail = isThumbnail
    }

    /**
     * get view backgrouond
     */
    override fun getViewBackground(): Any {
        return bg
    }

    /**
     * set flag whether fitzoom can be larger than 100% but smaller than the max zoom
     */
    override fun setIgnoreOriginalSize(ignoreOriginalSize: Boolean) {}

    /**
     * @return true fitzoom may be larger than 100% but smaller than the max zoom
     * false fitzoom can not larger than 100%
     */
    override fun isIgnoreOriginalSize(): Boolean {
        return false
    }

    override fun getPageListViewMovingPosition(): Byte {
        return IPageListViewListener.Moving_Horizontal
    }

    /**
     *
     */
    override fun isThumbnail(): Boolean {
        return isThumbnail
    }

    /**
     *
     */
    override fun updateViewImages(viewList: List<Int>?) {}

    /**
     *
     */
    override fun getTemporaryDirectory(): File? {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        return getExternalFilesDir(null) ?: filesDir
    }

    /**
     * 释放内存
     */
    override fun dispose() {
        isDispose = true

        control?.dispose()
        control = null

        toolsbar = null
        searchBar = null
        bottomBar = null

        dbService?.dispose()
        dbService = null

        if (appFrame != null) {
            val count = appFrame!!.childCount
            for (i in 0 until count) {
                val v = appFrame!!.getChildAt(i)
                if (v is AToolsbar) {
                    v.dispose()
                }
            }
            appFrame = null
        }
        if (wm != null) {
            wm = null
            wmParams = null
            pageUp!!.dispose()
            pageDown!!.dispose()
            penButton!!.dispose()
            eraserButton!!.dispose()
            settingsButton!!.dispose()
            pageUp = null
            pageDown = null
            penButton = null
            eraserButton = null
            settingsButton = null
        }
    }

    override fun getMainFrame(): FrameLayout {
        return binding.viewerOffice
    }

    //
    private var isDispose = false

    // 当前标星状态
    private var marked = false

    /**
     *
     */
    //
    private var applicationType = -1

    /**
     * @return Returns the filePath.
     */
    //
    private var filePath: String? = null

    // application activity control
    private var control: MainControl? = null

    //
    private var appFrame: AppFrame? = null

    //tool bar
    private var toolsbar: AToolsbar? = null

    //search bar
    private var searchBar: FindToolBar? = null

    //
    private var dbService: DBService? = null

    //
    private var bottomBar: SheetBar? = null

    //
    private var gapView: View? = null

    //float button: PageUp/PageDown
    private var wm: WindowManager? = null
    private var wmParams: WindowManager.LayoutParams? = null
    private var pageUp: AImageButton? = null
    private var pageDown: AImageButton? = null
    private var penButton: AImageCheckButton? = null
    private var eraserButton: AImageCheckButton? = null
    private var settingsButton: AImageButton? = null

    //whether write log to temporary file
    private var writeLog = true

    //open file to get thumbnail, or not
    private var isThumbnail = false

    //view background
    private val bg: Any = Color.GRAY

    //
    private var calloutBar: CalloutToolsbar? = null

    //
    private var fullscreen = false

    //
    private var fullscreenByMenu = false

    companion object {
        const val EXTRA_FILE_PATH = "EXTRA_FILE_PATH"
        const val EXTRA_BACK_TO_MAIN_APP = "EXTRA_BACK_TO_MAIN_APP"

        fun openFile(context: Context, baseFile: BaseFile) {
            context.startActivity(Intent(context, OfficeReaderActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, baseFile.data!!)
                putExtra("type", FileTypeExtension.getTypeFile(baseFile.data!!))
            })
        }
    }
}