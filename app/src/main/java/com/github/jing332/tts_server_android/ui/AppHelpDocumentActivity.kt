package com.github.jing332.tts_server_android.ui

import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.FileUtils.readAllText
import com.github.jing332.tts_server_android.utils.dp
import com.github.jing332.tts_server_android.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.DefaultMediaDecoder
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.network.OkHttpNetworkSchemeHandler
import io.noties.markwon.image.svg.SvgMediaDecoder
import io.noties.markwon.linkify.LinkifyPlugin

@OptIn(ExperimentalMaterial3Api::class)
class AppHelpDocumentActivity : AppCompatActivity() {
    companion object {
        const val TAG = "AppHelpDocumentActivity"
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
//            .usePlugin(HtmlPlugin.create())
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scrollView = NestedScrollView(this)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.setPadding(8.dp)

        val text = TextView(this)
        text.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        text.setTextIsSelectable(true)
        text.movementMethod = LinkMovementMethod.getInstance()
        scrollView.addView(text)

        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.app_help_document)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                                }
                            }
                        )
                    }
                ) {
                    AndroidView(modifier = Modifier.padding(it),
                        factory = {
                            scrollView
                        }
                    )
                }
            }
        }

        markwon.setMarkdown(text, assets.open("help/app.md").readAllText())
    }

}
