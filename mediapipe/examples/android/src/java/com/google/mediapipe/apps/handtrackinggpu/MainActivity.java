// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.handtrackinggpu;

import android.os.Bundle;
import android.util.Log;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import android.content.Intent;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Environment;
import android.widget.Toast;
import com.google.mediapipe.components.PermissionHelper;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import java.util.Date;
import java.text.SimpleDateFormat;



/** Main activity of MediaPipe hand tracking app. */
public class MainActivity extends com.google.mediapipe.apps.basic.MainActivity {
  private static final String TAG = "MainActivity";

  private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
  // Max number of hands to detect/process.
  private static final int NUM_HANDS = 1;

  public FileWriter fw;
  public BufferedWriter bw;
  public PrintWriter writer;



  @Override
  protected void onStop() {
    super.onStop();
    try {
      writer.close();
      bw.close();
      fw.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }


  }

  @Override
  protected void onResume() {
    super.onResume();

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
    String formattedDate= formatter.format(date);



    openFile(formattedDate + ".csv");


  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PermissionHelper.checkAndRequestPermissions(this, new String[] {"android.permission.WRITE_EXTERNAL_STORAGE"});


    AndroidPacketCreator packetCreator = processor.getPacketCreator();
    Map<String, Packet> inputSidePackets = new HashMap<>();
    inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
    processor.setInputSidePackets(inputSidePackets);

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
    String formattedDate= formatter.format(date);


    openFile(formattedDate + ".csv");

    // To show verbose logging, run:
    // adb shell setprop log.tag.MainActivity VERBOSE
    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      processor.addPacketCallback(
          OUTPUT_LANDMARKS_STREAM_NAME,
          (packet) -> {
            Log.v(TAG, "Received multi-hand landmarks packet.");
            List<NormalizedLandmarkList> multiHandLandmarks =
                PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

            String s = packet.getTimestamp()
                    + getMultiHandLandmarksDebugStringCSV(multiHandLandmarks);
            Log.v(
                TAG,
                s);
            writer.println(s);
          });
    }
  }

  private void openFile(String filename) {

    File dir = new File("/storage/emulated/0/Documents/", "mydir");
    if(!dir.exists()){
      dir.mkdir();
    }

    try {
      File gpxfile = new File(dir, filename);
      fw = new FileWriter(gpxfile, true);
      bw = new BufferedWriter(fw);
      writer = new PrintWriter(bw);


    } catch (Exception e){
      e.printStackTrace();
    }


  }

  private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
    if (multiHandLandmarks.isEmpty()) {
      return "No hand landmarks";
    }
    String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {
      multiHandLandmarksStr +=
          "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
        multiHandLandmarksStr +=
            "\t\tLandmark ["
                + landmarkIndex
                + "]: ("
                + landmark.getX()
                + ", "
                + landmark.getY()
                + ", "
                + landmark.getZ()
                + ")\n";
        ++landmarkIndex;
      }
      ++handIndex;
    }
    return multiHandLandmarksStr;
  }

  private String getMultiHandLandmarksDebugStringCSV(List<NormalizedLandmarkList> multiHandLandmarks) {
    if (multiHandLandmarks.isEmpty()) {
      return "No hand landmarks";
    }
    String multiHandLandmarksStr = "";
    int handIndex = 0;
    for (NormalizedLandmarkList landmarks : multiHandLandmarks) {

      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
        multiHandLandmarksStr += "," +
                        landmark.getX()
                        + ","
                        + landmark.getY()
                        + ","
                        + landmark.getZ()
                        ;
        ++landmarkIndex;
      }
      ++handIndex;
    }

    return multiHandLandmarksStr;
  }
}
