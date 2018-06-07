package com.example.sarapmax.iot_project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends BlunoLibrary {
    private Button buttonScan;

    private TextView serialReceivedText;

    private TextView mTextMessage;
    private ImageView ulcerImage;
    private TextView ulcerRiskText;
    private String tempString = ""; //used to store the framented message
    private boolean completed = false;  //used to let the program know if the message is sent over completely
    private ArrayList<String> data = new ArrayList<>();
    private final int ULCER_THRESHOLD = 100;    //more than 100 is the threshold as set by a journal paper

//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_health:
//                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                    return true;
//                case R.id.navigation_profile:
//                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
//                    return true;
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateProcess();

//        serialBegin(115200);
        serialBegin(9600);

//        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        navigation.getMenu().getItem(0).setChecked(true);

        serialReceivedText = (TextView) findViewById(R.id.serialReveicedText);


        ulcerImage = (ImageView) findViewById(R.id.UlcerImage);
        ulcerRiskText = (TextView) findViewById(R.id.UlcerRiskText);

        buttonScan = (Button) findViewById(R.id.buttonScan);                    //initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                buttonScanOnClickProcess();                                        //Alert Dialog for selecting the BLE device
            }
        });

        ulcerImage.setImageResource(R.drawable.ulcer0);
        ulcerRiskText.setText("Healthy");
        ulcerRiskText.setTextColor(getResources().getColor(R.color.colorSuccess));
    }

    protected void onResume() {
        super.onResume();
        onResumeProcess();                                                        //onResume Process by BlunoLibrary
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);                    //onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();                                                        //onPause Process by BlunoLibrary
    }

    protected void onStop() {
        super.onStop();
        onStopProcess();                                                        //onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();                                                        //onDestroy Process by BlunoLibrary
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {                                            //Four connection state
            case isConnected:
                buttonScan.setText("Connected");
                Toast.makeText(this, "Connected!", Toast.LENGTH_LONG).show();
                break;
            case isConnecting:
                buttonScan.setText("Connecting");
                break;
            case isToScan:
                buttonScan.setText("Scan");
                break;
            case isScanning:
                buttonScan.setText("Scanning");
                break;
            case isDisconnecting:
                buttonScan.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    int count = 0;

    @Override
    public void onSerialReceived(String theString) {

        //resets the arraylist when it reaches 100 data
        if(data.size() > 100){
            data.clear();
        }


        //run the check
        switch (getStatus()) {
            case 'T':
                ulcerImage.setImageResource(R.drawable.ulcer2);
                ulcerRiskText.setText("Top right");
                ulcerRiskText.setTextColor(getResources().getColor(R.color.colorDanger));
                break;
            case 'L':
                ulcerImage.setImageResource(R.drawable.ulcer4);
                ulcerRiskText.setText("Bottom right");
                ulcerRiskText.setTextColor(getResources().getColor(R.color.colorDanger));
                break;
            case 'B':
                ulcerRiskText.setText("Right");
                ulcerRiskText.setTextColor(getResources().getColor(R.color.colorDanger));
                ulcerImage.setImageResource(R.drawable.ulcer8);
                break;
            default:
                ulcerRiskText.setText("Healthy");
                ulcerRiskText.setTextColor(getResources().getColor(R.color.colorSuccess));
                ulcerImage.setImageResource(R.drawable.ulcer0);
                break;
        }

        //Once connection data received, this function will be called
        // TODO Auto-generated method stub
        tempString += theString;
        String cleanedString = "";
        if (theString.contains("\n") || theString.contains("\r")) {
            if ((tempString.indexOf("\r") > 0) || (tempString.indexOf("\n") > 0)) {      //some lines will be only /r or /n
                cleanedString = tempString.substring(0, tempString.indexOf("\r"));//substring to get the clean string
                int lengthOfString = cleanedString.length();    //save the length of string to use for substring later
                cleanedString = cleanedString.replace("\n", "").replace("\r", "");    //remove the newline

                tempString = tempString.substring(lengthOfString, tempString.length()); //save the behind value to prevent it from being erased
                tempString = tempString.replace("\n", "").replace("\r", "");    //remove the newline

                completed = true;
            } else {
                String s = "";
            }
        }

        if (completed) {  //add into arraylist
            data.add(cleanedString);
            completed = false;  //reset the boolean
        }

        //for debug purposse
        serialReceivedText.append(theString);
        ((ScrollView) serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);
    }

    /***
     * gets the status of the sole based on the data in the arraylist
     * if the count is more than 50% of the total data, means that the user is likely to have hit the threshold
     * @return T = top, L = lower, B = both, 0 if there is no data
     */
    private char getStatus() {
        boolean topUlcerPresent = false;
        boolean btmUlcerPresent = false;
        ArrayList<Integer> topList = new ArrayList<>();
        ArrayList<Integer> btmList = new ArrayList<>();
        if (data.size() > 0) {    //only run if there is data
            for (String s : data) {
                int ulcerRiskValue = Integer.parseInt(s.substring(s.indexOf(">") + 1).trim());
                if (s.contains("top")) {
                    topList.add(ulcerRiskValue);
                } else if (s.contains("bottom")) {
                    btmList.add(ulcerRiskValue);
                }
            }

            //run the arraylists to check if 50% of the value hit the threshold
            int topCount = 0;
            for (int top : topList) {
                if (top > ULCER_THRESHOLD) {
                    topCount++;
                }
            }
            if (topCount > (topList.size() / 2)) {
                topUlcerPresent = true;
            }
            int btmCount = 0;
            for (int btm : btmList) {
                if (btm > ULCER_THRESHOLD) {
                    btmCount++;
                }
            }
            if (btmCount > (btmList.size() / 2)) {
                btmUlcerPresent = true;
            }

            //return the result back
            if (topUlcerPresent && btmUlcerPresent) {
                return 'B';
            } else if (topUlcerPresent) {
                return 'T';
            } else if (btmUlcerPresent) {
                return 'L';
            }
        }
        return '0';
    }

}

