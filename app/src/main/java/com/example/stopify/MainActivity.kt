package com.example.stopify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stopify.SongHandling.Song
import com.example.stopify.SongHandling.SongsAdapter
import kotlinx.android.synthetic.main.activity_choose_song.*

class MainActivity : AppCompatActivity() {
    private var songsAdapter: SongsAdapter? = null
    var songs = ArrayList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        setContentView(R.layout.activity_choose_song)
        loadSongs()
        setUpLayoutManager()
    }

    private fun setUpLayoutManager() {
        songsAdapter = SongsAdapter(songs, applicationContext)

        songsRV.layoutManager = LinearLayoutManager(this)
        songsRV.adapter = songsAdapter
    }

    private fun loadSongs() {
        val contentResolver = contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.ARTIST + " ASC," +
                                MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        if (cursor != null && cursor.moveToNext()) {
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)).toInt()
                songs.add(Song(data, title, album, artist, duration))
            }
        }
        cursor!!.close()
    }
}