package com.example.stopify

class Utils {
    companion object {
        fun createTimeLabel(time: Int): String {
            val min = time / 1000 / 60
            val sec = time / 1000 % 60

            var timeLabel = "$min:"
            if(sec < 10)
                timeLabel +="0"
            timeLabel += sec

            return timeLabel
        }
    }
}