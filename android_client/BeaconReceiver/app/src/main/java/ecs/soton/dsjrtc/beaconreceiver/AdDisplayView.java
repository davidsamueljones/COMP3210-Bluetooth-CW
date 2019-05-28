package ecs.soton.dsjrtc.beaconreceiver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioTrack;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import ecs.soton.dsjrtc.adobjects.Drawable;
import ecs.soton.dsjrtc.adobjects.ObjCanvas;
import ecs.soton.dsjrtc.adobjects.ObjImage;
import ecs.soton.dsjrtc.adobjects.ObjPolygon;
import ecs.soton.dsjrtc.adobjects.ObjText;
import ecs.soton.dsjrtc.adobjects.Point;
import ecs.soton.dsjrtc.beacondecoders.BeaconReceiver;
import ecs.soton.dsjrtc.beacondecoders.ReceivedData;
import ecs.soton.dsjrtc.parser.Parser;

public class AdDisplayView extends View {

    BeaconReceiver receiver;
    private Map<Integer, ReceivedData> allReceivedData;
    private int numAds;
    private int adIndex;
    private TextView indexText;

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
    }

    public void initView() {
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

        this.updateReceivedData();
        this.updateView();

//        this.makeKirkAd();
    }

    public void makeKirkAd() {
        // black background
        objList.add(new ObjCanvas(0, 0, Color.BLACK));
        // smaller kirk image
        objList.add(new ObjImage(3, new Point(300, 200), 0, 0, 0));
        // text
        objList.add(new ObjText(new Point(100, 200), 0, Color.GREEN, 48, 245, 0, "New from ECS productions!"));
        objList.add(new ObjText(new Point(700, 70), 0, Color.GREEN, 48, 35, 0, "Only limited stock!"));
        objList.add(new ObjText(new Point(100, 800), 0, Color.RED, 64, 0, 0, "Show this message for 99% off!"));
//        objList.add(new ObjText(new Point(), 0, Color.BLUE, 12, , 0, ""));
        // adding his crown
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point(400, 230));
        points.add(new Point(430, 290));
        points.add(new Point(460, 230));
        points.add(new Point(490, 290));
        points.add(new Point(520, 230));
        points.add(new Point(550, 290));
        points.add(new Point(580, 230));
        points.add(new Point(610, 290));
        points.add(new Point(640, 230));
        points.add(new Point(670, 290));
        points.add(new Point(670, 330));
        points.add(new Point(400, 330));
//        points.add(new Point());
        objList.add(new ObjPolygon(Color.YELLOW, points.size(), points));

        this.invalidate();
    }

    public void setIndexText(TextView indexText) {
        this.indexText = indexText;
    }

    private void updateReceivedData() {
        allReceivedData = receiver.allReceivedData;
        numAds = allReceivedData.size();

        if (numAds > 0 && adIndex == 0) {
            adIndex = 1;
        } else if (numAds == 0) {
            adIndex = 0;
        }
    }

    public void updateView() {
        updateReceivedData();
        // update the textview with the index in
        indexText.setText(MessageFormat.format("Page {0}/{1}", adIndex, numAds));

        // makes life simpler
        if (numAds == 0) {
            objList.clear();
            this.invalidate();
            return;
        }

        // Get the indexed ad
        ReceivedData data = null;
        Iterator<ReceivedData> itr = allReceivedData.values().iterator();
        int i = 1;
        while (itr.hasNext()) {
            ReceivedData current = itr.next();

            if (i == adIndex) {
                // need to remove the current ad
                data = current;
                break;
            }

            i++;
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
                    Parser parser = new Parser(context);
                    parser.parseStream(payload);
                    objList = parser.getObjList();
                default:
                    break;
            }
        }

//        byte[] test = {(byte) 0x03, (byte) 0x03, (byte) 0xe8, (byte) 0x01, (byte) 0xf4, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x2c, (byte) 0x00, (byte) 0x96, (byte) 0x01, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x32, (byte) 0xf8, (byte) 0x15, (byte) 0x42, (byte) 0x75, (byte) 0x79, (byte) 0x20, (byte) 0x6f, (byte) 0x75, (byte) 0x72, (byte) 0x20, (byte) 0x73, (byte) 0x77, (byte) 0x61, (byte) 0x6e, (byte) 0x6b, (byte) 0x79, (byte) 0x20, (byte) 0x63, (byte) 0x72, (byte) 0x6f, (byte) 0x77, (byte) 0x6e, (byte) 0x21, (byte) 0x05, (byte) 0x01, (byte) 0x2c, (byte) 0x02, (byte) 0x58, (byte) 0x01, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x32, (byte) 0x07, (byte) 0x13, (byte) 0x4f, (byte) 0x6e, (byte) 0x6c, (byte) 0x79, (byte) 0x20, (byte) 0x6c, (byte) 0x69, (byte) 0x6d, (byte) 0x69, (byte) 0x74, (byte) 0x65, (byte) 0x64, (byte) 0x20, (byte) 0x73, (byte) 0x74, (byte) 0x6f, (byte) 0x63, (byte) 0x6b, (byte) 0x21, (byte) 0x06, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0x11, (byte) 0x00, (byte) 0xc8, (byte) 0x00, (byte) 0xc8, (byte) 0x00, (byte) 0xfa, (byte) 0x00, (byte) 0xfa, (byte) 0x01, (byte) 0x2c, (byte) 0x00, (byte) 0xc8, (byte) 0x01, (byte) 0x5e, (byte) 0x00, (byte) 0xfa, (byte) 0x01, (byte) 0x90, (byte) 0x00, (byte) 0xc8, (byte) 0x01, (byte) 0xc2, (byte) 0x00, (byte) 0xfa, (byte) 0x01, (byte) 0xf4, (byte) 0x00, (byte) 0xc8, (byte) 0x02, (byte) 0x26, (byte) 0x00, (byte) 0xfa, (byte) 0x02, (byte) 0x58, (byte) 0x00, (byte) 0xc8, (byte) 0x02, (byte) 0x8a, (byte) 0x00, (byte) 0xfa, (byte) 0x02, (byte) 0xbc, (byte) 0x00, (byte) 0xc8, (byte) 0x02, (byte) 0xee, (byte) 0x00, (byte) 0xfa, (byte) 0x03, (byte) 0x20, (byte) 0x00, (byte) 0xc8, (byte) 0x03, (byte) 0x52, (byte) 0x00, (byte) 0xfa, (byte) 0x03, (byte) 0x84, (byte) 0x00, (byte) 0xc8, (byte) 0x03, (byte) 0x84, (byte) 0x01, (byte) 0x90, (byte) 0x00, (byte) 0xc8, (byte) 0x01, (byte) 0x90, (byte) 0x05, (byte) 0x00, (byte) 0xc8, (byte) 0x02, (byte) 0xee, (byte) 0x01, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x32, (byte) 0x00, (byte) 0x1e, (byte) 0x53, (byte) 0x68, (byte) 0x6f, (byte) 0x77, (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x6d, (byte) 0x65, (byte) 0x73, (byte) 0x73, (byte) 0x61, (byte) 0x67, (byte) 0x65, (byte) 0x20, (byte) 0x66, (byte) 0x6f, (byte) 0x72, (byte) 0x20, (byte) 0x39, (byte) 0x39, (byte) 0x25, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x21};
//        Parser parser = new Parser(context);
//        parser.parseStream(test);
//        this.setObjectList(parser.getObjList());

        this.invalidate();
    }

    public void deleteAd() {
        Iterator<Map.Entry<Integer, ReceivedData>> itr = allReceivedData.entrySet().iterator();

        int i = 1;
        while (itr.hasNext()) {
            Map.Entry<Integer, ReceivedData> entry = itr.next();
            if (i == adIndex) {
                // need to remove the current ad
                receiver.chunkDecoders.remove(entry.getKey());
                itr.remove();
                break;
            }

            i++;
        }

        adIndex -= 1;
        this.updateReceivedData();
        this.updateView();
    }

    public void previousAd() {
        this.updateReceivedData();

        if (adIndex <= 1) return;

        adIndex -= 1;
        this.updateView();
    }

    public void nextAd() {
        this.updateReceivedData();

        if (adIndex >= numAds) return;

        adIndex += 1;
        this.updateView();
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

//        canvas.drawColor(canvasColor);

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
