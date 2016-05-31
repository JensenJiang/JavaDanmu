package danmaQ;

import com.trolltech.qt.gui.*;
import com.trolltech.qt.network.QHttp;
import com.trolltech.qt.network.QHttpRequestHeader;
import com.trolltech.qt.network.QHttpResponseHeader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;

public class Subscriber extends QObject implements Runnable{
	public Signal3<String, String, String> new_danmaku = new Signal3<>();
	public Signal1<String> new_alert = new Signal1<>();
	public boolean mark_stop;
	private QHttp http;
	private QHttpRequestHeader header;
	private String server, channel, passwd, _uuid;
	Subscriber(String server, String channel, String passwd, QObject parent){
		this.server = server;
		this.channel = channel;
		this.passwd = passwd;
		this.setParent(parent);
		QUuid uuid = QUuid.createUuid();
		this._uuid = uuid.toString();
		String uri = String.format("/api/v1.1/channels/%s/danmaku", this.channel);
		QUrl baseUrl = new QUrl(this.server);
		int port = baseUrl.port(80);
		QHttp.ConnectionMode mode = QHttp.ConnectionMode.ConnectionModeHttp;
		
		if(baseUrl.scheme().compareTo("https") == 0){
			port = baseUrl.port(443);
			mode = QHttp.ConnectionMode.ConnectionModeHttps;
		}
		http = new QHttp(baseUrl.host(),mode, port, this);
		header = new QHttpRequestHeader("GET",uri);
		header.setValue("Host", baseUrl.host());
		header.setValue("X-GDANMAKU-SUBSCRIBER-ID", this._uuid);
		header.setValue("X-GDANMAKU-AUTH-KEY", this.passwd);
	}
	public void run(){
		mark_stop = false;
		QEventLoop loop = new QEventLoop();
		http.done.connect(loop,"quit()");
		((App)this.parent()).stop_subscription.connect(loop,"quit()");
		
		//Set timeout
		QTimer timeout = new QTimer();
		timeout.setSingleShot(true);
		timeout.timeout.connect(http,"abort()");
		while(true){
			timeout.start(10000);
			http.request(header);
			loop.exec();
			timeout.stop();
			if(mark_stop){
				System.out.println("Thread marked to stop");
				break;
			}
			if(http.error() != null){
				System.out.println(http.errorString() + "\nWait 2 secs");
				try{
					Thread.sleep(2000);
				}
				catch(InterruptedException e){
					System.out.println("Thread has been interrupted!");
				}
			}
			else{
				parse_response();
			}
		}
	}
	void parse_response(){
		QHttpResponseHeader resp = http.lastResponse();
		if(resp.isValid()){
			boolean fatal = false;
			int statusCode = resp.statusCode();
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
			if(fatal){
				return;
			}
		}
		JSONParser parser = new JSONParser();
		QByteArray json = http.readAll();
		try{
			JSONArray dms = (JSONArray)parser.parse(json.toString());
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
