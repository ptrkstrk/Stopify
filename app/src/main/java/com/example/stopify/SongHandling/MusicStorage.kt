package com.example.stopify.SongHandling

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson


class MusicStorage(private val context: Context) {

    companion object {
        const val SONGS_SP_KEY = "songs"
        const val SONGS_KEY = "songs list"
        const val SONG_INDEX_KEY = "song index"
    }

    fun storeSongs(arrayList: ArrayList<Song>) {
        val preferences = context.getSharedPreferences(SONGS_SP_KEY, Context.MODE_PRIVATE)

        val editor = preferences!!.edit()
        val json = Gson().toJson(arrayList)
        editor.putString(SONGS_KEY, json)
        editor.apply()
    }

    fun loadSongs(): ArrayList<Song> {
        val preferences = context.getSharedPreferences(SONGS_SP_KEY, Context.MODE_PRIVATE)
        val json = preferences!!.getString(SONGS_KEY, null)
        val type = object : TypeToken<ArrayList<Song>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun storeSongIndex(index: Int) {
        val preferences = context.getSharedPreferences(SONGS_SP_KEY, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.putInt(SONG_INDEX_KEY, index)
        editor.apply()
    }

    fun loadSongIndex(): Int {
        val preferences = context.getSharedPreferences(SONGS_SP_KEY, Context.MODE_PRIVATE)
        return preferences!!.getInt(SONG_INDEX_KEY, -1)
    }

    fun clearSongsData() {
        val preferences = context.getSharedPreferences(SONGS_SP_KEY, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.clear()
        editor.apply()
    }
}