package com.example.stopify.SongHandling

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stopify.PlayMusicActivity
import com.example.stopify.R

class SongsAdapter(private val songs:ArrayList<Song>, val context: Context) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.song_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.titleTV.text = song.title
        holder.albumTV.text = song.album
        holder.artistTV.text = song.artist
        val metaRetriever = MediaMetadataRetriever()
        try{
            metaRetriever.setDataSource(song.data)
            val albumArt = metaRetriever.embeddedPicture
            val albumBmp = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size)
            holder.albumIV.setImageBitmap(albumBmp)
        }
        catch (e: Exception){
            holder.albumIV.setImageResource(R.mipmap.default_img)
        }
        holder.songClickListener = object : SongEntryClickListener {
            override fun onSongEntryClick(view: View, pos: Int) {
                val storage = MusicStorage(context)
                storage.storeSongs(songs)
                storage.storeSongIndex(pos)
                val playMusicIntent = Intent(context, PlayMusicActivity::class.java)
                holder.itemView.context.startActivity(playMusicIntent)
            }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var titleTV:TextView = itemView.findViewById(R.id.song_title)
        var artistTV:TextView = itemView.findViewById(R.id.artist)
        var albumTV:TextView = itemView.findViewById(R.id.album_title)
        var albumIV:ImageView = itemView.findViewById(R.id.song_image)
        var songClickListener: SongEntryClickListener? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v != null) {
                this.songClickListener!!.onSongEntryClick(v, adapterPosition)
            }
        }
    }
}