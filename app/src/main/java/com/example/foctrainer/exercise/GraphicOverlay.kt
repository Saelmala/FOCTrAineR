package com.example.foctrainer.exercise

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import androidx.core.util.Preconditions


class GraphicOverlay : View {
    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()

    // Matrix for transforming from image coordinates to overlay view coordinates.
    private val transformationMatrix: Matrix = Matrix()

    private var imageWidth = 0
    private var imageHeight = 0

    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    private var scaleFactor = 1.0f

    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private var postScaleWidthOffset = 0f

    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private var postScaleHeightOffset = 0f
    private var isImageFlipped = false
    private var needUpdateTransformation = true

    /**
     * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
     * this and implement the [Graphic.draw] method to define the graphics element. Add
     * instances to the overlay using [GraphicOverlay.add].
     */
    abstract class Graphic(private val overlay: GraphicOverlay?) {
        /**
         * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
         * to view coordinates for the graphics that are drawn:
         *
         *
         *  1. [Graphic.scale] adjusts the size of the supplied value from the image
         * scale to the view scale.
         *  1. [Graphic.translateX] and [Graphic.translateY] adjust the
         * coordinate from the image's coordinate system to the view coordinate system.
         *
         *
         * @param canvas drawing canvas
         */
        abstract fun draw(canvas: Canvas?)

        /** Adjusts the supplied value from the image scale to the view scale.  */
        fun scale(imagePixel: Float): Float {
            return imagePixel * overlay!!.scaleFactor
        }

        /** Returns the application context of the app.  */
        val applicationContext: Context
            get() = overlay!!.getContext().getApplicationContext()


        fun isImageFlipped(): Boolean {
            return overlay!!.isImageFlipped
        }

        /**
         * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
         */
        fun translateX(x: Float): Float {
            if (overlay!!.isImageFlipped) {
                return overlay.getWidth() - (scale(x) - overlay.postScaleWidthOffset);
            } else {
                return scale(x) - overlay.postScaleWidthOffset;
            }
        }

        /**
         * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
         */
        fun translateY(y: Float): Float {
            return scale(y) - overlay!!.postScaleHeightOffset
        }

        /**
         * Returns a [Matrix] for transforming from image coordinates to overlay view coordinates.
         */
        fun getTransformationMatrix(): Matrix {
            return overlay!!.transformationMatrix
        }

        fun postInvalidate() {
            overlay!!.postInvalidate()
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context,attrs) {
        addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            needUpdateTransformation = true
        }
    }

    /** Removes all graphics from the overlay.  */
    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    /** Adds a graphic to the overlay.  */
    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    /** Removes a graphic from the overlay.  */
    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    /**
     * Sets the source information of the image being processed by detectors, including size and
     * whether it is flipped, which informs how to transform image coordinates later.
     *
     * @param imageWidth the width of the image sent to ML Kit detectors
     * @param imageHeight the height of the image sent to ML Kit detectors
     * @param isFlipped whether the image is flipped. Should set it to true when the image is from the
     * front camera.
     */
    @SuppressLint("RestrictedApi")
    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        Preconditions.checkState(imageWidth > 0, "image width must be positive")
        Preconditions.checkState(imageHeight > 0, "image height must be positive")
        synchronized(lock) {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            isImageFlipped = isFlipped
            needUpdateTransformation = true
        }
        postInvalidate()
    }

    fun getImageWidth(): Int {
        return imageWidth
    }

    fun getImageHeight(): Int {
        return imageHeight
    }

    private fun updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
            return
        }
        val viewAspectRatio = getWidth().toFloat() / getHeight()
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        postScaleWidthOffset = 0f
        postScaleHeightOffset = 0f
        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = getWidth().toFloat() / imageWidth
            postScaleHeightOffset = (getWidth().toFloat() / imageAspectRatio - getHeight()) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = getHeight().toFloat() / imageHeight
            postScaleWidthOffset = (getHeight().toFloat() * imageAspectRatio - getWidth()) / 2
        }
        transformationMatrix.reset()
        transformationMatrix.setScale(scaleFactor, scaleFactor)
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, getWidth() / 2f, getHeight() / 2f)
        }
        needUpdateTransformation = false
    }

    /** Draws the overlay with its associated graphic objects.  */
    protected override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        synchronized(lock) {
            updateTransformationIfNeeded()
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}