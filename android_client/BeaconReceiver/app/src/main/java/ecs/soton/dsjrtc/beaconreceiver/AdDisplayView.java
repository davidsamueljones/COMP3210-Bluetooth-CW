package ecs.soton.dsjrtc.beaconreceiver;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import ecs.soton.dsjrtc.adobjects.Drawable;
import ecs.soton.dsjrtc.adobjects.ObjImage;
import ecs.soton.dsjrtc.adobjects.ObjText;
import ecs.soton.dsjrtc.adobjects.Point;
import ecs.soton.dsjrtc.beacondecoders.BeaconReceiver;
import ecs.soton.dsjrtc.beacondecoders.ReceivedData;
import ecs.soton.dsjrtc.parser.Parser;

public class AdDisplayView extends View {

    BeaconReceiver receiver;
    private int index = 1;

    Canvas mCanvas;
    Bitmap mBitmap;
    Paint mPaint;
    Context context;
    private int canvasColor;

    private Random rand;

    ArrayList<Drawable> objList;

    private android.graphics.drawable.Drawable kirkImage;
    private Bitmap kirkBitmap;

    public AdDisplayView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);
        mPaint.setTextSize(64f);

        rand = new Random();
        randomiseColor();

        objList = new ArrayList<Drawable>();
    }

    public void updateView() {
        Map<Integer, ReceivedData> allReceivedData = receiver.allReceivedData;

        // Get the indexed ad
        ReceivedData data = null;
        Iterator<ReceivedData> itr = allReceivedData.values().iterator();
        int i = 0;
        while(itr.hasNext() && (i < index)) {
            data = itr.next();
        }

        // add all the contents of the current selected ad
        objList = new ArrayList<Drawable>();
        if (data != null) {
            byte[] payload = data.getPayload();

            switch (data.getType()) {
                case TEXT:
                    Drawable text = new ObjText(payload);
                    objList.add(text);
                    break;
                case IMAGE:
                    Drawable image = new ObjImage(payload);
                    objList.add(image);
                    break;
                case ADVERT:
                    Parser parser = new Parser();
                    parser.parseStream(payload);
                    objList = parser.getObjList();
                default:
                    break;
            }
        }

        this.invalidate();
    }

    public void setReceiver(BeaconReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(canvasColor);

//        new ObjImage(1, new Point(0,0), 200, 200, 0).draw(context, canvas, mPaint);

        for (Drawable object : objList)
            object.draw(context, canvas, mPaint);
    }

    public void setObjectList(ArrayList<Drawable> objList) {
        this.objList = objList;
    }

    public void addObject(Drawable object) {
        objList.add(object);
        invalidate();
    }

    public void randomiseColor() {
        canvasColor = Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        this.invalidate();
    }
}
