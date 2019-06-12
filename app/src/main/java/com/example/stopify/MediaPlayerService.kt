package com.example.stopify

import android.app.Service
import android.content.BroadcastReceiver
import android.media.MediaPlayer
import android.content.Intent
import android.os.IBinder
import android.media.AudioManager
import android.os.Binder
import android.content.Context
import android.content.IntentFilter
import com.example.stopify.SongHandling.MusicStorage
import com.example.stopify.SongHandling.Song


@Suppress("UNCHECKED_CAST")
class MediaPlayerService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    private val binder = LocalBinder()
    var musicPlayer: MediaPlayer? = null
    private var musicManager: AudioManager? = null
    private var resumePosition: Int = 0
    private var songs: ArrayList<Song>? = null
    private var currentSongIndex = -1
    var interruptionOccurred = false

    override fun onCreate() {
        super.onCreate()
        registerPlayBroadcast()
    }

    private fun initMediaPlayer() {
        musicPlayer = MediaPlayer()
        musicPlayer!!.setOnPreparedListener(this)
        musicPlayer!!.setOnBufferingUpdateListener(this)
        musicPlayer!!.setVolume(1.0f, 1.0f)
        musicPlayer!!.reset()
        musicPlayer!!.setDataSource(songs!![currentSongIndex].data)
        musicPlayer!!.prepareAsync()
    }

    private val playNewAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            currentSongIndex = MusicStorage(applicationContext).loadSongIndex()
            stopMusic()
            musicPlayer!!.reset()
            musicPlayer!!.setDataSource(songs!![currentSongIndex].data)
            musicPlayer!!.prepareAsync()
        }
    }

    private fun registerPlayBroadcast() {
        val filter = IntentFilter(PlayMusicActivity.PLAY_NEW_SONG_BROADCAST)
        registerReceiver(playNewAudio, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        currentSongIndex = MusicStorage(applicationContext).loadSongIndex()
        songs = MusicStorage(applicationContext).loadSongs()

        if (!requestAudioFocus()) {
            stopSelf()
        }
        initMediaPlayer()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicPlayer != null) {
            stopMusic()
            musicPlayer!!.release()
        }
        removeAudioFocus()
        unregisterReceiver(playNewAudio)
        MusicStorage(applicationContext).clearSongsData()
    }

    fun playMusic() {
            musicPlayer!!.start()
    }

    private fun stopMusic() {
        if (musicPlayer!!.isPlaying) {
            musicPlayer!!.stop()
        }
    }

    fun pauseMusic() {
        musicPlayer!!.pause()
        resumePosition = musicPlayer!!.currentPosition
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {}

    override fun onPrepared(mp: MediaPlayer) {
        playMusic()
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_LOSS ->
                if (musicPlayer!!.isPlaying) {
                    musicPlayer!!.pause()
                    interruptionOccurred = true
                }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                if (musicPlayer!!.isPlaying) {
                    musicPlayer!!.pause()
                    interruptionOccurred = true
                }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                if (musicPlayer!!.isPlaying)
                    musicPlayer!!.setVolume(0.1f, 0.1f)
        }
    }

    private fun requestAudioFocus(): Boolean {
        musicManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = musicManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == musicManager!!.abandonAudioFocus(this)
    }

    fun isPlaying(): Boolean {
        if(musicPlayer != null)
            return musicPlayer!!.isPlaying
        return false
    }

    fun seekTo(progress: Int) {
        musicPlayer!!.seekTo(progress)
    }

    fun currentPosition(): Int{
        return musicPlayer!!.currentPosition
    }

    fun isPlayerNotNull(): Boolean {
        return musicPlayer != null
    }

    inner class LocalBinder : Binder(), IBinder {
        fun getService(): MediaPlayerService {
            return this@MediaPlayerService
        }
    }
}