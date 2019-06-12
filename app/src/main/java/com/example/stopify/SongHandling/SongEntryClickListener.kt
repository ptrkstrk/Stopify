package com.example.stopify.SongHandling

import android.view.View

interface SongEntryClickListener {
    fun onSongEntryClick(view: View, pos: Int){}
}