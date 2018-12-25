package com.anwesh.uiprojects.linkedsemicirclelinesview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.semicirclelinesview.SemiCircleLinesView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : SemiCircleLinesView = SemiCircleLinesView.create(this)
    }
}
