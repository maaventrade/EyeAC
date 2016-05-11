package com.alexmochalov.buttons;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.alexmochalov.eyeac.Params;
import com.alexmochalov.animation.SurfaceViewScreen;

public class ButtonVAC {
	private RectF mRect; // Rectangle of the button
	private String mVak; // Text of the button
	private boolean mTopLine = false;

	private static final int DELTATEXT = 5;

	boolean isPressed = false; // Is Button pressed or not

	public ButtonVAC(int index, RectF rect, boolean topLine) {
		mRect = rect;
		mTopLine = topLine;
	}

	public ButtonVAC(int index, RectF rect, String vak, boolean topLine) {
		mRect = rect;
		mVak = vak;
		mTopLine = topLine;
	}

	public void setVAK(String vak) {
		mVak = vak;
	}

	public void setPressed(boolean isPressed) {
		this.isPressed = isPressed;
	}

	public String getVAK() {
		return mVak;
	}

	/**
	 * Check if touch event is in the button rect
	 * 
	 * @param x
	 *            - coordinate of touching
	 * @param y
	 *            - coordinate of touching
	 * @return
	 */
	public boolean contains(float x, float y) {
		return mRect.contains(x, y);
	}

	public void draw(Canvas canvas, Paint paint, SurfaceViewScreen surface,
			int shift) {
		if (mRect == null)
			return;
		if (mVak.equals(""))
			return; // only if title is not empty
		if (mVak.equals("F"))
			return; // Not draw the center button

		if (surface == null)
			return;

		Rect bounds = new Rect();

		if (surface.canPressBytton()) {
			paint.setColor(Params.colorBtnBg);
		} else {
			paint.setColor(Params.colorBtnBgDisable);
		}
		paint.setAlpha(Params.transparency);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(8);

		// draw the button
		canvas.drawRoundRect(mRect, 30, 30, paint);
		paint.setAlpha(255);

		if (isPressed) {
			paint.setColor(Params.colorBtnBorder);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawRoundRect(mRect, 30, 30, paint);
		}

		paint.setStrokeWidth(1);
		paint.setTextSize(Params.fontSize);
		// Get the sise of the buttons text
		paint.getTextBounds(mVak, 0, mVak.length(), bounds);

		// Coordinates of the text in the center
		float textX = Math.max(
				mRect.left + ((int) (mRect.width() - bounds.width()) >> 1),
				mRect.left);
		float textY = mRect.top
				+ ((int) (mRect.height() + bounds.height()) >> 1);

		// if coordinates less when zero, move it
		textX = Math.max(textX, DELTATEXT);
		textY = Math.max(textY, bounds.height() + DELTATEXT);

		// if coordinates more then bottom and right border of the screen, move
		// it
		textX = Math.min(textX, Params.width - bounds.width() - DELTATEXT);
		textY = Math.min(textY, Params.height - (bounds.height() >> 1));

		if (mTopLine)
			textY = textY + shift;

		if (surface.canPressBytton()) {
			if (isPressed)
				paint.setColor(Params.colorBtnPressedText);
			else
				paint.setColor(Params.colorBtnText);

		} else
			paint.setColor(Params.colorBtnTextDisable);

		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawText(mVak, textX, textY, paint);
	}

}
