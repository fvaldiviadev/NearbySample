package fvaldiviadev.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.DataTransferedAdapter;
import fvaldiviadev.nearbysample.R;
import pojo.DataTransfered;
import utils.Utils;

public class MainActivity extends AppCompatActivity {

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static Strategy STRATEGY = Strategy.P2P_CLUSTER;

    private static String SERVICE_ID="";

    private static String TAG_LOGS="NearbySample";

//    private static String MODE="CLUSTER";
    //Device 1 is the advertiser
    private static String MODE="ADVERTISING";
    //Device 2 is the discover
//    private static String MODE ="DISCOVERY";



    private TextView textViewConnected;

    private Button buttonConnect;
    private Button buttonSend;

    private EditText editTextMessage;

    private ListView listViewMessages;

    private Context context;

    private String idDeviceConnected;
    private String nameDeviceConnected;

    private DataTransfered myDataTransfered;
    private DataTransfered herDataTransfered;

    private Gson gson;

    private Map<String,Payload> incomingPayloads;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;

        SERVICE_ID= getPackageName();

        textViewConnected =findViewById(R.id.tv_connected);
//        textViewMessagesReceived =findViewById(R.id.tv_messages_received);

        buttonConnect =findViewById(R.id.b_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttonConnect.getText().equals(getString(R.string.connect))) {
                    findDevices();
                }else{
                    disconnect();
                }
            }
        });
        buttonSend =findViewById(R.id.b_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        editTextMessage =findViewById(R.id.et_message);

        listViewMessages =findViewById(R.id.lv_messages);
        List<DataTransfered> listDataTransfered=new ArrayList<DataTransfered>();
        DataTransferedAdapter promoAdapter = new DataTransferedAdapter(this, R.layout.item_data_transfered, listDataTransfered);
        listViewMessages.setAdapter(promoAdapter);

        connectionsClient = Nearby.getConnectionsClient(this);

        gson=new Gson();

        setTitle(MODE);

        incomingPayloads=new HashMap<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onStop() {
        connectionsClient.stopAllEndpoints();
        resetUI();

        super.onStop();
    }
    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void findDevices(){
        if(getTitle().equals("CLUSTER")){
            STRATEGY=Strategy.P2P_CLUSTER;
            startAdvertising();
            startDiscovery();
        }else if(getTitle().equals("ADVERTISING")){
            STRATEGY=Strategy.P2P_STAR;
            startAdvertising();
        }else if(getTitle().equals("DISCOVERY")){
            STRATEGY=Strategy.P2P_STAR;
            startDiscovery();
        }
        textViewConnected.setText(getString(R.string.connecting));
        buttonConnect.setEnabled(false);
    }

    private void startAdvertising() {
        Nearby.getConnectionsClient(context).startAdvertising(
                getUserNickname(),
                SERVICE_ID,
                mConnectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.i(TAG_LOGS, "We're advertising!");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG_LOGS, "Failed. We were unable to start advertising. e: "+e);
                            }
                        });
    }

    private String getUserNickname(){
        return Utils.getDeviceName();
    }


    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG_LOGS, getString(R.string.accepting_connection));
                    textViewConnected.setText(getString(R.string.accepting_connection));
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    nameDeviceConnected = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG_LOGS, getString(R.string.success_connecting_to)+ " " +nameDeviceConnected);
                        textViewConnected.setText(getString(R.string.success_connecting_to)+ " "+nameDeviceConnected);

                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();

                        idDeviceConnected = endpointId;

                        buttonConnect.setText(getString(R.string.disconnect));
                        buttonConnect.setEnabled(true);
                        buttonSend.setEnabled(true);
                        editTextMessage.setEnabled(true);
                    } else {
                        textViewConnected.setText(R.string.connection_failed);
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG_LOGS, getString(R.string.disconnected_of)+ " " + nameDeviceConnected);
                    textViewConnected.setText(getString(R.string.disconnected_of)+ " " + nameDeviceConnected);
                    resetUI();
                }
            };


    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    if(payload.getType() == Payload.Type.BYTES) {
                        String payloadJSON = new String(payload.asBytes());
                        Log.i(TAG_LOGS, "onPayloadReceived: " + payloadJSON);

                        herDataTransfered = gson.fromJson(payloadJSON, DataTransfered.class);

                        DataTransferedAdapter adapter = (DataTransferedAdapter) listViewMessages.getAdapter();
                        adapter.add(herDataTransfered);
                        adapter.notifyDataSetChanged();

                    }else if(payload.getType()==Payload.Type.FILE){
                        incomingPayloads.put(endpointId, payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    try {
                        if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                            Payload payload = incomingPayloads.remove(endpointId);
                            if(payload!=null) {
                                if (payload.getType() == Payload.Type.FILE) {
                                    File payloadFile = payload.asFile().asJavaFile();
                                    InputStream is = null;

                                    is = new FileInputStream(payloadFile);

                                    int size = is.available();
                                    byte[] buffer = new byte[size];
                                    is.read(buffer);
                                    is.close();
                                    String payloadJSON = new String(buffer, "UTF-8");

//                            String payloadJSON=new String(payload.asBytes());

                                    herDataTransfered = gson.fromJson(payloadJSON, DataTransfered.class);

                                    Log.i(TAG_LOGS, "onPayloadReceived: " + payloadJSON);

                                    DataTransferedAdapter adapter = (DataTransferedAdapter) listViewMessages.getAdapter();
                                    adapter.add(herDataTransfered);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException io) {

                    }
                }

            };

    private void resetUI(){
        buttonConnect.setText(getString(R.string.connect));
        buttonConnect.setEnabled(true);
        buttonSend.setEnabled(false);
        editTextMessage.setEnabled(false);
    }

    /** Starts looking for other players using Nearby Connections. */
    private void startDiscovery() {
        Nearby.getConnectionsClient(context).startDiscovery(
                SERVICE_ID,
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(STRATEGY))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.i(TAG_LOGS, "We're discovering!");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG_LOGS, "Failed. We were unable to start discovering.e: "+e);

                            }
                        });
    }


    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.i(TAG_LOGS, "onEndpointFound: endpoint found, connecting");
                    connectionsClient.requestConnection(getUserNickname(), endpointId, mConnectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    Log.i(TAG_LOGS, "onEndpointLost: endpoint lost");
                    // A previously discovered endpoint has gone away.
                }
            };

    /** Send data */
    private void sendData() {
        try {
            DataTransfered myDataTransfered=new DataTransfered();
            myDataTransfered.setMessage(editTextMessage.getText().toString());
            String imageBase64=getString(R.string.image_base64_sample);
            myDataTransfered.setImageBase64(imageBase64);

            this.myDataTransfered = myDataTransfered;
            String dataTransferedJSON=gson.toJson(myDataTransfered);
            Writer output = null;
            String path = getFilesDir().getAbsolutePath()+ File.separator+"data.json";
            File file = new File(path);
            output = new BufferedWriter(new FileWriter(file));
            output.write(dataTransferedJSON);
            output.close();

            Payload filePayload = Payload.fromFile(file);

            connectionsClient.sendPayload(idDeviceConnected, filePayload);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /** Disconnects from the device and reset the UI. */
    public void disconnect() {
        connectionsClient.disconnectFromEndpoint(idDeviceConnected);
        resetUI();
    }
}
