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
val DELAY : Long = 20

fun Int.inverse() : Float = 1f / this

fun Float.maxOfScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxOfScale(i, n)) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Int.modulo2() : Int = (this % 2)

fun Int.modulo2Mirror2() : Float = 1f - 2 * modulo2()

fun Int.modulo2Mirror1(k :  Int, n : Int) : Int = (1 - modulo2()) * k + modulo2() * (n - k)

fun Canvas.drawSCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val size : Float = gap / sizeFactor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = color
    paint.style = Paint.Style.STROKE
    save()
    translate(w/2, gap * (i + 1))
    rotate(-90f * (1 - sc1 * i.modulo2Mirror2()))
    drawArc(RectF(-size, -size, size, size), 90f, 180f, true, paint)
    val yGap : Float = (2 * size - size / 5) / (lines + 1)
    for (j in 0..(lines - 1)) {
        val sc : Float = sc2.divideScale(i.modulo2Mirror1(j, lines - 1), lines)
        save()
        translate(size/10 + w/2 * sc, -size + size/10 + yGap + yGap * j)
        drawLine(0f, 0f, (size - size/10), 0f, paint)
        restore()
    }
    restore()
}

class SemiCircleLinesView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, 1, lines)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if(animated) {
                animated = false
            }
        }
    }

    data class SCLNode(var i : Int, val state : State = State()) {

        private var next : SCLNode? = null
        private var prev : SCLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SCLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SCLNode {
            var curr : SCLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SemiCircleLines(var i : Int) {

        private val root : SCLNode = SCLNode(0)
        private var curr : SCLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SemiCircleLinesView) {
        private var scl : SemiCircleLines = SemiCircleLines(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            scl.draw(canvas, paint)
            animator.animate {
                scl.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            scl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : SemiCircleLinesView {
            val view : SemiCircleLinesView = SemiCircleLinesView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
