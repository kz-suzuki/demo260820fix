package com.lotus9492.demowebrtc202009;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.List;

public class DisplayCamera extends AppCompatActivity {

    //VideoView videoView;
    //CameraManager cameraManager;
    Intent intent;
    Button btnEnd;
    //private Object PeerConnectionFactory;
    VideoCapturer videoCapturerAndroid;
    PeerConnectionFactory peerConnectionFactory;
    PeerConnectionFactory.Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_camera);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    50); }

        //Initialize PeerConnectionFactory globals.
        PeerConnectionFactory.initializeAndroidGlobals(this,true);
        //PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true)

        //Create a new PeerConnectionFactory instance.
        options = new PeerConnectionFactory.Options();
        peerConnectionFactory = new PeerConnectionFactory(options);


        //Create a VideoCapturer instance which uses the camera of the device
        videoCapturerAndroid = createVideoCapturer();

        MediaConstraints constraints = new MediaConstraints();

        //Create a VideoSource instance
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid);
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        //create an AudioSource instance
        AudioSource audioSource = peerConnectionFactory.createAudioSource(constraints);
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        //we will start capturing the video from the camera
        //params are width,height and fps
        videoCapturerAndroid.startCapture(1000, 1000, 30);

        //create surface renderer, init it and add the renderer to the track
        SurfaceViewRenderer videoView = (SurfaceViewRenderer)findViewById(R.id.videoView);
        videoView.setVisibility(View.VISIBLE);

        //create an EglBase instance
        EglBase rootEglBase = EglBase.create();

        //init the SurfaceViewRenderer using the eglContext
        videoView.init(rootEglBase.getEglBaseContext(), null);

        //a small method to provide a mirror effect to the SurfaceViewRenderer
        videoView.setMirror(true);

        //Add the renderer to the video track
        localVideoTrack.addRenderer(new VideoRenderer(videoView));

        btnEnd =(Button)findViewById(R.id.btnEnd);
        intent = null;
        btnEnd.setOnClickListener(v -> {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        if (videoCapturer == null) {
            Log.d("opencamera","Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private  VideoCapturer createCameraCapturer(CameraEnumerator enumerator){
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

}
