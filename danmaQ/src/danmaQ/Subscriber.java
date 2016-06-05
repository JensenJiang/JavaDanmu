package danmaQ;

import com.trolltech.qt.gui.*;
import com.trolltech.qt.network.QHttp;
import com.trolltech.qt.network.QHttpRequestHeader;
import com.trolltech.qt.network.QHttpResponseHeader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;

import java.io.Closeable;
import java.io.IOException;

public class Subscriber extends QObject implements Runnable{
	public Signal3<String, String, String> new_danmaku = new Signal3<>();
	public Signal1<String> new_alert = new Signal1<>();
	public boolean mark_stop;
//	private QHttp http;
//	private QHttpRequestHeader header;
	private String server, channel, passwd, _uuid;
	QObject parent;
	private HttpGet get;
	private CloseableHttpClient http = new DefaultHttpClient();
	Subscriber(String server, String channel, String passwd, QObject parent){
		this.server = server;
		this.channel = channel;
		this.passwd = passwd;
		this.parent = parent;
		QUuid uuid = QUuid.createUuid();
		this._uuid = uuid.toString();
		String uri = String.format("/api/v1.1/channels/%s/danmaku", this.channel);
		QUrl baseUrl = new QUrl(this.server);
//		int port = baseUrl.port(80);
//		QHttp.ConnectionMode mode = QHttp.ConnectionMode.ConnectionModeHttp;

		get = new HttpGet(this.server + uri);
		get.setHeader("Host", baseUrl.host());
		get.setHeader("X-GDANMAKU-SUBSCRIBER-ID", this._uuid);
		get.setHeader("X-GDANMAKU-AUTH-KEY", this.passwd);

//		if(baseUrl.scheme().compareTo("https") == 0){
//			port = baseUrl.port(443);
//			mode = QHttp.ConnectionMode.ConnectionModeHttps;
//		}
//		http = new QHttp(baseUrl.host(),mode, port, this.parent);
//		header = new QHttpRequestHeader("GET",uri);
//		header.setValue("Host", baseUrl.host());
//		header.setValue("X-GDANMAKU-SUBSCRIBER-ID", this._uuid);
//		header.setValue("X-GDANMAKU-AUTH-KEY", this.passwd);
	}
	public void run(){
		mark_stop = false;
//		QEventLoop loop = new QEventLoop();
		//http.done.connect(loop,"quit()");
//		((App)this.parent).stop_subscription.connect(loop,"quit()");
		
		//Set timeout
//		QTimer timeout = new QTimer();
//		timeout.setSingleShot(true);
//		timeout.timeout.connect(http,"abort()");
		while(true){
//			timeout.start(10000);
			try {
				CloseableHttpResponse response = http.execute(this.get);
				parse_response(response);
				response.close();
			} catch (IOException e) {
				System.out.println("Network error");
			}
//			loop.exec();
//			timeout.stop();
//			if(mark_stop){
//				System.out.println("Thread marked to stop");
//				break;
//			}
//			if(http.error() != null){
//				System.out.println(http.errorString() + "\nWait 2 secs");
//				try{
//					Thread.sleep(2000);
//				}
//				catch(InterruptedException e){
//					System.out.println("Thread has been interrupted!");
//				}
//			}
//			else{
//				parse_response();
//			}
		}
	}
	void parse_response(HttpResponse resp){
//		QHttpResponseHeader resp = http.lastResponse();
		boolean fatal = false;
		int statusCode = resp.getStatusLine().getStatusCode();
		if(statusCode >= 400){
			fatal = true;
			String errMsg = "";
			if(statusCode == 403){
				errMsg = "Wrong Password";
			}else if(statusCode == 404){
				errMsg = "No Such Channel";
			}else if(statusCode >= 500){
				errMsg = "Server Error";
			}
			System.out.println(errMsg);
			new_alert.emit(errMsg);
		}
		if(fatal) {
			return;
		}
		JSONParser parser = new JSONParser();
		byte []content = new byte[(int)(resp.getEntity().getContentLength())];
		try {
			resp.getEntity().getContent().read(content);
		} catch (IOException e) {
			System.out.println("content io error");
		}
		System.out.println(new String(content));
//		QByteArray json = http.readAll();
		try{
			JSONArray dms = (JSONArray)parser.parse(new String(content));
			for(Object i : dms){
				JSONObject dm = (JSONObject)i;
				String text = (String)dm.get("text"),
					  color = (String)dm.get("style"),
				   position = (String)dm.get("position");
				System.out.println(text);
				new_danmaku.emit(text, color, position);
			}
		}catch(ParseException e){
			System.out.println(e);
		}
	}
}
