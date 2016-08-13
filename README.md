# PluginsDemo
插件化编程

需求：程序B需要调用插件A里面的资源文件、XML、Java代码等。


----

A.建立一个Demo：DrawableAnimationDemo，用来表示远程服务器端的apk。包名：com.ivy.drawableanimationdemo


效果如图所示

![](http://2)

B. 新建另外一个项目：PluginsDemo，用来演示调用插件

其中布局文件可以这样写：

    <ImageView
        android:id="@+id/imageView1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="156dp"
        android:src="@drawable/icon_1" />


C. 理下思路

PluginsDemo要想调用DrawableAnimationDemo的资源文件。

---思路---

* 实现远程服务端动画
	* 判断远程apk有没有下载到sdcard里面
	* excute plugin



* android 程序
* this.getResources().getString(R.string.app_name)
* this-->context


* apk 安装在手机的哪个地方？
	* data/app/package-1/base.apk
* 通过系统签名变成系统程序

* Context pluginContext = this.createPackageContext("com.cn.cloud",flag)
	* 这种方式只能拿到安装的APK。所以这种方式不可以。

- this.getResources();
	- ContextWrapper
		- mBase.getRersources()
	- Context
		- abstract getResources()



- 系统源码
	- ContextImpl.java
		- getResources(); mResources
		- mResourcesManager = ResourcesManager.get
		- Resources resources = packageInfo.getResource(mainThread)
	- LoadedApk
		- 单例
		- mainThread.getTopLevelResources(mResDir,...)
	- ActivityThread
		- 
	

- PluginResources
	- 专门加载其他的
	- extends Resources
	- 自定义加载插件apk的AssertManager


getPluginAssertManager

	public static AssertManager getPluginAssertManager(File apk){
	
		//AssertManager {hide},需要用到反射
		
		Class cls = Class.forName("android.content.res.AssertManager")
		
		// addAssertPatj(String path) {hide} 
	
		Method[] methods = cls.getDeclaredMethods();
	
		for(Method method:methods){
			if(method.getName().equeals("addAssetPath")){
				method.invoke(assetManage,apk.getAbsoutePath());
				return assetManager;
			}
		}
	
	
	}


	public static PluginResources getPlunginResource(Resources r,AssetManger asset){
		
		PluginRescours pr = new PluginRescours(asset,r.getDisplayMe,r.getConfig);
		return pr;
	}


// excute plugin


	AssetManager am = PluginResources.getPluginAssertManager(apkFile);
	
	PluginResources pr = PluginResources.getPlunginResource(getResource(),am);

// 反射R文件

	
	DexClassLoader classLoader = new DexClassLoader(apkFile.getAbs(),this.getDir(fileName,Context.MODE_PRIVATE).getAbs(),null,getClassLoader());
	
	Class<?> loadClass = classLoader.loadClass(packageName+".R$anim");
	
	Field[] fields = loadClass.getDeclaredFields();
	
	for(Field field:fields){
		if(field.getName().equals(CLOUD_TAG)){
			int animId = field.getInt(R.anim.class);
			Drawable drawable = pr.getDrawable(animId);
	
			((ImageView)v).setBackgroudDrawable(drawable);
			handlerAnim(v);
		}
	}

---思路END---


核心代码：

PluginResources：

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


MainActivity:

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