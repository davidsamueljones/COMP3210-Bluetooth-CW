package org.altbeacon.beaconreference;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Html;
import android.widget.EditText;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.ltdecoder.LTBlock;
import org.ltdecoder.LTDecoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private BeaconReceiver receiver = new BeaconReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                receiver.handleBeacons(beacons);
                // Refresh the display to reflect any updates
                updateStatsDisplay();
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
                EditText editText = (EditText) RangingActivity.this.findViewById(R.id.rangingText);
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
                EditText editText = (EditText) RangingActivity.this.findViewById(R.id.rangingText);
                editText.setText("");
            }
        });
    }
}
