package com.example.vedioorrecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 处理音频文件MediaPlayer 和AudioTrack
 *
 * @author zmz
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG ="RecordAndMedia" ;
    private MediaRecorder mMediaRecorder;
private AudioTrack mAudioTrack;
private  MediaPlayer mMediaPlayer;
    private static String mFileName = null;
    private String fileurl = null;
    private boolean mRecord=false;
    private boolean mplayRecord=false;

    private AudioRecord audioRecord;// 录音对象
    private int frequence = 8000;// 采样率 8000
    private int channelInConfig = AudioFormat.CHANNEL_IN_MONO;// 定义采样通道
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// 定义音频编码（16位）
    private byte[] buffer = null;// 录制的缓冲数组
private int mbufferSize=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        fileurl = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/suraly";
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/suraly";
        mFileName += "/audiorecordtest.wav";

        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        Button btn4 = findViewById(R.id.btn4);
    btn1.setOnClickListener(this);
    btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn1:
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    File file=new File(fileurl);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    mRecord=!mRecord;
                    if(mRecord){
                        startMediaRecorderRecording();
                    }else{
                        stopMediaRecorderRecording();
                    }
                }
                break;
                case R.id.btn2:
                    mplayRecord=!mplayRecord;
                    if(mplayRecord){
                    startMediaPlayRecording();
                    }else{
                        stopMediaPlayRecording();
                    }
                    break;
            case R.id.btn3:
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(fileurl);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    mplayRecord = !mplayRecord;
                    if (mplayRecord) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                startAudioRecordRecording();
                            }
                        }).start();

                    } else {
                        stopAudioRecordRecording();
                    }

                }
                break;
            case R.id.btn4:
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        mplayRecord = !mplayRecord;
                        if (mplayRecord){
                        startAudioTrackRecording();
                        }else{
                            stopAudioTrackRecording();
                        }
                    }
                }).start();

                    default:
                        break;
        }
    }

    /**
     * 停止AudioRecording进行录音.
     */
    private void stopAudioRecordRecording() {
        Log.d(TAG,"停止录音");
        if(audioRecord != null){
            audioRecord.stop();
            audioRecord.release();
            audioRecord=null;
        }
    }

    /**
     * 使用AudioRecording进行录音
     */
    private void startAudioRecordRecording() {
        Log.d(TAG,"开始录音");
        mbufferSize=AudioRecord.getMinBufferSize(frequence,channelInConfig,audioEncoding)*4;
        audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,frequence,channelInConfig,audioEncoding,mbufferSize);
        SimpleDateFormat msimpleDateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date=new Date(System.currentTimeMillis());
        String time=msimpleDateFormat.format(date);
        FileOutputStream mfileOutputStream=null;
        try {
          //  File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()
            //        + "/suraly/audiorecord"+time);
            File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/suraly/test.pcm");
            if(!file.exists()){
                file.createNewFile();
                Log.d(TAG,"创建成功");
            }
            mfileOutputStream=new FileOutputStream(file);
            audioRecord.startRecording();
            byte[]byteBuffer=new byte[mbufferSize];
            while(mplayRecord){
                int end=audioRecord.read(byteBuffer,0,byteBuffer.length);
                mfileOutputStream.write(byteBuffer,0,end);
                mfileOutputStream.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (mfileOutputStream!=null){
                try {
                    mfileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 停止使用MediaPlayer播放录音
     */
    private void stopMediaPlayRecording() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer=null;
    }

    /**
     * 使用Mediaplayer播放录音文件
     */
    private void startMediaPlayRecording() {
        mMediaPlayer=new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mFileName);
           // File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                //    + "/suraly/test.pcm");
           // Log.d(TAG,"filename="+file.getPath().toString());
           // mMediaPlayer.setDataSource(file.getPath().toString());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 停止AudioTrack播放
     */
    private void stopAudioTrackRecording(){
        if (mAudioTrack != null) {
            Log.d(TAG, "Stopping");
            mAudioTrack.stop();
            Log.d(TAG, "Releasing");
            mAudioTrack.release();
            Log.d(TAG, "Nulling");
            mAudioTrack=null;
        }
    }

    /**
     * 使用AudioTrack进行播放
     */
    private void startAudioTrackRecording() {

        FileInputStream fileInputStream = null;
        mbufferSize = AudioTrack.getMinBufferSize(frequence, AudioFormat.CHANNEL_OUT_MONO,
               audioEncoding)*4;
// 实例AudioTrack
        mAudioTrack = new AudioTrack(3, frequence,
                AudioFormat.CHANNEL_OUT_MONO, audioEncoding, mbufferSize,
                AudioTrack.MODE_STREAM);
        File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/suraly/audiorecordtest.wav");
        if (file.exists()){
            try {
                Log.d(TAG, "播放录音");
                mAudioTrack.play();
                 fileInputStream=new FileInputStream(file);
                byte[] mbuffer=new byte[mbufferSize];
                int readCount=fileInputStream.read(mbuffer);
                while(mplayRecord){
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                            readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {
                        mAudioTrack.write(mbuffer, 0, readCount);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (fileInputStream != null){
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 停止使用MediaRecorder录音
     */
    private void stopMediaRecorderRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder=null;
    }


    /**
     * 使用MediaRecorder 录音
     */
    private void startMediaRecorderRecording() {
        mMediaRecorder=new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(mFileName);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //实时录音

    /**
     * 动态申请权限读写权限,以及录音权限
     *
     */
    private void checkPermission() {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 200);
                return;
            }
        }
    }

}