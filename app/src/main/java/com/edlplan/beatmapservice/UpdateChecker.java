package com.edlplan.beatmapservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateChecker {

    public static void startCheckUpdate(Activity activity) {
        Util.asynLoadString(
                BuildConfig.DEBUG ? "https://raw.githubusercontent.com/EdrowsLuo/BeatmapService/master/release.json" :
                        "https://raw.githubusercontent.com/EdrowsLuo/BeatmapService/release/release.json",
                s -> {
                    try {
                        JSONObject obj = new JSONObject(s);
                        System.out.println(obj.toString(2));
                        obj = obj.getJSONObject("latest_release");
                        if (obj.getInt("code") > BuildConfig.VERSION_CODE) {
                            Util.toast(activity, "发现新版本 " + obj.getString("name") + " ,尝试更新");
                        } else {
                            return;
                        }

                        File cache = new File(activity.getCacheDir(), "updates/" + obj.getString("name") + ".apk");
                        if (cache.exists()) {
                            //文件已经存在，计算MD5比对
                            if (Util.md5(cache).equals(obj.getString("md5"))) {
                                //比对成功，直接安装
                                install(activity, cache);
                                return;
                            }
                        }

                        URL url = new URL(obj.getString("suggested_url"));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(10000);
                        byte[] buf = new byte[1024 * 10];
                        int l = 0;
                        int total = 0;
                        InputStream in = connection.getInputStream();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        while ((l = in.read(buf)) != -1) {
                            out.write(buf, 0, l);
                            total += l;
                            System.out.println(total);
                        }
                        System.out.println("update done");
                        byte[] bytes = out.toByteArray();
                        Util.checkFile(cache);

                        OutputStream outputStream = new FileOutputStream(cache);
                        outputStream.write(bytes);
                        outputStream.close();
                        System.out.println("write file done");

                        if (Util.md5(cache).equals(obj.getString("md5"))) {
                            //比对成功，安装
                            System.out.println("md5 check done");
                            install(activity, cache);
                            return;
                        } else {
                            Util.toast(activity, "更新apk包MD5校验失败!");
                            System.out.println("md5 check failed " + Util.md5(cache));
                            cache.delete();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Util.toast(activity, "更新失败");
                    }
                },
                null
        );
    }

    public static void install(Activity activity, File apk) {
        installApp(activity, apk);
    }

    private static void installApp(Context pContext, File pFile) {
        if (null == pFile)
            return;
        if (!pFile.exists())
            return;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(pContext.getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", pFile);
        } else {
            uri = Uri.fromFile(pFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        pContext.startActivity(intent);
    }
}
