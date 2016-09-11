package com.cantalou.manager.soloader;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import com.cantalou.android.util.Log;
import com.cantalou.android.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cantalou
 * @date 2016年08月29日 13:58
 */
public class SoLoaderManager {
	
	public static final DOWNLOAD_SUCCESS = 0;
	
	public static final DOWNLOAD_ERROR = 1;

	private static class Holder {
		static final SoLoaderManager INSTANCE = new SoLoaderManager();
	}

	public static SoLoaderManager getInstance() {
		return Holder.INSTANCE;
	}

	private String matchAbi = null;

	private String[] supportedAbis = null;

	private HashMap<String, Object> loadedSo = new HashMap<String, Object>();
	
	private HashMap<String, Object> downloadedSo = new HashMap<String, Object>();

	private ArrayList<RequestListener> requestListeners = new ArrayList<RequestListener>();

	private ExecutorService es = Executors.newCachedThreadPool();

	private Handler handler;

	private SoLoaderManager() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			supportedAbis = Build.SUPPORTED_ABIS;
		}

		if (supportedAbis == null || supportedAbis.length == 0) {
			supportedAbis = new String[] { Build.CPU_ABI, Build.CPU_ABI2 };
		}
		for (String abi : supportedAbis) {
			if (abi.contains("64")) {
				continue;
			}
			matchAbi = abi;
			break;
		}
		
		handler = new Handler(new HandlerThread("soLoadThread").getLooper());
	}

	/**
	 * Checking that all so file were loaded
	 *
	 * @param builder
	 * @return true if all loaded  
	 */
	public boolean isAllLoaded(Request builder) {

		List<DownloadItem> items = builder.getDownloadItems();
		if (items.isEmpty()) {
			items = createDownloadItem(builder);
			builder.setDownloadItems(items);
		}

		for (DownloadItem item : items) {
			if (!loadedSo.contains(item.getDest())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checking that all so file were downloaded
	 *
	 * @param builder
	 * @return true if all downloaded  
	 */
	public boolean isAllDownloaded(Request builder) {
		
		for (DownloadItem item : builder.getDownloadItems()) {
			String path = item.getDest();
			if(loadedSo.contains(path) || downloadedSo.contains(path)){
				continue;
			}
			if (new File(path).exists()) {
				downloadedSo.put(path, path)
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param builder
	 */
	public void download(Request builder) {

		RequestListener listener = builder.getRequestListener();

		Platform def = builder.getDefaultPlatform();
		for (String abi : supportedAbis) {
			if (abi.equals(def.name)) {
				Log.i("So file had bind in apk");
				listener.afterLoaded();
				return;
			}
		}

		if (StringUtils.isBlank(matchAbi)) {
			Log.w("Not supported cpu abi");
			return;
		}

		listener.preLoad();

		List<DownloadItem> items = builder.getDownloadItems();
		if (items.isEmpty()) {
			items = createDownloadItem(builder);
			builder.setDownloadItems(items);
		}

		if (isAllLoaded(builder)) {
			listener.afterLoaded();
			return;
		}

		for (DownloadItem item : items) {
			es.execute(new Downloader(item, this));
		}
	}

	/**
	 * Generate download infomation<p>
	 * 1.Dowanload url "<b>libDirUrl</b>/<b>matchAbi</b>/lib<b>soname</b>.so"<br>
	 * 2.Store path "/data/data/com.package/files/libs/<b>matchAbi</b>/lib<b>soname</b>.so"
	 * 
	 * @param builder
	 * @return
	 */
	private List<DownloadItem> createDownloadItem(Request builder) {

		String[] soFiles = builder.getSoFiles();
		if (soFiles == null || soFiles.length == 0) {
			return Collections.emptyList();
		}
		ArrayList<DownloadItem> items = new ArrayList<DownloadItem>(
				soFiles.length);
		StringBuilder sb = new StringBuilder();
		File destDir = new File(builder.getContext().getFilesDir() + "/libs/"
				+ matchAbi);
		destDir.mkdirs();
		for (int i = 0; i < builder.getSoFiles().length; i++) {

			sb.setLength(0);
			String fileName = sb.append("lib").append(soFiles[i]).append(".so")
					.toString();

			sb.setLength(0);
			String dest = sb.append(destDir).append('/').append(fileName);
			if (loadedSo.contains(dest)) {
				continue;
			}

			sb.setLength(0);
			String url = sb.append(builder.getLibDirUrl()).append('/')
					.append(matchAbi).append('/').append(fileName).toString();
			items.add(new DownloadItem(url, dest, builder));
		}
		return items;
	}

	@Override
	public void afterLoaded() {
	}

	@Override
	public void preLoad() {
	}

	public void onError(DownloadItem item, Throwable t) {
		RequestListener listener = item.getBuilder().getRequestListener();
		if (listener != null) {
			return listener.onError(item, t);
		}
		return false;
	}
	
	public void onSuccess(DownloadItem item) {
		Request rb = item.getBuilder();
		RequestListener listener = rb.getRequestListener();
		synchronized (this) {
			if (!isAllDownloaded(rb)) {
				return ;
			}

			try {
				for (DownloadItem di : rb.getDownloadItems()) {
					String path = di.getDest().getAbsolutePath();
					if (loadedSo.contains(path)) {
						continue;
					}
					System.load(path);
					loadedSo.put(path, path);
				}
				if (listener != null) {
					listener.afterLoaded();
				}
			} catch (Throwable e) {
				// item.getDest().delete();
				Log.w(e, "Loading lib error");
			}
		}
	}
}
