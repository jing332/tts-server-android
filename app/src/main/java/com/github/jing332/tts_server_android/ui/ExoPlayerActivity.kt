package com.github.jing332.tts_server_android.ui

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.theme.AppTheme
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath

@kotlin.OptIn(ExperimentalMaterial3Api::class)
class ExoPlayerActivity : AppCompatActivity(), Player.Listener {
    private val exoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            addListener(this@ExoPlayerActivity)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var text by mutableStateOf("")
        setContent {
            AppTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.exo_player_title)) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowBack,
                                    stringResource(id = R.string.nav_back)
                                )
                            }
                        })
                }) {
                    Column(Modifier.padding(it)) {
                        SelectionContainer {
                            Text(text = text, style = MaterialTheme.typography.bodyMedium)
                        }
                        AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                val transparentDrawable = ShapeDrawable(OvalShape())
                                transparentDrawable.paint.color =
                                    ContextCompat.getColor(context, android.R.color.transparent)

                                background = transparentDrawable
                                setShutterBackgroundColor(
                                    ContextCompat.getColor(
                                        context,
                                        android.R.color.transparent
                                    )
                                )
                            }
                        })
                    }
                }
            }
        }

        intent.data?.let { uri ->
            text = try {
                getPath(uri, isTree = false) ?: ""
            } catch (e: Exception) {
                uri.toString()
            }
            exoPlayer.addMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        displayErrorDialog(error)
    }
}