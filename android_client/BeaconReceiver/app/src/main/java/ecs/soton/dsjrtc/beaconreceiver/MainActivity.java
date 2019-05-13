package ecs.soton.dsjrtc.beaconreceiver;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import ecs.soton.dsjrtc.adobjects.*;
import ecs.soton.dsjrtc.beacondecoders.BeaconReceiver;

class DataBroadcastBeaconParser extends BeaconParser {
    public static final String TAG = "DataBroadcastBeaconParser";

    public DataBroadcastBeaconParser() {
        this.setBeaconLayout("m:2-3=b0dc,i:4-24,p:25-25");
        this.mIdentifier = "databroadcastbeacon";
    }

}

class TestPacketBeaconParser extends BeaconParser {
    public static final String TAG = "TestPacketBeaconParser";

    public TestPacketBeaconParser() {
        this.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        this.mIdentifier = "testpacketbeacon";
    }

}

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static String TAG = "Amazing Application";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BeaconManager beaconManager;
    private BeaconReceiver receiver;
    private BackgroundPowerSaver backgroundPowerSaver;

    private AdDisplayView myAdDisplay;
    private TextView myStatsDisplay;

    Random rand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rand = new Random();

        myAdDisplay = findViewById(R.id.adDisplay);
        myStatsDisplay = findViewById(R.id.statsTextView);
        myStatsDisplay.setMovementMethod(new ScrollingMovementMethod());

        beaconManager = BeaconManager.getInstanceForApplication(this);
        receiver = new BeaconReceiver();
        myAdDisplay.setReceiver(receiver);

        beaconManager.setDebug(true);
        beaconManager.setEnableScheduledScanJobs(true);

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new DataBroadcastBeaconParser());
        beaconManager.getBeaconParsers().add(new TestPacketBeaconParser());

        // check permissions
        verifyBluetooth();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }

        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager.setForegroundScanPeriod(500L); // 200ms
        beaconManager.setForegroundBetweenScanPeriod(0L); // 0ms
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot talk to service" + (e));
        }

        beaconManager.bind(this);
    }

    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();
        }
    }

    public void clearAd(View view) {
        // TODO: Do something with this
//        myAdDisplay.randomiseColor();
        generateTestObjects();
    }

    public void refreshAd(View view) {
        // TODO: Do something with this
        myStatsDisplay.setTextColor(Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                boolean dataReceived = receiver.handleBeacons(beacons);
                // Refresh the display to reflect any updates
                updateStatsDisplay();

                if (dataReceived) {
                    myAdDisplay.updateView();
                }
            }
        };
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {

        }
    }

    /**
     * Update the display with current beacon receive state.
     */
    public void updateStatsDisplay() {
        String toDisplay = receiver.getStatsString();
        clearDisplay();
        logToDisplay(toDisplay, true);
    }

    private void logToDisplay(final String line, final boolean html) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView editText = findViewById(R.id.statsTextView);
                if (html) {
                    editText.append(Html.fromHtml(line));
                } else {
                    editText.append(line + "\n");
                }
            }
        });
    }

    private void clearDisplay() {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView editText = findViewById(R.id.statsTextView);
                editText.setText("");
            }
        });
    }

    private void generateTestObjects() {
        // Adding text
        Drawable text = new ObjText(new Point(50, 50), 0, Color.BLUE, 64, 20, 10, "HelloWorld");

        // Adding polygon
        ArrayList<Point> tri = new ArrayList<Point>();
        tri.add(new Point(600, 600));
        tri.add(new Point(650, 500));
        tri.add(new Point(700, 600));
        Drawable triangle = new ObjPolygon(Color.CYAN, 3, tri);

        // Adding pure text
        Drawable pureText = new ObjText(0, Color.BLUE, 16, 0, 0, this.getResources().getString(R.string.stats_filler));

        // Adding a canvas
//        Drawable myCanvas = new ObjCanvas(200, 200, Color.MAGENTA);
//        myAdDisplay.addObject(myCanvas);

        // Adding polygon
        ArrayList<Point> rect = new ArrayList<Point>();
        rect.add(new Point(50, 50));
        rect.add(new Point(100, 50));
        rect.add(new Point(100, 100));
        rect.add(new Point(50, 100));
        Drawable rectangle = new ObjPolygon(Color.CYAN, 4, rect);
        myAdDisplay.addObject(rectangle);

        myAdDisplay.addObject(pureText);
        myAdDisplay.addObject(text);
        myAdDisplay.addObject(triangle);
    }
}
