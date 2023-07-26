package com.github.jing332.tts_server_android.ui

import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.AppHelpDocumentActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.utils.addOnBackPressed
import com.github.jing332.tts_server_android.utils.dp
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.DefaultMediaDecoder
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
import io.noties.markwon.image.svg.SvgMediaDecoder
import io.noties.markwon.linkify.LinkifyPlugin

class AppHelpDocumentActivity : BackActivity() {
    companion object {
        const val TAG = "AppHelpDocumentActivity"
    }

    private val binding by lazy {
        AppHelpDocumentActivityBinding.inflate(layoutInflater)
    }

    private val markwon by lazy {
        Markwon.builder(this)
            .usePlugin(ImagesPlugin.create {
                it.addSchemeHandler(OkHttpNetworkSchemeHandler.create())
                it.addMediaDecoder(DefaultMediaDecoder.create())
                it.addMediaDecoder(SvgMediaDecoder.create())
                it.errorHandler { url, throwable ->
                    throwable.printStackTrace()
                    println(url)
                    null
                }
            })
            .usePlugin(HtmlPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { view, link ->
                        Log.d(TAG, "configureConfiguration: $link")
                    }

                    // or subclass default instance
                    builder.linkResolver(object : LinkResolverDef() {
                        override fun resolve(view: View, link: String) {
                            Log.d(TAG, "resolve: $link")
                            MaterialAlertDialogBuilder(this@AppHelpDocumentActivity)
                                .setTitle("是否跳转？")
                                .setMessage("是否跳转到 $link ?")
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    super.resolve(view, link)
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .setNeutralButton(R.string.copy_address) { _, _ ->
                                    ClipboardUtils.copyText(link)
                                    toast(R.string.copied)
                                }
                                .show()
                        }
                    })
                }
            })
            .usePlugin(object : AbstractMarkwonPlugin(), MarkwonPlugin {
                override fun beforeSetText(textView: TextView, markdown: Spanned) {
                    AsyncDrawableScheduler.unschedule(textView)
                }

                override fun afterSetText(textView: TextView) {
                    AsyncDrawableScheduler.schedule(textView);
                }
            })
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    private val accessibilityManager by lazy {
        getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    private var bottom = false
    private var backTimes = 0

    private fun back(): Boolean {
        if (accessibilityManager.isTouchExplorationEnabled) return true
        if (bottom) return true

        MaterialAlertDialogBuilder(this).setTitle("想退出？")
            .setMessage("淦！读完了吗就想退！没门！\n╰（‵□′）╯")
            .apply {
                if (backTimes > 1)
                    setNeutralButton("我错了") { _, _ -> finish() }
            }
            .setPositiveButton("看！") { _, _ ->
                backTimes++
            }
            .show()

        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.text.movementMethod = LinkMovementMethod.getInstance()
        markwon.setMarkdown(binding.text, assets.open("help/app.md").readAllText())


        bottom = false
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (!bottom)
                bottom = isBottom()
        }
        addOnBackPressed {
            return@addOnBackPressed !back()
        }

    }

    private fun isBottom(): Boolean {
        val scrollView = binding.scrollView
        // 获取ScrollView的高度
        val scrollViewHeight = scrollView.height
        // 获取ScrollView的子View
        val lastChild = scrollView.getChildAt(scrollView.childCount - 1)
        // 获取ScrollView的子View的Y轴坐标
        val lastChildBottomY = lastChild.bottom
        // 获取ScrollView的滚动位置
        val scrollY = scrollView.scrollY
        // 判断滚动位置是否到达底部
        return scrollY + scrollViewHeight + 48.dp >= lastChildBottomY
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            if (back()) finish()
            true
        } else super.onOptionsItemSelected(item)
    }
}
