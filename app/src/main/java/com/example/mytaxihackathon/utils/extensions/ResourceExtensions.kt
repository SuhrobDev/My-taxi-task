package com.example.mytaxihackathon.utils.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

//fun getStrings(@StringRes int: Int) = resources.getString(int)
//
//fun getString(@StringRes int: Int, str: String) = resources.getString(int, str)

fun getColor(context: Context, color: Int): Int {
    return ContextCompat.getColor(
        context,
        color
    )
}

fun getImage(context: Context, string: Int): Drawable? {
    return ContextCompat.getDrawable(
        context,
        string
    )
}