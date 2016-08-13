package com.ivy.pluginsdemo;

import java.io.File;
import java.lang.reflect.Method;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

public class PluginResources extends Resources {

	public PluginResources(AssetManager assets, DisplayMetrics metrics,
			Configuration config) {
		super(assets, metrics, config);
	}

	public static AssetManager getPluginAssertManager(File apk) {

		try {
			// AssertManager {hide},需要用到反射
			Class cls = Class.forName("android.content.res.AssetManager");

			// addAssertPatj(String path) {hide}
			Method[] methods = cls.getDeclaredMethods();

			for (Method method : methods) {
				if (method.getName().equals("addAssetPath")) {
					Log.v("PluginResources", method.getName());
					
					AssetManager assetManager = AssetManager.class
							.newInstance();
					method.invoke(assetManager, apk.getAbsolutePath());
					return assetManager;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static PluginResources getPlunginResource(Resources r,
			AssetManager asset) {
		

		PluginResources pr = new PluginResources(asset, r.getDisplayMetrics(),
				r.getConfiguration());
		return pr;
	}

}
