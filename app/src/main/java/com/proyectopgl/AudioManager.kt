package com.proyectopgl

import android.content.Context
import android.media.MediaPlayer

class AudioManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun play(audioResId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, audioResId)
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
