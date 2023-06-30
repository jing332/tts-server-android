package com.github.jing332.tts_server_android.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.ViewCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.ExoPlayerActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog
import com.github.jing332.tts_server_android.ui.view.ThemeExtensions.initAppTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class ExoPlayerActivity : BackActivity(), Player.Listener {
    companion object {
        const val PARAM_URI = "PARAM_URI"
    }

    private val binding by lazy { ExoPlayerActivityBinding.inflate(layoutInflater) }

    private val exoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            addListener(this@ExoPlayerActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.playerView.player = exoPlayer
        intent.getStringExtra(PARAM_URI)?.let { uri ->
            binding.tvUri.text = uri
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