package ee.tallinn.soiduplaan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ee.tallinn.soiduplaan.MainActivity.ResponseReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

public class UpdateService extends IntentService {
	
    public static final String MSG = "omsg";
    public static final String INDEX = "http://m.soiduplaan.tallinn.ee/";
    public static final String REMOTE = "http://m.soiduplaan.tallinn.ee/mobile/";
    public static final String LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ee.tallinn.soiduplaan/files/";
    public static Map<String,String> files;
    
    {
    	files = new HashMap<String,String>();
    	files.put("css/", "bootstrap.css");;
    	files.put("css/", "gmapv2.css");
    	files.put("js/", "jq.mobi.min.js");
    	files.put("js/", "jquery.plugins.js");
    	files.put("js/", "mobile.js");
    	files.put("img/", "add-favourite-small.png");
		files.put("img/", "arrow-small.png");
		files.put("img/", "arrow.png");
		files.put("img/", "back.png");
		files.put("img/", "bar.png");
		files.put("img/", "bus.png");
		files.put("img/", "collapse.png");
		files.put("img/", "commercebus.png");
		files.put("img/", "corner.png");
		files.put("img/", "expand.png");
		files.put("img/", "express.png");
		files.put("img/", "favourite-small.png");
		files.put("img/", "favourites-invert.png");
		files.put("img/", "favourites.png");
		files.put("img/", "footer.png");
		files.put("img/", "glyphicons-halflings-white.png");
		files.put("img/", "glyphicons-halflings.png");
		files.put("img/", "harju.png");
		files.put("img/", "home-invert.png");
		files.put("img/", "home.png");
		files.put("img/", "info-invert.png");
		files.put("img/", "info.png");
		files.put("img/", "lang-de.png");
		files.put("img/", "lang-en.png");
		files.put("img/", "lang-et.png");
		files.put("img/", "lang-lt.png");
		files.put("img/", "lang-lv.png");
		files.put("img/", "lang-ru.png");
		files.put("img/", "list-small.png");
		files.put("img/", "map-invert.png");
		files.put("img/", "map-small.png");
		files.put("img/", "map.png");
		files.put("img/", "menu.png");
		files.put("img/", "next.png");
		files.put("img/", "pin-end.png");
		files.put("img/", "pin-start.png");
		files.put("img/", "planner-invert.png");
		files.put("img/", "planner.png");
		files.put("img/", "previous.png");
		files.put("img/", "regionalbus.png");
		files.put("img/", "remove.png");
		files.put("img/", "schedule-invert.png");
		files.put("img/", "schedule.png");
		files.put("img/", "search-invert.png");
		files.put("img/", "search-small.png");
		files.put("img/", "search.png");
		files.put("img/", "tallinn.png");
		files.put("img/", "train.png");
		files.put("img/", "tram.png");
		files.put("img/", "trolley.png");
		files.put("img/", "walk.png");
		files.put("img/", "wheelchair.png");
    }
    
	public UpdateService() {
		super("UpdateService");
		System.out.println("init");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("alga " + LOCAL);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		File file = new File(LOCAL);
		if (!file.exists()) {
			System.out.println("teeme kaustad " + LOCAL);
			file.mkdirs();
		}
		for (Entry<String,String> e : files.entrySet()) {
			download(httpclient, REMOTE, e.getKey(), e.getValue());
		}
		download(httpclient, INDEX, "", "index.html");
		download(httpclient, INDEX, "", "mobile-min.js");
		download(httpclient, INDEX, "data/", "routes.txt");
		download(httpclient, INDEX, "data/", "stops.txt");
		
		file = new File(LOCAL);
		for (File f : file.listFiles()) {
			System.out.println("-"+f.getName());
			if (f.listFiles() != null)
			for (File f2 : f.listFiles()) {
				System.out.println("--"+f2.getName());
			}
		}
		
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(MSG, "ja valmis saigi");
        sendBroadcast(broadcastIntent);
	}

	private void download(HttpClient httpclient, String remote, String folderName, String fileName) {
		HttpGet httpget = null;
		HttpEntity entity = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		File file = null;
		int inByte;
		httpget = new HttpGet(remote + folderName + fileName);
		try {
			entity = httpclient.execute(httpget).getEntity();
			bis = new BufferedInputStream(httpclient.execute(httpget).getEntity().getContent());
			file = new File(LOCAL + folderName);
			if (!file.exists()) {
				System.out.println("teeme kausta " + folderName);
				file.mkdirs();
			}
			System.out.println("dirs: " + file.mkdirs());
			file = new File(LOCAL + folderName + fileName);
			if (!file.exists()) {
				System.out.println("teeme uu faili " + fileName);
				file.createNewFile();
			} 
			bos = new BufferedOutputStream(new FileOutputStream(file));
			while ((inByte = bis.read()) != -1) {
				bos.write(inByte);
			}
			bis.close();
			bos.close();
			entity.consumeContent(); // sulgeme ühenduse
			entity = null;
		} catch (ClientProtocolException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}


