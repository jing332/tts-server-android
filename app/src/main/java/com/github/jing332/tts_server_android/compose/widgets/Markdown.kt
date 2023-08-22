package com.github.jing332.tts_server_android.compose.widgets

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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

@Composable
fun Markdown(
    content: String,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    onLinkResolve: (url: String) -> Boolean = { false },
    onTextViewConfiguration: (TextView) -> Unit = {},
) {
    val context = LocalContext.current

    val markwon = remember {
        Markwon.builder(context)
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
//                    builder.linkResolver { view, link ->
//                        Log.d(TAG, "configureConfiguration: $link")
//                    }

                    // or subclass default instance
                    builder.linkResolver(object : LinkResolverDef() {
                        override fun resolve(view: View, link: String) {
                            if (!onLinkResolve(link))
                                super.resolve(view, link)
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

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(it).apply {
                movementMethod = LinkMovementMethod.getInstance()
                onTextViewConfiguration(this)
            }
        }) {
        it.setTextIsSelectable(isSelectable)

        it.setTextColor(textColor.toArgb())
        markwon.setMarkdown(it, content)
    }

}