package com.github.jing332.tts_server_android.ui

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.github.jing332.tts_server_android.databinding.ExoPlayerActivityBinding
import com.github.jing332.tts_server_android.ui.base.BackActivity
import com.github.jing332.tts_server_android.ui.view.AppDialogs.displayErrorDialog

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