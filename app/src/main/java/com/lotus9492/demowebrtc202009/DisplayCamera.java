package com.lotus9492.demowebrtc202009;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DisplayCamera extends AppCompatActivity implements SignallingClient.SignalingInterface {

    Intent intent;
    Button btnEnd;
    VideoCapturer videoCapturerAndroid;
    PeerConnectionFactory peerConnectionFactory;
    PeerConnectionFactory.Options options;
    //List<IceServer> iceServers;
    IceServer iceServer;
    List<PeerConnection.IceServer> peerIceServers  = new ArrayList<>();
    MediaConstraints sdpConstraints;
    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;
    VideoSource videoSource;
    VideoTrack localVideoTrack;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    SurfaceViewRenderer myVideoView;
    SurfaceViewRenderer peerVideoView;
    SurfaceTextureHelper surfaceTextureHelper;
    PeerConnection myConnection, peerConnection;
    EglBase rootEglBase;
    boolean gotUserMedia;
    final int ALL_PERMISSIONS_CODE = 1;
    PeerConnection.RTCConfiguration rtcConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_camera);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        } else {
            // all permissions already granted
            start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ALL_PERMISSIONS_CODE
                && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // all permissions granted
            start();
        } else {
            finish();
        }
    }

    /**
    *
    */
    public void initViews(){
        Log.d("initViewsMethod","initViewsMethod called");

        //init surface renderer
        myVideoView = (SurfaceViewRenderer)findViewById(R.id.myVideoView);
        peerVideoView = (SurfaceViewRenderer)findViewById(R.id.peerVideoView);

        btnEnd =(Button)findViewById(R.id.btnEnd);
        intent = null;
    }

    /**
     *
     */
    public void initVideos(){
        Log.d("initVideosMethod","initVideosMethod called");

        //create an EglBase instance
        rootEglBase = EglBase.create();

        //init the SurfaceViewRenderer using the eglContext
        myVideoView.init(rootEglBase.getEglBaseContext(), null);
        peerVideoView.init(rootEglBase.getEglBaseContext(), null);

        myVideoView.setZOrderMediaOverlay(true);
        peerVideoView.setZOrderMediaOverlay(true);
    }

    /**
     *
     */
    private void getIceServers() {
        Log.d("getIceServersMethod","getIceServersMethod called");

        //get Ice servers using xirsys
        byte[] data = new byte[0];
        try {
            data = ("lotus9492:efb4281c-eb67-11ea-91cd-0242ac150003").getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d("getIceServersErr", "getIceServers error");
            e.printStackTrace();
        }
        String authToken = "Basic " + Base64.encodeToString(data, Base64.NO_WRAP);
        Log.d("authToken", "authToken " + authToken);


        Map<String, Object> jsonParams = new ArrayMap<>();
        //put something inside the map, could be null
        jsonParams.put("format", "urls");
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(jsonParams)).toString());

                Utils.getInstance().getRetrofitInstance().getIceCandidates(authToken,body).enqueue(new Callback<TurnServerPojo>() {

                    @Override
                    public void onResponse(@NonNull Call<TurnServerPojo> call, @NonNull Response<TurnServerPojo> response) {
                        Log.d("onResponse", "onResponse called");
                        TurnServerPojo body = response.body();

                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        Log.d("getbody", gson.toJson(response.body()));

                        if (body != null) {
                            iceServer = body.v.iceServer;
                            Log.d("iceServers", String.valueOf(iceServer));

                        }
                        for (String url: iceServer.urls) {
                            if (url == null) {
                                continue;
                            }
                            PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(url).createIceServer();
                            Log.d("peerIceServer", String.valueOf(peerIceServer));
                            peerIceServers.add(peerIceServer);
                        }
                        Log.d("onApiResponse", "IceServers\n" + iceServer.toString());
                    }

                    @Override
                    public void onFailure(@NonNull Call<TurnServerPojo> call, @NonNull Throwable t) {
                        Log.d("onFailureErr", "onFailure error");
                        t.printStackTrace();
                    }
                });
    }

    /**
     *
     */
    public void start(){
        Log.d("startMethod","startMethod called");

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();
        initVideos();
        getIceServers();

        SignallingClient.getInstance().init(this);

        //Initialize PeerConnectionFactory globals
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);


        //Create a new PeerConnectionFactory instance - - using Hardware encoder and decoder
        options = new PeerConnectionFactory.Options();
        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(),  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory();

        //Create a VideoCapturer instance which uses the camera of the device
        videoCapturerAndroid = createVideoCapturer();

        //Create MediaConstraints - Will be useful for specifying video and audio constraints.
        audioConstraints = new MediaConstraints();
        videoConstraints = new MediaConstraints();

        //Create a VideoSource instance
        if (videoCapturerAndroid != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
            videoCapturerAndroid.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        }
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        //create an AudioSource instance
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        //we will start capturing the video from the camera
        //params are width,height and fps
        if (videoCapturerAndroid != null) {
            videoCapturerAndroid.startCapture(1024, 720, 30);
        }

        myVideoView.setVisibility(View.VISIBLE);

        //Add the renderer to the video track
        localVideoTrack.addSink(myVideoView);

        //a small method to provide a mirror effect to the SurfaceViewRenderer
        myVideoView.setMirror(true);
        peerVideoView.setMirror(true);

        gotUserMedia = true;
        Log.d("isInitiator","isInitiator" + SignallingClient.getInstance().isInitiator);
        if (SignallingClient.getInstance().isInitiator) {
            onTryToStart();
        }

        btnEnd.setOnClickListener(v -> {
            hangup();
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    /**
     * This method will be called directly by the app when it is the initiator and has got the local media
     * or when the remote peer sends a message through socket that it is ready to transmit AV data
     */

    @Override
    public void onTryToStart() {
        Log.d("onTryToStartMethod","onTryToStartMethod called");
        runOnUiThread(() -> {
            Log.d("isStarted","isStarted" + SignallingClient.getInstance().isStarted);
            Log.d("isChannelReady","isChannelReady" + SignallingClient.getInstance().isChannelReady);
            if (!SignallingClient.getInstance().isStarted && localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                createPeerConnection();
                SignallingClient.getInstance().isStarted = true;
                if (SignallingClient.getInstance().isInitiator) {
                    call();
                }
            }
        });
    }

    /**
     * Creating the local peerconnection instance
     */
    private void createPeerConnection() {
        Log.d("createPeerConnectionMethod","createPeerConnectionMethod called");

        rtcConfig = new PeerConnection.RTCConfiguration(peerIceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        myConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver("localPeerCreation") {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                showToast("Received Remote stream");
                super.onAddStream(mediaStream);
                gotRemoteStream(mediaStream);
            }
        });

        addStreamToLocalPeer();
    }

    /**
     * Adding the stream to the localpeer
     */
    private void addStreamToLocalPeer() {
        //creating local mediastream
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        stream.addTrack(localVideoTrack);
        myConnection.addStream(stream);
    }

    /**
     *
     */
    public void call(){
        Log.d("callMethod","callMethod called");

        //we already have video and audio tracks. Now create peerconnections
        //create sdpConstraints
        sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveAudio", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("offerToReceiveVideo", "true"));

        //creating Offer
        myConnection.createOffer(new CustomSdpObserver("localCreateOffer"){
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                //we have localOffer. Set it as local desc for localpeer and remote desc for remote peer.
                //try to create answer from the remote peer.
                super.onCreateSuccess(sessionDescription);
                myConnection.setLocalDescription(new CustomSdpObserver("localSetLocalDesc"), sessionDescription);
                Log.d("onCreateSuccess", "SignallingClient emit ");
                SignallingClient.getInstance().emitMessage(sessionDescription);
            }
        },sdpConstraints);
    }

    /**
     *
     */
    private void gotRemoteStream(MediaStream stream) {
        Log.d("gotRemoteStreamMethod","gotRemoteStreamMethod called");

        //we have remote video stream. add to the renderer.
        final VideoTrack peerVideoTrack = stream.videoTracks.get(0);
        AudioTrack audioTrack = stream.audioTracks.get(0);
        runOnUiThread(() -> {
            try {
                peerVideoView.setVisibility(View.VISIBLE);
                peerVideoTrack.addSink(peerVideoView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        SignallingClient.getInstance().emitIceCandidate(iceCandidate);
    }

    /**
     *
     */
    @Override
    public void onCreatedRoom() {
        showToast("You created the room " + gotUserMedia);
        if (gotUserMedia) {
            SignallingClient.getInstance().emitMessage("got user media");
        }
    }

    /**
     *
     */
    @Override
    public void onJoinedRoom() {
        showToast("You joined the room " + gotUserMedia);
        if (gotUserMedia) {
            SignallingClient.getInstance().emitMessage("got user media");
        }
    }

    /**
     *
     */
    @Override
    public void onNewPeerJoined() {
        showToast("Remote Peer Joined");

    }

    /**
     *
     */
    @Override
    public void onRemoteHangUp(String msg) {
        showToast("Remote Peer hungup");
        runOnUiThread(this::hangup);
    }

    /**
     *
     */
    @Override
    public void onOfferReceived(JSONObject data) {
        showToast("Received Offer");
        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isInitiator && !SignallingClient.getInstance().isStarted) {
                onTryToStart();
            }

            try {
                myConnection.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp")));
                doAnswer();
                updateVideoViews(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     *
     */
    private void doAnswer() {
        myConnection.createAnswer(new CustomSdpObserver("localCreateAns") {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                myConnection.setLocalDescription(new CustomSdpObserver("localSetLocal"), sessionDescription);
                SignallingClient.getInstance().emitMessage(sessionDescription);
            }
        }, new MediaConstraints());
    }

    /**
     *
     */
    @Override
    public void onAnswerReceived(JSONObject data) {
        showToast("Received Answer");
        try {
            myConnection.setRemoteDescription(new CustomSdpObserver("localSetRemote"), new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
            updateVideoViews(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remote IceCandidate received
     */
    @Override
    public void onIceCandidateReceived(JSONObject data) {
        try {
            myConnection.addIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void updateVideoViews (final boolean remoteVisible){
        runOnUiThread(() -> {
            ViewGroup.LayoutParams params = myVideoView.getLayoutParams();
            if (remoteVisible) {
                params.height = dpToPx(100);
                params.width = dpToPx(100);
            } else {
                params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
            myVideoView.setLayoutParams(params);
        });
    }

    /**
     *
     */
    private VideoCapturer createVideoCapturer() {
        Log.d("createVideoCapturerMethod","createVideoCapturerMethod called");

        VideoCapturer videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        if (videoCapturer == null) {
            //Log.d("opencamera","Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    /**
     *
     */
    private  VideoCapturer createCameraCapturer(CameraEnumerator enumerator){
        Log.d("VideoCapturerMethod","VideoCapturerMethod called");

        final String[] deviceNames = enumerator.getDeviceNames();

        // Trying to find a front facing camera!
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // We were not able to find a front camera. Look for other cameras
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    /**
     *
     */
    public void hangup(){
        Log.d("hangupMethod","hangupMethod called");

        myConnection.close();
        peerConnection.close();
        myConnection = null;
        peerConnection = null;
    }

    /**
     * Util Methods
     */
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(DisplayCamera.this, msg, Toast.LENGTH_SHORT).show());
    }

}
