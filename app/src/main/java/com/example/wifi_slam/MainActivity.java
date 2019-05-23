package com.example.wifi_slam;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.*;
import android.hardware.*;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.BaseArFragment.OnTapArPlaneListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;


public final class MainActivity extends AppCompatActivity {
    private ArrayList dataArray;
    private ArrayList lineNodeArray;
    private ArrayList sphereNodeArray;
    private ArrayList startNodeArray;
    private ArrayList endNodeArray;
    private Timer timer;
    private Handler handler;
    private AnchorNode startNode;
    private TimerTask task;
    private WifiManager wm;
    private SensorManager sm;
    private WifiInfo wifiInfo;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver wifiChangeReceiver;
    private List<ScanResult> scanResults;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_main);
        LitePal.initialize(this);
        this.initView();
    }

    private final float distance() {
        Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
        ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
        Scene scene = arSceneView.getScene();
        Camera camera = scene.getCamera();
        float dx = camera.getWorldPosition().x - Point.cameraX;
        float dz = camera.getWorldPosition().y - Point.cameraZ;
        float dy = camera.getWorldPosition().z - Point.cameraY;
        return (float) Math.sqrt(dx * (float) 100 * dx * (float) 100 + dy * (float) 100 * dy * (float) 100 + dz * (float) 100 * dz * (float) 100);
    }

    private final void display() {
        Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
        ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
        Scene scene = arSceneView.getScene();
        Camera camera = scene.getCamera();
        Point.cameraX = camera.getWorldPosition().x;
        Point.cameraZ = camera.getWorldPosition().y;
        Point.cameraY = camera.getWorldPosition().z;
        //  Toast.makeText(this, ("x:" + (int) (Point.cameraX * (float) 100) + " y:" + (int) (Point.cameraY * (float) 100) + " z:" + (int) (Point.cameraZ * (float) 100)), Toast.LENGTH_LONG).show();
        //wm.startScan();

        /*Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
        ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
        Scene scene = arSceneView.getScene();
        Camera camera = scene.getCamera();
        Point.cameraX = camera.getWorldPosition().x;
        Point.cameraZ = camera.getWorldPosition().y;
        Point.cameraY = camera.getWorldPosition().z;
        WiFi_info wiFi_info = new WiFi_info();
        wiFi_info.setX(Point.cameraX);
        wiFi_info.setY(Point.cameraY);
        wiFi_info.setZ(Point.cameraZ);
        if (wiFi_info.save() == false)
            Toast.makeText(MainActivity.this, "WiFi保存出错", Toast.LENGTH_LONG).show();
        List<RSSI> rssi_list=new ArrayList<RSSI>();
        RSSI rssi;
        //String s="";
            for (int i = 0; i < 5; i++) {
                wifiInfo = wm.getConnectionInfo();
                try {
                    Thread.sleep(10);
                } catch(InterruptedException e) {
                    System.out.println("got interrupted!");
                }
                rssi = new RSSI();
                rssi.setSSID(wifiInfo.getSSID());
                rssi.setLevel(wifiInfo.getRssi());
                rssi.setWiFi_info(wiFi_info);
                rssi_list.add(rssi);
               // s=s+rssi.getLevel()+"  ";
            }
        rssi=rssi_max(rssi_list);
        rssi.save();
        Toast.makeText(MainActivity.this, ("x:" + (int) (Point.cameraX * (float) 100) + " y:" + (int) (Point.cameraY * (float) 100) + " z:" + (int) (Point.cameraZ * (float) 100) + "\n"+rssi.getSSID()+" "+rssi.getLevel()), Toast.LENGTH_LONG).show();
        //LitePal.saveAll(wiFi_info.getScanResults());
        */
    }

    private void initView() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        dataArray = new ArrayList<AnchorInfoBean>();
        lineNodeArray = new ArrayList<Node>();
        sphereNodeArray = new ArrayList<Node>();
        startNodeArray = new ArrayList<Node>();
        endNodeArray = new ArrayList<Node>();
        startNode = new AnchorNode();
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wm.getConnectionInfo();
        timer = new Timer();
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (MainActivity.this.distance() >= (float) 15) {
                            MainActivity.this.display();
                        }
                        break;
                    case 1:
                        if (MainActivity.this.distance() >= (float) 15) {
                            MainActivity.this.display();
                        }
                        break;
                    default:
                        if (MainActivity.this.distance() >= (float) 15) {
                            MainActivity.this.display();
                        }
                }

            }
        };
        this.task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                MainActivity.this.handler.sendMessage(message);
            }
        };

        wifiChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//这个监听wifi的打开与关闭，与wifi的连接无关
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLED://WIFI已关闭
                            if (wm.setWifiEnabled(true) == false)
                                Toast.makeText(MainActivity.this, "WiFi开启出错", Toast.LENGTH_LONG).show();
                            break;
                        case WifiManager.WIFI_STATE_DISABLING://WIFI正在关闭中
                            break;
                        case WifiManager.WIFI_STATE_ENABLED://WIFI已启用
                            break;
                        case WifiManager.WIFI_STATE_ENABLING://WIFI正在启动中
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN://未知WIFI状态
                            break;
                    }
                }


            }
        };
        registerReceiver(wifiChangeReceiver, intentFilter);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    scanResults = wm.getScanResults();
                    StringBuilder tmp = new StringBuilder();
                    if (scanResults != null) {
                        for (ScanResult sr : scanResults) {
                            if (sr.SSID.equals("Test Ap-1") || sr.SSID.equals("Test Ap-2") || sr.SSID.equals("Test Ap-3") || sr.SSID.equals("Test Ap-4"))
                                tmp.append(sr.SSID + "  " + String.valueOf(sr.level) + " \n");
                        }
                    }
                    Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
                    ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
                    Scene scene = arSceneView.getScene();
                    Camera camera = scene.getCamera();
                    Point.cameraX = camera.getWorldPosition().x;
                    Point.cameraZ = camera.getWorldPosition().y;
                    Point.cameraY = camera.getWorldPosition().z;
                    Toast.makeText(MainActivity.this, ("x:" + (int) (Point.cameraX * (float) 100) + " y:" + (int) (Point.cameraY * (float) 100) + " z:" + (int) (Point.cameraZ * (float) 100) + "\n" + tmp), Toast.LENGTH_LONG).show();
                    WiFi_info wiFi_info = new WiFi_info();
                    wiFi_info.setX(Point.cameraX);
                    wiFi_info.setY(Point.cameraY);
                    wiFi_info.setZ(Point.cameraZ);
                    if (wiFi_info.save() == false)
                        Toast.makeText(MainActivity.this, "WiFi保存出错", Toast.LENGTH_LONG).show();
                    RSSI rssi;
                    for (ScanResult sr : scanResults) {
                        rssi = new RSSI();
                        rssi.setSSID(sr.SSID);
                        rssi.setLevel(sr.level);
                        rssi.setWiFi_info(wiFi_info);
                        rssi.save();
                    }
                    //LitePal.saveAll(wiFi_info.getScanResults());
                }
                if (wm.setWifiEnabled(false) == false)
                    Toast.makeText(MainActivity.this, "WiFi关闭出错", Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);

        String[] PERMS_INITIAL = {Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(PERMS_INITIAL, 127);
        ImageView UI_Last = findViewById(R.id.UI_Last);
        ImageView UI_Start = findViewById(R.id.UI_Start);
        UI_Last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<WiFi_info> list = LitePal.findAll(WiFi_info.class);
                long id;
                //Toast.makeText(MainActivity.this,list.size()+"",Toast.LENGTH_LONG).show();
                StringBuilder tmp = new StringBuilder();
                Point point1 = new Point(((AnchorInfoBean) dataArray.get(0)).getAnchor().getPose().tx(), ((AnchorInfoBean) dataArray.get(0)).getAnchor().getPose().tz(), ((AnchorInfoBean) dataArray.get(0)).getAnchor().getPose().ty());
                Point point2 = new Point(((AnchorInfoBean) dataArray.get(1)).getAnchor().getPose().tx(), ((AnchorInfoBean) dataArray.get(1)).getAnchor().getPose().tz(), ((AnchorInfoBean) dataArray.get(1)).getAnchor().getPose().ty());
                Point point3 = new Point(((AnchorInfoBean) dataArray.get(2)).getAnchor().getPose().tx(), ((AnchorInfoBean) dataArray.get(2)).getAnchor().getPose().tz(), ((AnchorInfoBean) dataArray.get(2)).getAnchor().getPose().ty());
                Point point4 = new Point(((AnchorInfoBean) dataArray.get(3)).getAnchor().getPose().tx(), ((AnchorInfoBean) dataArray.get(3)).getAnchor().getPose().tz(), ((AnchorInfoBean) dataArray.get(3)).getAnchor().getPose().ty());
                String str = "";
                str = str + "x1:" + point1.getX() + " y1:" + point1.getY() + " z1:" + point1.getZ() + "\n";
                str = str + "x2:" + point2.getX() + " y2:" + point2.getY() + " z2:" + point2.getZ() + "\n";
                str = str + "x3:" + point3.getX() + " y3:" + point3.getY() + " z3:" + point3.getZ() + "\n";
                str = str + "x4:" + point4.getX() + " y4:" + point4.getY() + " z4:" + point4.getZ() + "\n" + "\n";
                for (WiFi_info wiFi_info : list) {
                    id = wiFi_info.getId();
                    str = str + "x:" + wiFi_info.getX() + " y:" + wiFi_info.getY() + " z:" + wiFi_info.getZ() + "\n";
                    tmp.append("x:" + (int) (wiFi_info.getX() * 100) + " y:" + (int) (wiFi_info.getY() * 100) + " z:" + (int) (wiFi_info.getZ() * 100) + "\n");
                    List<RSSI> scanResults = LitePal.where("wifi_info_id = ?", id + "").find(RSSI.class);
                    //
                    // str=str+scanResults.size()+"\n";
                    //char c='"';
                    if (scanResults.size() == 0)
                        Toast.makeText(MainActivity.this, "数据提取有误", Toast.LENGTH_SHORT).show();
                    for (RSSI rssi : scanResults) {
                        if (rssi.getSSID().equals("Test Ap-1") || rssi.getSSID().equals("Test Ap-2") || rssi.getSSID().equals("Test Ap-3") || rssi.getSSID().equals("Test Ap-4")) {
                            //if (rssi.getSSID().equals(c+"Test Ap-1"+c) || rssi.getSSID().equals(c+"Test Ap-2"+c) || rssi.getSSID().equals(c+"Test Ap-3"+c) || rssi.getSSID().equals(c+"Test Ap-4"+c)) {
                            tmp.append(rssi.getSSID() + "  " + (rssi.getLevel()) + " \n");
                            str = str + rssi.getSSID() + "  " + rssi.getLevel() + "\n";
                        }
                    }
                }
                //Toast.makeText(MainActivity.this, tmp, Toast.LENGTH_LONG).show();
                writeTxt(str);

                /*switch (dataArray.size()) {
                    case 0:
                        ToastUtils.showLong("没有操作记录");
                        break;
                    case 1: {
                        MainActivity.this.dataArray.clear();
                        MainActivity.this.lineNodeArray.clear();
                        MainActivity.this.sphereNodeArray.clear();
                        MainActivity.this.startNodeArray.clear();
                        MainActivity.this.endNodeArray.clear();
                        Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
                        ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
                        Scene scene = arSceneView.getScene();
                        scene.removeChild(startNode);
                    }
                    break;
                    default:
                        MainActivity.this.dataArray.remove(MainActivity.this.dataArray.size() - 1);
                        int index = MainActivity.this.startNodeArray.size() - 1;
                        ((Node) MainActivity.this.startNodeArray.get(index)).removeChild((Node) MainActivity.this.lineNodeArray.remove(index));
                        ((Node) MainActivity.this.endNodeArray.get(index)).removeChild((Node) MainActivity.this.sphereNodeArray.remove(index + 1));
                        Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
                        ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
                        Scene scene = arSceneView.getScene();
                        scene.removeChild((Node) MainActivity.this.startNodeArray.remove(index));
                        scene.removeChild((Node) MainActivity.this.endNodeArray.remove(index));
                }*/
            }
        });
        UI_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.startScan();
                //MainActivity.this.display();
                //MainActivity.this.timer.schedule((TimerTask) MainActivity.this.task, 0L, 10L);
            }
        });
        this.initAr();
    }

    private final void initAr() {
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
        ((MyArFragment) fragment).setOnTapArPlaneListener((OnTapArPlaneListener) (new OnTapArPlaneListener() {
            public final void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorInfoBean anchorInfoBean = new AnchorInfoBean("", anchor, 0.0);
                MainActivity.this.dataArray.add(anchorInfoBean);
                Toast.makeText(MainActivity.this, "x:" + String.format("%.1f", ((AnchorInfoBean) dataArray.get(dataArray.size() - 1)).getAnchor().getPose().tx() * 100) + " y:" + String.format("%.1f", ((AnchorInfoBean) dataArray.get(dataArray.size() - 1)).getAnchor().getPose().tz() * 100) + " z:" + String.format("%.1f", ((AnchorInfoBean) dataArray.get(dataArray.size() - 1)).getAnchor().getPose().ty() * 100), Toast.LENGTH_LONG).show();
                if (MainActivity.this.dataArray.size() > 1) {
                    Anchor endAnchor = ((AnchorInfoBean) MainActivity.this.dataArray.get(MainActivity.this.dataArray.size() - 1)).getAnchor();
                    Anchor startAnchor = ((AnchorInfoBean) MainActivity.this.dataArray.get(MainActivity.this.dataArray.size() - 2)).getAnchor();
                    Pose startPose = endAnchor.getPose();
                    Pose endPose = startAnchor.getPose();
                    float dx = startPose.tx() - endPose.tx();
                    float dy = startPose.ty() - endPose.ty();
                    float dz = startPose.tz() - endPose.tz();
                    anchorInfoBean.setLength(Math.sqrt((double) (dx * dx + dy * dy + dz * dz)));
                    MainActivity.this.drawLine(startAnchor, endAnchor, anchorInfoBean.getLength());
                } else {
                    MainActivity.this.startNode = new AnchorNode(hitResult.createAnchor());
                    Fragment fragment = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
                    ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
                    startNode.setParent((NodeParent) arSceneView.getScene());
                    MaterialFactory.makeOpaqueWithColor((Context) MainActivity.this, new Color(0.33F, 0.87F, 0.0F)).thenAccept(new Consumer() {
                        // $FF: synthetic method
                        // $FF: bridge method
                        public void accept(Object var1) {
                            this.accept((Material) var1);
                        }

                        public final void accept(Material material) {
                            ModelRenderable sphere = ShapeFactory.makeSphere(0.02F, Vector3.zero(), material);
                            Node node = new Node();
                            node.setParent(startNode);
                            node.setLocalPosition(Vector3.zero());
                            node.setRenderable(sphere);
                            sphereNodeArray.add(node);
                        }
                    });
                }
            }
        }));
    }

    private final void drawLine(Anchor firstAnchor, Anchor secondAnchor, final double length) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final AnchorNode firstAnchorNode = new AnchorNode(firstAnchor);
            this.startNodeArray.add(firstAnchorNode);
            final AnchorNode secondAnchorNode = new AnchorNode(secondAnchor);
            this.endNodeArray.add(secondAnchorNode);
            Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
            ArSceneView arSceneView = ((MyArFragment) fragment).getArSceneView();
            firstAnchorNode.setParent((NodeParent) arSceneView.getScene());
            secondAnchorNode.setParent((NodeParent) arSceneView.getScene());
            MaterialFactory.makeOpaqueWithColor((Context) this, new Color(0.53F, 0.92F, 0.0F)).thenAccept((Consumer) (new Consumer() {
                // $FF: synthetic method
                // $FF: bridge method
                public void accept(Object var1) {
                    this.accept((Material) var1);
                }

                public final void accept(Material material) {
                    ModelRenderable sphere = ShapeFactory.makeSphere(0.02F, new Vector3(0.0F, 0.0F, 0.0F), material);
                    Node node = new Node();
                    node.setParent(secondAnchorNode);
                    node.setLocalPosition(Vector3.zero());
                    node.setRenderable(sphere);
                    sphereNodeArray.add(node);
                }
            }));
            final Vector3 firstWorldPosition = firstAnchorNode.getWorldPosition();
            final Vector3 secondWorldPosition = secondAnchorNode.getWorldPosition();
            final Vector3 difference = Vector3.subtract(firstWorldPosition, secondWorldPosition);
            Vector3 directionFromTopToBottom = difference.normalized();
            final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
            MaterialFactory.makeOpaqueWithColor((Context) this, new Color(0.33F, 0.87F, 0.0F)).thenAccept((Consumer) (new Consumer() {
                // $FF: synthetic method
                // $FF: bridge method
                public void accept(Object var1) {
                    this.accept((Material) var1);
                }

                public final void accept(Material material) {
                    ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.01F, 0.01F, difference.length()), Vector3.zero(), material);
                    Node node = new Node();
                    node.setParent((NodeParent) firstAnchorNode);
                    node.setRenderable((Renderable) lineMode);
                    node.setWorldPosition(Vector3.add(firstWorldPosition, secondWorldPosition).scaled(0.5F));
                    node.setWorldRotation(rotationFromAToB);
                    lineNodeArray.add(node);
                    ViewRenderable.builder().setView((Context) MainActivity.this, R.layout.renderable_text).build().thenAccept((Consumer) (new Consumer() {
                        // $FF: synthetic method
                        // $FF: bridge method
                        public void accept(Object var1) {
                            this.accept((ViewRenderable) var1);
                        }

                        public final void accept(ViewRenderable it) {
                            View view = it.getView();
                            TextView textView = (TextView) view;
                            StringBuilder stringBuilder = new StringBuilder();
                            String string = String.format("%.1f", length * 100);
                            textView.setText(string + "CM");
                            it.setShadowCaster(false);
                            FaceToCameraNode faceToCameraNode = new FaceToCameraNode();
                            faceToCameraNode.setParent((NodeParent) node);
                            faceToCameraNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0F, 1.0F, 0.0F), 90.0F));
                            faceToCameraNode.setLocalPosition(new Vector3(0.0F, 0.02F, 0.0F));
                            faceToCameraNode.setRenderable((Renderable) it);
                        }
                    }));
                }
            }));
        }
    }

    public void writeTxt(String str) {
        //新建文件夹
        String folderName = "YZW";
        File sdCardDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), folderName);
        if (sdCardDir == null)
            Toast.makeText(MainActivity.this, "创建失败", Toast.LENGTH_SHORT).show();
        if (!sdCardDir.exists()) {
            if (!sdCardDir.mkdirs()) {

                try {
                    sdCardDir.createNewFile();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "创建失败", Toast.LENGTH_SHORT).show();
                }
            }
        }


        try {
            //新建文件
            File saveFile = new File(sdCardDir, "test2.txt");

            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            // FileOutputStream outStream =null;
            //outStream = new FileOutputStream(saveFile);

            final FileOutputStream outStream = new FileOutputStream(saveFile);

            try {
                outStream.write(str.getBytes());
                outStream.close();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "写入失败", Toast.LENGTH_SHORT).show();
            }
            //outStream.write("测试写入文件".getBytes());
            //outStream.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
        }
    }

    private RSSI rssi_max(List<RSSI> rssiList) {
        if (!rssiList.isEmpty()) {
            RSSI rssi = rssiList.get(0);
            for (RSSI r : rssiList) {
                if (rssi.getLevel() < r.getLevel())
                    rssi = r;
            }
            return rssi;
        } else {
            Toast.makeText(MainActivity.this, "WiFi信号为空", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    protected void onDestroy() {
        super.onDestroy();
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.UI_ArSceneView);
        ((MyArFragment) fragment).onDestroy();
    }
}




