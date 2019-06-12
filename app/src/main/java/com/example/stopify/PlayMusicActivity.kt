
/**
* Player Icons made by Chanut from www.flaticon.com
 * */
package com.example.stopify

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaMetadataRetriever
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.IBinder
import android.view.Menu
import android.view.View
import com.example.stopify.SongHandling.MusicStorage
import com.example.stopify.SongHandling.Song
import com.example.stopify.Utils.Companion.createTimeLabel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_play_music.*
import kotlin.random.Random.Default.nextInt


@Suppress("UNCHECKED_CAST")
class PlayMusicActivity : AppCompatActivity() {

    private var totalTime: Int = 0
    private lateinit var musicPlayerService : MediaPlayerService
    private var mpServiceBound = false
    private var currentSongIndex = 0
    private var shuffle = false
    private var replay = false
    private var songs = ArrayList<Song>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MediaPlayerService.LocalBinder
            musicPlayerService = binder.getService()
            mpServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mpServiceBound = false
        }
    }

    companion object {
        const val PLAY_NEW_SONG_BROADCAST = "play new song"
        const val DEFAULT_IMAGE_PATH = "https://cdn.pixabay.com/photo/2017/04/19/10/24/vinyl-2241789_960_720.png"
        const val THREAD_SLEEP_TIME:Long = 300
    }

    private var handler =
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                val currentTimePos = msg.what
                timeBar.progress = currentTimePos

                val elapsedTime = createTimeLabel(currentTimePos)
                elapsedTimeLabel.text = elapsedTime

                val remainingTime = createTimeLabel(totalTime - currentTimePos)
                remainingTimeLabel.text = "-$remainingTime"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_play_music)
        musicPlayerService = MediaPlayerService()
        songs = MusicStorage(applicationContext).loadSongs()
        currentSongIndex = MusicStorage(applicationContext).loadSongIndex()
        totalTime = songs[currentSongIndex].duration

        setUpNewSong()
        playAudio()
        setOnClickListeners()
        timeBar.max = totalTime
        prepareSeekBar()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        startThread()
    }

    override fun onResume() {
        super.onResume()
        if (musicPlayerService.interruptionOccurred) {
            playPauseButton.setBackgroundResource(R.mipmap.play_btn)
            musicPlayerService.interruptionOccurred = false
        }
    }

    private fun setOnClickListeners() {
        playPauseButton.setOnClickListener {
            if(musicPlayerService.isPlaying()) {
                musicPlayerService.pauseMusic()
                playPauseButton.setBackgroundResource(R.mipmap.play_btn)
            }
            else{
                musicPlayerService.playMusic()
                playPauseButton.setBackgroundResource(R.mipmap.pause_btn)
            }
        }

        next_btn.setOnClickListener{
            prepareForNewSong(true)
        }

        previous_btn.setOnClickListener{
            prepareForNewSong(false)
        }

        shuffle_btn.setOnClickListener{
            if(shuffle)
                shuffle_dot.visibility = View.INVISIBLE
            else
                shuffle_dot.visibility = View.VISIBLE
            shuffle = !shuffle
        }

        replay_btn.setOnClickListener{
            if(replay) {
                musicPlayerService.musicPlayer!!.isLooping = true
                replay_dot.visibility = View.INVISIBLE
            }
            else
                replay_dot.visibility = View.VISIBLE
            replay = !replay
        }
    }

    private fun startThread() {
        Thread(Runnable {
            Thread.sleep(THREAD_SLEEP_TIME)
            musicPlayerService.musicPlayer!!.setOnCompletionListener {
                prepareForNewSong(true)
            }
            while(musicPlayerService.isPlayerNotNull() && mpServiceBound){
                try{
                    val msg = Message()
                    msg.what = musicPlayerService.currentPosition()
                    handler.sendMessage(msg)
                    Thread.sleep(THREAD_SLEEP_TIME)
                }
                catch(e: InterruptedException){
                }
            }
        }).start()
    }

    private fun prepareForNewSong(isNext:Boolean) {
        var nextIndex = currentSongIndex
        if(!replay)
            when {
                shuffle -> while (nextIndex == currentSongIndex)
                    nextIndex = nextInt(songs.size)
                isNext -> nextIndex = if (nextIndex + 1 >= songs.size) 0 else nextIndex + 1
                else -> nextIndex = if (nextIndex - 1 < 0) songs.size - 1 else nextIndex - 1
            }

        currentSongIndex = nextIndex
        MusicStorage(applicationContext).storeSongIndex(currentSongIndex)
        setUpNewSong()
        if(!musicPlayerService.isPlaying())
            playPauseButton.setBackgroundResource(R.mipmap.pause_btn)
        playAudio()
    }

    private fun playAudio() {
        if (mpServiceBound) {
            val broadcastIntent = Intent(PLAY_NEW_SONG_BROADCAST)
            sendBroadcast(broadcastIntent)
        }
        else {
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            startService(playerIntent)
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setUpNewSong() {
        val metaRetriever = MediaMetadataRetriever()
        val currSong = songs[currentSongIndex]
        totalTime = currSong.duration
        timeBar.max = totalTime
        current_song_title.text = currSong.title
        current_album.text  =  currSong.album
        current_artist.text = currSong.artist
        try{
            metaRetriever.setDataSource(currSong.data)
            val albumArt = metaRetriever.embeddedPicture
            val albumBmp = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size)
            current_album_art.setImageBitmap(albumBmp)
        }
        catch (e: Exception){
            Picasso.get().load(DEFAULT_IMAGE_PATH).into(current_album_art)
        }
    }

    private fun prepareSeekBar() {
        timeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        musicPlayerService.seekTo(progress)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mpServiceBound) {
            unbindService(serviceConnection)
            musicPlayerService.stopSelf()
            mpServiceBound = false
        }
    }

}
