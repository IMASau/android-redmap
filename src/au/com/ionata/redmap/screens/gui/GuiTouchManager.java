package au.com.ionata.redmap.screens.gui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.TextView;

public class GuiTouchManager {

	public void setPressedColours(Iterable<TextView> views) {
		for (TextView view: views){
			setPressedColours(view);
		}
	}
	
	public void setPressedColours(TextView view) {

		ColorStateList oldColorList = view.getTextColors();
		int mainColor = oldColorList.getColorForState(new int[] {}, Color.BLACK);
		
		ColorStateList newColorList = new ColorStateList(
			new int[][] {
		        new int[] {android.R.attr.state_pressed, android.R.attr.state_focused},
		        new int[] {android.R.attr.state_pressed}, 
		        new int[] {android.R.attr.state_focused},
		        new int[] {},
		}, new int[] {
		        Color.WHITE,
		        Color.WHITE,
		        Color.WHITE,
		        mainColor
		});

		view.setTextColor(newColorList);
	}
	
    public void setPressedColours(EditText view) {

        ColorStateList oldColorList = view.getTextColors();
        int mainColor = oldColorList.getColorForState(new int[] {}, Color.BLACK);
        
        ColorStateList newColorList = new ColorStateList(
            new int[][] {
                new int[] {android.R.attr.state_pressed, android.R.attr.state_focused},
                new int[] {android.R.attr.state_pressed}, 
                new int[] {android.R.attr.state_focused},
                new int[] {},
        }, new int[] {
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                mainColor
        });

        view.setTextColor(newColorList);
    }
	
}
