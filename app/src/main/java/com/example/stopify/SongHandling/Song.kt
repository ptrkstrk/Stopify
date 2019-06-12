package com.example.stopify.SongHandling
import java.io.Serializable

class Song(val data: String, val title: String?, val album: String?,
           val artist: String?, val duration:Int) : Serializable