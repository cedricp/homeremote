package com.mycode.cedric.swGate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ToggleButton;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cedric on 12/9/15.
 */
public class swGateControl extends ControlExtension implements swGateHttpRequest.onHttpRequestComplete{
    protected int mLastKnowPosition = 0;
    protected boolean mConnected = false;
    private String httpServer, port, key;
    private String[][] mLayoutContent = {
            {"Cars yard", "Gate", "Car Light", "temp"},
            {"Backyard", "WallLight1", "WallLight2", "SwPoolLight", "BackyardLight", "All ON", "All Off"}
    };
    private String water_temp = "";
    private String air_temp = "";

    boolean walllight1_status, walllight2_status, swimmingpoollight_status, backyardlight_status;

    swGateControl(Context context, String hostAppPackageName) {
        super(context, hostAppPackageName);
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        httpServer = SP.getString("host", "");
        port = SP.getString("port", "");
        key = SP.getString("key", "");
    }

    @Override
    public void onHttpRequestComplete(String s) {
        if (s.equals("NOK")){
            sendListCount(R.id.gallery, 0);
            mConnected = false;
            return;
        }

        try {
            JSONObject jsonOb = new JSONObject(s);
            JSONArray relays = jsonOb.getJSONArray("RELAYSSTAUS");

            water_temp = jsonOb.getString("WATERTEMP");
            air_temp = jsonOb.getString("AIRTEMP");
            walllight1_status = relays.get(0).toString().equals("1") ? true : false;
            walllight2_status = relays.get(1).toString().equals("1") ? true : false;
            swimmingpoollight_status = relays.get(3).toString().equals("1") ? true : false;
            backyardlight_status = relays.get(2).toString().equals("1") ? true : false;
        }

        catch (JSONException e){
            e.printStackTrace();
            sendListCount(R.id.gallery, 0);
            mConnected = false;
            return;
        }

        // Regenerate list items page 2
        ControlListItem item = createControlListItem(1);
        if (item != null) {
            sendListItem(item);
        }

        mConnected = true;
    }

    private void sendHttpRequest(String s){
        if (port.isEmpty() || httpServer.isEmpty())
            return;
        if (s.isEmpty())
            (new swGateHttpRequest(this)).execute("http://" + httpServer + ":" + port + "/control.py?key=" + key);
        else
            (new swGateHttpRequest(this)).execute("http://" + httpServer + ":" + port + "/control.py?key=" +key + "&" + s);
    }

    @Override
    public void onResume() {
        // Control is now visible. Start listening for changes.
        showLayout(R.layout.control_main_layout, null);
        sendListCount(R.id.gallery, mLayoutContent.length);
        sendListPosition(R.id.gallery, mLastKnowPosition);
        sendHttpRequest("");
    }

    @Override
    public void onPause() {
        // Control is no longer visible. Stop listening for changes.

    }

    @Override
    public void onListItemSelected(ControlListItem listItem) {
        super.onListItemSelected(listItem);
        // We save the last "selected" position, this is the current visible
        // list item index. The position can later be used on resume
        mLastKnowPosition = listItem.listItemPosition;
    }

    @Override
    public void onRequestListItem(final int layoutReference, final int listItemPosition) {
        Log.d(swGateExtensionService.LOG_TAG, "onRequestListItem() - position " + listItemPosition);
        if (layoutReference != -1 && listItemPosition != -1 && layoutReference == R.id.gallery) {
            ControlListItem item = createControlListItem(listItemPosition);
            if (item != null) {
                sendListItem(item);
            }
        }
    }

    @Override
    public void onKey(final int action, final int keyCode, final long timeStamp) {
        if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
            toggleMenu();
        }
        else if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_BACK) {
        }
    }

    @Override
    public void onListItemClick(final ControlListItem listItem, final int clickType,
                                final int itemLayoutReference) {

        if (clickType == Control.Intents.CLICK_TYPE_SHORT && listItem.listItemId == 1) {
            switch (itemLayoutReference) {
                case R.id.topButton:
                    startVibrator(200,0,1);
                    sendHttpRequest("setrelay=0&status=" + Integer.valueOf(walllight1_status == true ? 0 : 1));
                    break;
                case R.id.centerButton:
                    startVibrator(200,0,1);
                    sendHttpRequest("setrelay=1&status=" + Integer.valueOf(walllight2_status == true ? 0 : 1));
                    break;
                case R.id.bottomButton:
                    startVibrator(200,0,1);
                    sendHttpRequest("setrelay=3&status=" + Integer.valueOf(swimmingpoollight_status == true ? 0 : 1));
                    break;
                case R.id.topRight:
                    startVibrator(200,0,1);
                    sendHttpRequest("setrelay=2&status=" + Integer.valueOf(backyardlight_status == true ? 0 : 1));
                    break;
                case R.id.centerRight:
                    startVibrator(200,0,1);
                    sendHttpRequest("setrelay=0,1,2,3&status=1");
                    break;
                case R.id.bottomRight:
                    startVibrator(200,0,1);
                    sendHttpRequest("setrelay=0,1,2,3&status=0");
                    break;
            }
        }
    }

    public void onMenuItemSelected(final int menuItem) {
        if (menuItem == 0){
            onResume();
        }
    }

    private void toggleMenu(){
        Bundle mMenuItemsText[] = new Bundle[3];
        mMenuItemsText[0] = new Bundle();
        mMenuItemsText[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, 0);
        mMenuItemsText[0].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Reconnect");
        mMenuItemsText[1] = new Bundle();
        mMenuItemsText[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, 1);
        mMenuItemsText[1].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "NoOp");
        mMenuItemsText[2] = new Bundle();
        mMenuItemsText[2].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, 2);
        mMenuItemsText[2].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "NoOp");
        showMenu(mMenuItemsText);

    }

    protected ControlListItem createControlListItem(int position) {
        if (position == 0){
            return createControlListItemCar();
        }

        if(position == 1){
            return createControlListItemExterior();
        }

        return null;
    }

    protected ControlListItem createControlListItemExterior(){
        ControlListItem item = new ControlListItem();
        item.layoutReference = R.id.gallery;
        item.dataXmlLayout = R.layout.exterior_layout;
        item.listItemId = 1;
        item.listItemPosition = 1;

        String topState = "Off";
        String centerState = "Off";
        String bottomState = "Off";
        String topRightState = "Off";

        String headertext = mLayoutContent[1][0];

        if (walllight1_status) topState = "On";
        if (walllight2_status) centerState = "On";
        if (swimmingpoollight_status) bottomState = "On";
        if (backyardlight_status) topRightState = "On";

        headertext += " | Air:" + air_temp + "| Water:" + water_temp;

        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();
        Bundle bundle3 = new Bundle();
        Bundle bundle4 = new Bundle();
        Bundle bundle5 = new Bundle();
        Bundle bundle6 = new Bundle();
        Bundle bundle7 = new Bundle();
        bundle1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.topButton);
        bundle1.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[1][1] + ":" + topState);
        bundle2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.centerButton);
        bundle2.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[1][2] + ":" + centerState);
        bundle3.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.bottomButton);
        bundle3.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[1][3] + ":" + bottomState);

        bundle4.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.headerText);
        bundle4.putString(Control.Intents.EXTRA_TEXT, headertext);

        bundle5.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.topRight);
        bundle5.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[1][4] + ":" + topRightState);
        bundle6.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.centerRight);
        bundle6.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[1][5]);
        bundle7.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.bottomRight);
        bundle7.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[1][6]);
        item.layoutData = new Bundle[7];
        item.layoutData[0] = bundle1;
        item.layoutData[1] = bundle2;
        item.layoutData[2] = bundle3;
        item.layoutData[3] = bundle4;
        item.layoutData[4] = bundle5;
        item.layoutData[5] = bundle6;
        item.layoutData[6] = bundle7;

        return item;
    }

    protected ControlListItem createControlListItemCar() {
        ControlListItem item = new ControlListItem();
        item.layoutReference = R.id.gallery;
        item.dataXmlLayout = R.layout.control_layout;
        item.listItemId = 0;
        item.listItemPosition = 0;
        String headertext = mLayoutContent[0][0];

        Bundle bundle1 = new Bundle();
        Bundle bundle2 = new Bundle();
        Bundle bundle3 = new Bundle();
        Bundle bundle4 = new Bundle();
        bundle1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.topButton);
        bundle1.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[0][1]);
        bundle2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.centerButton);
        bundle2.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[0][2]);
        bundle3.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.bottomButton);
        bundle3.putString(Control.Intents.EXTRA_TEXT, mLayoutContent[0][3]);
        bundle4.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.headerText);
        bundle4.putString(Control.Intents.EXTRA_TEXT, headertext);
        item.layoutData = new Bundle[4];
        item.layoutData[0] = bundle1;
        item.layoutData[1] = bundle2;
        item.layoutData[2] = bundle3;
        item.layoutData[3] = bundle4;


        return item;
    }
}
