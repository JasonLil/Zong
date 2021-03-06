package com.xenoage.zong.android.renderer.background;

import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.xenoage.utils.android.color.AndroidColorUtils;
import com.xenoage.zong.layout.frames.background.Background;
import com.xenoage.zong.layout.frames.background.ColorBackground;

/**
 * Android background renderer.
 * 
 * @author Andreas Wenger
 */
public class AndroidBackgroundRenderer {

	/**
	 * Returns the color of the given background.
	 */
	public static Paint getPaint(Background background) {
		switch (background.getType()) {
			case Color:
				return getPaint((ColorBackground) background);
		}
		return null;
	}

	/**
	 * Returns the color of the given background.
	 */
	public static Paint getPaint(ColorBackground background) {
		Paint paint = new Paint();
		paint.setColor(AndroidColorUtils.createColor(background.getColor()));
		paint.setStyle(Style.FILL);
		return paint;
	}

}
