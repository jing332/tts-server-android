package com.github.jing332.tts_server_android.service.systts.help

import android.content.Context
import android.util.Log
import com.github.jing332.tts_server_android.help.config.SysTtsConfig
import com.github.jing332.tts_server_android.util.FileUtils
import com.github.jing332.tts_server_android.util.runOnUI
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import java.io.File


class BgmPlayer(val context: Context) {
    companion object {
        const val TAG = "BgmPlayer"
    }

    private val exoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    val volume = mediaItem?.localConfiguration?.tag
                    if (volume != null && volume is Float && volume != this@apply.volume)
                        this@apply.volume = volume
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    removeMediaItem(currentMediaItemIndex)
                    seekToNextMediaItem()
                    prepare()
                }
            })
            repeatMode = Player.REPEAT_MODE_ALL
            shuffleModeEnabled = SysTtsConfig.isBgmShuffleEnabled
        }
    }
    private val currentPlaySet = mutableSetOf<Pair<Float, String>>()

    fun release() {
        exoPlayer.release()
    }

    fun stop() {
        Log.d(TAG, "stop()...")
        runOnUI { exoPlayer.pause() }
    }

    fun play() {
        Log.d(TAG, "play()...")
        runOnUI {
            if (!exoPlayer.isPlaying) exoPlayer.play()
        }
    }

    fun setPlayList(shuffleMode: Boolean, list: Set<Pair<Float, String>>) {
        if (list == currentPlaySet) return
        currentPlaySet.clear()
        currentPlaySet.addAll(list)
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        for (path in list) {
            val file = File(path.second)
            if (file.isDirectory) {
                val allFiles = FileUtils.getAllFilesInFolder(file)
                    .run { if (shuffleMode) this.shuffled() else this }
                for (subFile in allFiles) {
                    val mime = FileUtils.getMimeType(subFile)
                    // 非audio或未知则跳过
                    if (mime == null || !mime.startsWith("audio")) continue

                    Log.d(TAG, subFile.absolutePath)
                    val item =
                        MediaItem.Builder().setTag(path.first).setUri(subFile.absolutePath).build()
                    exoPlayer.addMediaItem(item)
                }
            }
        }
        exoPlayer.prepare()
    }

}