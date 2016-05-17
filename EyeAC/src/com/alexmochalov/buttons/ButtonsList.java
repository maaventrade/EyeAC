package com.alexmochalov.buttons;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.alexmochalov.eyeac.Params;
import com.alexmochalov.eyeac.SurfaceViewScreenButtons;
import com.alexmochalov.eyeac.SurfaceViewScreenButtons.OnEventListener;
import com.alexmochalov.animation.SurfaceViewScreen;

/**
 * 
 * @author @Alexey Mochalov
 *	Class ButtonsList contains a list of the screen buttons
 */
public class ButtonsList {
	private static ArrayList <ButtonVAC> buttons = new ArrayList<ButtonVAC>(); // List of the screen buttons
	private static ButtonVAC buttonSelected = null; // Selected (highlighted) button

	private static final int DELTAX = 25;
	private static final int DELTAY = 25;
	
	public static OnEventListener listener;
	public interface OnEventListener{
		void onTouchDown(String VAC);
		void onTouchUp();
	}
	
	public String[] getVAK() { // Returns an array of the buttons titles
		String vak[] = new String[8];
		for (int i = 0; i <= 7; i++){
			vak[i] = buttons.get(i).getVAK();
		}
		return vak;
	}

	public String getVAKStr() { // Returns a concatenation of the buttons titles
		String str = "";

		str = buttons.get(0).getVAK();
		for (int i = 1; i < 8; i++)
			str = str+","+buttons.get(i).getVAK();
		return str;
	}
	
	
	public void setVAK(String[] array) { // Sets the buttons titles 
		for (int i=0 ; i<array.length; i++)
			buttons.get(i).setVAK(array[i]);
	}
	
	public void setVAKi(int i, String s) { // Sets the button title 
		buttons.get(i).setVAK(s);
	}
	
	public static void clear() {
		buttons.clear();
		buttonSelected = null;
	}

	/**
	 * Creates buttons, fill list of the buttons. Sets coordinates of the buttons. 
	 * @param width - width of the screen
	 * @param height - height of the screen
	 * @param allDirections - if true, create 9 buttons else 6
	 */
	public static void setButtonsRects(int width, int height, boolean allDirections) {
		ButtonsList.clear();

		int S = 50;
		int S2 = 25;

		int h1, w1;

		w1 = width;
		h1 = height;

		int w2 = width >> 1;
		int h2 = h1 >> 1;
		S = h1 / 4;
		S2 = S >> 1;

		Params.fontSize = (Math.min(width, height) / 4) >> 1;
		// fontSize2 = S >> 2;
		Params.fontSize4 = Math.min(width, height) >> 3;
		// fontSize8 = S >> 4;

		String vak[] = { "Vc", "Up", "Vr", "Ar", "Ad", "Dn", "K", "Ac", "F" };

		int dx = width / 4;
		int dx1 = (width - dx * 2) / 2 - DELTAX;

		int dxVert = width / 5;

		int dy = (h1 - S * 2) / 2 - DELTAY;

		RectF rect = new RectF(-dx, -S, dx, S); // VC
		buttons.add(new ButtonVAC(0, rect, vak[0], true));

		if (allDirections) {
			rect = new RectF(w2 - dx1, -S, w2 + dx1, S - 20); // UP
			buttons.add(new ButtonVAC(1, rect, vak[1], true));
		}

		rect = new RectF(width - dx, -S, width + dx, S); // VR
		buttons.add(new ButtonVAC(2, rect, vak[2], true));

		if (width > height)
			rect = new RectF(width - S, h2 - dy, width + S, h2 + dy); // AR
		else
			rect = new RectF(width - dxVert, h2 - dy, width + dxVert, h2 + dy); // AR

		buttons.add(new ButtonVAC(3, rect, vak[3], false));

		rect = new RectF(width - dx, h1 - S, width + dx, h1 + dx); // AD
		buttons.add(new ButtonVAC(4, rect, vak[4], false));

		if (allDirections) {
			rect = new RectF(w2 - dx1, h1 - S + 20, w2 + dx1, h1 + 50); // DN
			buttons.add(new ButtonVAC(5, rect, vak[5], false));
		}

		rect = new RectF(-dx, h1 - S, dx, h1 + dx); // �
		buttons.add(new ButtonVAC(6, rect, vak[6], false));

		if (width > height)
			rect = new RectF(-S, h2 - dy, S, h2 + dy); // AC
		else
			rect = new RectF(-dxVert, h2 - dy, dxVert, h2 + dy); // AC

		buttons.add(new ButtonVAC(7, rect, vak[7], false));

		if (allDirections) {
			// int d8 = (width - S*2 )/2-20;
			rect = new RectF(w2 - dx1, h2 - dy, w2 + dx1, h2 + dy); // F
			buttons.add(new ButtonVAC(8, rect, vak[8], false));
		} 
	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param surfaceViewScreenButtons
	 * @param textTopShift
	 */
	public static void draw(Canvas canvas, Paint paint,
			SurfaceViewScreenButtons surfaceViewScreenButtons, int textTopShift) {
		
	    for (ButtonVAC b: buttons)
			b.draw(canvas, paint, surfaceViewScreenButtons, textTopShift);
	}
	
	public static void buttonUp(){
		if (buttonSelected != null)
			buttonSelected.setPressed(false);
		
		if (listener != null)
			listener.onTouchUp();
		
		buttonSelected = null;
	}

	public static String ACTION_DOWN(int x, int y) {
		for (int i=0; i<buttons.size(); i++)
			if (buttons.get(i).contains(x, y) && ! buttons.get(i).getVAK().equals("")){
				buttonSelected = buttons.get(i);
				buttonSelected.setPressed(true);
				return buttonSelected.getVAK();
			}	
		return null;
	}

	public static boolean ACTION_UP() {
    	if (buttonSelected != null){
			buttonSelected.setPressed(false);
			buttonSelected = null;
			return true;
    	}
		return false;
	}
	
	

}
