package ecs.soton.dsjrtc.adobjects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class Advert implements Drawable {

    private Resources res;

    ArrayList<Drawable> objList;

    public Advert() {
        objList = new ArrayList<Drawable>();
    }

    @Override
    public void draw(Context context, Canvas canvas, Paint mPaint) {
        for (Drawable object : objList) {
            object.draw(context, canvas, mPaint);
        }
    }
}
