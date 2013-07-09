package ee.tallinn.soiduplaan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
    public static final String REMOTE = "http://m.soiduplaan.tallinn.ee/";
    public static final String LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ee.tallinn.soiduplaan/files/";
    public static Map<String,String> files; // FIXME crawler
    
    {
    	files = new HashMap<String,String>();
    	files.put("mobile/css/", "bootstrap.css");;
    	files.put("mobile/css/", "gmapv2.css");
    	files.put("mobile/js/", "jq.mobi.min.js");
    	files.put("mobile/js/", "jquery.plugins.js");
    	files.put("mobile/js/", "mobile.js");
    	files.put("mobile/img/", "add-favourite-small.png");
		files.put("mobile/img/", "arrow-small.png");
		files.put("mobile/img/", "arrow.png");
		files.put("mobile/img/", "back.png");
		files.put("mobile/img/", "bar.png");
		files.put("mobile/img/", "bus.png");
		files.put("mobile/img/", "collapse.png");
		files.put("mobile/img/", "commercebus.png");
		files.put("mobile/img/", "corner.png");
		files.put("mobile/img/", "expand.png");
		files.put("mobile/img/", "express.png");
		files.put("mobile/img/", "favourite-small.png");
		files.put("mobile/img/", "favourites-invert.png");
		files.put("mobile/img/", "favourites.png");
		files.put("mobile/img/", "footer.png");
		files.put("mobile/img/", "glyphicons-halflings-white.png");
		files.put("mobile/img/", "glyphicons-halflings.png");
		files.put("mobile/img/", "harju.png");
		files.put("mobile/img/", "home-invert.png");
		files.put("mobile/img/", "home.png");
		files.put("mobile/img/", "info-invert.png");
		files.put("mobile/img/", "info.png");
		files.put("mobile/img/", "lang-de.png");
		files.put("mobile/img/", "lang-en.png");
		files.put("mobile/img/", "lang-et.png");
		files.put("mobile/img/", "lang-lt.png");
		files.put("mobile/img/", "lang-lv.png");
		files.put("mobile/img/", "lang-ru.png");
		files.put("mobile/img/", "list-small.png");
		files.put("mobile/img/", "map-invert.png");
		files.put("mobile/img/", "map-small.png");
		files.put("mobile/img/", "map.png");
		files.put("mobile/img/", "menu.png");
		files.put("mobile/img/", "next.png");
		files.put("mobile/img/", "pin-end.png");
		files.put("mobile/img/", "pin-start.png");
		files.put("mobile/img/", "planner-invert.png");
		files.put("mobile/img/", "planner.png");
		files.put("mobile/img/", "previous.png");
		files.put("mobile/img/", "regionalbus.png");
		files.put("mobile/img/", "remove.png");
		files.put("mobile/img/", "schedule-invert.png");
		files.put("mobile/img/", "schedule.png");
		files.put("mobile/img/", "search-invert.png");
		files.put("mobile/img/", "search-small.png");
		files.put("mobile/img/", "search.png");
		files.put("mobile/img/", "tallinn.png");
		files.put("mobile/img/", "train.png");
		files.put("mobile/img/", "tram.png");
		files.put("mobile/img/", "trolley.png");
		files.put("mobile/img/", "walk.png");
		files.put("mobile/img/", "wheelchair.png");
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
		download(httpclient, REMOTE, "", "index.html");
		download(httpclient, REMOTE, "", "mobile-min.js");
		download(httpclient, REMOTE, "data/", "routes.txt");
		download(httpclient, REMOTE, "data/", "stops.txt");
		
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
			muuda(LOCAL);
		} catch (ClientProtocolException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void muuda(String location) throws IOException {
		File inFile = new File(location + "index.html");
		File outFile = new File(location + "index2.html");
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		String findLine1 = "<meta name=\"description\" content=\"\">";
		int found = 0;
		String findLine2 = "</body>";
		String currentLine;
		while((currentLine = reader.readLine()) != null) {
		    // trim newline when comparing with lineToRemove
		    if(found == 0 && currentLine.equals(findLine1)) {
		    	writer.write("<base href=\"file://" + location + "\">");
		    	found++;
		    } else if (found == 1 && currentLine.equals(findLine2)) {
		    	writer.write("<link href=\"file:///android_asset/www/ext.css\" rel=\"stylesheet\" media=\"screen\">" +
		    			"<script type=\"text/javascript\" charset=\"utf-8\" src=\"file:///android_asset/www/cordova.js\"></script>");
		    	found++;
		    }
		    writer.write(currentLine);
		}
		reader.close();
		writer.close();
		outFile.renameTo(inFile);
	}
}
