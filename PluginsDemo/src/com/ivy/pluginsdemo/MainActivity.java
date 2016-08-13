package com.ivy.pluginsdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {

	private static final String WIFI_TAG = "drawableanimationdemo";
	private static final String ANIM_TAG = "anim_wifi";
	protected ImageView ivWifi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ivWifi = (ImageView) findViewById(R.id.imageView1);

		this.findViewById(R.id.button1).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						 plugins();
						//anim();
					}
				});

	}

	protected void anim() {
		ivWifi.setBackgroundResource(R.anim.bullet_anim);
		handleAnim();
		
	}

	protected void plugins() {

		Log.v("plugins demo", "click...");

		String fileName = WIFI_TAG + ".apk";
		String filePath = this.getCacheDir() + File.separator + fileName;
		String packName = "com.ivy.drawableanimationdemo";

		File f = new File(filePath);
		if (f.exists()) {
			Drawable drawable = ivWifi.getBackground();
			if (drawable instanceof AnimationDrawable) {
				Log.v("plugins demo",
						"drawable instanceof AnimationDrawable...");
				handleAnim();
			} else {
				// excute plugin
				Log.v("plugins demo", "excute plugin...");
				AssetManager assets = PluginResources.getPluginAssertManager(f);

				
				
				PluginResources resources = PluginResources.getPlunginResource(getResources(), assets);

				// 也可以这么调用
				// PluginResources resources = new PluginResources(assets,getResources().getDisplayMetrics(), getResources().getConfiguration());

				// 反射R文件

				try {
					DexClassLoader classLoader = new DexClassLoader(
							f.getAbsolutePath(), this.getDir(fileName,
									Context.MODE_PRIVATE).getAbsolutePath(),
							null, getClassLoader());

					Class loadClass = classLoader.loadClass(packName
							+ ".R$anim");

					Field[] fields = loadClass.getDeclaredFields();

					for (Field field : fields) {
						if (field.getName().equals(ANIM_TAG)) {

							int animId = field.getInt(R.anim.class);// 这个要求本地要要有anim资源

							Drawable drawable2 = resources.getDrawable(animId);

							Log.v("plugins demo", "animId..." + animId);

							 ivWifi.setBackgroundDrawable(drawable2);

							 // 下面这句也可以
//							ivWifi.setBackground(drawable2);

							handleAnim();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} else {
			// download
			try {
				InputStream is = this.getAssets().open(fileName);
				FileOutputStream fos = new FileOutputStream(new File(
						getCacheDir(), fileName));

				int len = 0;
				byte[] buffer = new byte[1024];
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				is.close();

				Toast.makeText(this, "finish download.", 0).show();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private void handleAnim() {

		AnimationDrawable anim = (AnimationDrawable) ivWifi.getBackground();

		Log.v("plugins demo", "anim!=null " + (anim != null));

		if (anim != null) {
			Log.v("plugins demo", "anim.isRunning() " + (anim.isRunning()));
			if (anim.isRunning()) {
				anim.stop();
			} else {
				anim.stop();
				anim.start();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
