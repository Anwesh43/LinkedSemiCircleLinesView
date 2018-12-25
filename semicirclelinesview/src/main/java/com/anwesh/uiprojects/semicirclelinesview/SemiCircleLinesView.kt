package com.anwesh.uiprojects.semicirclelinesview

/**
 * Created by anweshmishra on 25/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val lines : Int = 4
val color : Int = Color.parseColor("#5E35B1")
val strokeFactor : Int = 90
val sizeFactor : Float = 2.6f
val scDiv : Double = 0.51
val scGap : Float = 0.05f
