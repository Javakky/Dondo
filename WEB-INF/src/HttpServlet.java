package server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServerへのHTTPリクエストがあったとき初めに呼ばれるクラス。<br>
 * URLのパス毎に適当な{@link server.UserData}のメソッドを呼び出し、処理結果を返す。
 * @author 家村
 */
@MultipartConfig (
	     fileSizeThreshold= 32768 ,
	     maxFileSize= 5242880 ,
	     maxRequestSize= 27262976
	 )
public class HttpServlet extends javax.servlet.http.HttpServlet {

	/**
	 * GETメソッドでリクエストされたときに呼ばれるメソッド。<br>
	 * 以下パスと必要なパラメータ。<br><br>
	 * <table border="1">
	 * <caption>URIのパスと必要なリクエストパラメータ</caption>
	 * <tr><th>URlのパス</th><th>パラメータ</th><th>説明</th></tr>
	 * <tr><td>/login</td><td><a href="#id">id</a>・<a href="#pass">pass</a></td><td>ユーザIDとパスワードが正しいかチェックする。<br>戻り値は<code>true</code>もしくはエラーコード。</td></tr>
	 * <tr><td>/createUser</td><td><a href="#id">id</a>・<a href="#name">name</a>・<a href="#latitude">latitude</a>・<a href="#longitude">longitude</a></td><td>パラメータに沿ったユーザを生成する。</td></tr>
	 * <tr><td>/unsubscride</td><td><a href="#id">id</a>・<a href="#pass">pass</a></td><td>そのユーザIDを持つアカウントを削除する。</tr>
	 * <tr><td>/getContribution</td><td><a href="#id">id</a>・<a href="#pass">pass</a></td><td>そのユーザが見ることのできる投稿をCSV形式で返す。<br>形式は、<code>"ユーザID", "ユーザ名", "投稿内容", "年-月-日 時:分:秒.ミリ秒"\n</code></tr>
	 * <tr><td>/getUserContribution</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#otherId">otherId</a></td><td>otherIdのユーザーの投稿をCSV形式で返す。<br>形式は、<code>"ユーザID", "ユーザ名", "投稿内容", "年-月-日 時:分:秒.ミリ秒"\n</code></tr>
	 * <tr><td>/contribution</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#contribution">contribution</a></td><td>contributionの文字列を投稿する。</td></tr>
	 * <tr><td>/deleteContribution</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#date">date</a></td><td>dateの時間に投稿された投稿を削除する。</td></tr>
	 * <tr><td>/changeName</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#name">name</a></td><td>名前をnameに変更する。</td></tr>
	 * <tr><td>/changePass</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#newPass">newPass</a></td><td>パスワードをnewPassに変更する。</td></tr>
	 * <tr><td>/updatePlace</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#latitude">latitude</a>・<a href="#longitude">longitude</a></td><td>位置情報を更新する。</td></tr>
	 * <tr><td>/setProfile</td><td><a href="#id">id</a>・<a href="#pass">pass</a>・<a href="#profile">profile</a></td><td>プロフィールをprofileに変更する。</td></tr>
	 * <tr><td>/getMyUserData</td><td><a href="#id">id</a>・<a href="#pass">pass</a></td><td>自分のユーザ情報をCSV形式で返す。<br>形式は<code>"ユーザID", "パスワード", "緯度", "経度", "プロフィール"\n</code></tr>
	 * <tr><td>/getOtherUserData</td><td><a href="#id">id</a>・<a href="#pass">pass・<a href="#otherId">otherId</a></td><td>otherIdを持つユーザのユーザ情報をCSV形式で返す。<br>形式は<code>"ユーザID", "パスワード", "緯度", "経度", "プロフィール"\n</code></tr>
	 * <tr><td>/getNumberOfPeople</td><td><a href="#id">id</a>・<a href="#pass">pass</a></td><td>同一クラン内にいるユーザーの人数を返す。</td></tr>
	 * <tr><td>/aroundUsere</td><td><a href="#id">id</a>・<a href="#pass">pass</a></td><td>同一クラン内にいるユーザーのリストを返す。<br>形式は<code>"ユーザID", </code></td></tr>
	 * <tr><td>/getIcon</td><td><a href="#id">id</a>・<a href="#pass">pass・<a href="#otherId">otherId</a></td><td>otherIdを持つユーザのアイコンをbase64でエンコードされた文字列で返す。<br>形式は<code>"ユーザID", "画像のデータ"</code></tr>
	 * </table>
	 * <br>
	 * <div id="id">id  ......リクエストを送信したユーザーのid。<br></div>
	 * <div id="pass">pass......リクエストを送信したユーザーのパスワード。<br></div>
	 * <div id="name">name......作成or変更したいユーザ名。<br></div>
	 * <div id="latitude">latitude......リクエストを送信したユーザーの現在の緯度。(倍精度浮動小数点数)<br></div>
	 * <div id="longitude">longitude......リクエストを送信したユーザーの現在の経度。(倍精度浮動小数点数)<br></div>
	 * <div id="otherId">otherId......他人のデータが必要な時の、そのユーザのID。<br></div>
	 * <div id="contribution">contribution......投稿するときの、投稿内容。<br></div>
	 * <div id="date">date......日付。(形式は<code>"年-月-日 時:分:秒.ミリ秒"</code>)<br></div>
	 * <div id="newPass">newPass......変更後のパスワード。<br></div>
	 * <div id="profile">profile......自分のプロフィール。<br></div>
	 */

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		PrintWriter out = null;
		UserData ud = null;
		StringBuilder sb = new StringBuilder();
		Logger errlog = new Logger("/usr/share/tomcat7/webapps/SNS/WEB-INF/log/errlog.csv");

		try {

			response.setContentType("text/html; charset=UTF-8");
	        response.setHeader("Access-Control-Allow-Origin", "*");
			request.setCharacterEncoding("utf-8");

			out = response.getWriter();

			String path = request.getServletPath();

			ud = new UserData();

			if(path.equals("/createUser")){
				ud.signin(Encoder.encodeUTF_8(request.getParameter("id")), Encoder.encodeUTF_8(request.getParameter("name")), Double.parseDouble(request.getParameter("latitude")), Double.parseDouble(request.getParameter("longitude")));
				sb.append("true\n");

			}else if(path.equals("/lostpass")){
				ud.resetPass(Encoder.encodeUTF_8(request.getParameter("id")));
			}else{

				ud.login(Encoder.encodeUTF_8(request.getParameter("id")), Encoder.encodeUTF_8(request.getParameter("pass")));

			}

			ArrayList<ContributionData> list = null;

			switch(path){

			case "/deleteContribution":
				ud.deleteContribution(request.getParameter("date"));
				break;

			case "/login":
				sb.append("true\n");
				break;

			case "/getContribution":
				list = ud.getTimeLine();
				for(int i = 0; i < list.size(); i++){
					sb.append(list.get(i).toString());
					sb.append("\n");
				}
				break;

			case "/getUserContribution":
				list = ud.getTimeLine(Encoder.encodeUTF_8(request.getParameter("contributor")));
				for(int i = 0; i < list.size(); i++){
					sb.append(list.get(i).toString());
					sb.append("\n");
				}
				break;

			case "/changeName":
				ud.changeName(request.getParameter("name"));
				break;

			case "/changeRange":
				ud.changeRange(request.getParameter("range"));
				break;

			case "/updatePlace":
				ud.updatePlace(Double.parseDouble(request.getParameter("latitude")), Double.parseDouble(request.getParameter("longitude")));
				break;

			case "/contribution":
				ud.contribution(Encoder.encodeUTF_8(request.getParameter("contribution")));
				break;

			case "/unsubscride":
				ud.unsubscride();
				break;

			case "/getMyUserData":
				sb.append(ud.getUserData().toString());
				sb.append("\n");
				break;

			case "/getOtherUserData":
				sb.append(ud.getUserData(Encoder.encodeUTF_8(request.getParameter("otherId"))).toString());
				sb.append("\n");
				break;

			case "/changePass":
				ud.changePass(Encoder.encodeUTF_8(request.getParameter("newPass")));
				break;

			case "/setProfile":
				ud.setProfile(Encoder.encodeUTF_8(request.getParameter("profile")));
				break;

			case "/getNumberOfPeople":
				sb.append(ud.getNumberOfPeople());
				break;

			case "/getIcon":
				sb.append(Encoder.encodeUTF_8(request.getParameter("otherId")));
				sb.append(", ");
				sb.append(ud.getIcon( Encoder.encodeUTF_8(request.getParameter("otherId"))));
				break;

			case "/getImage":
				sb.append(ud.getImage( Encoder.encodeUTF_8(request.getParameter("otherId")), Encoder.encodeUTF_8(request.getParameter("date"))));
				break;

			case "/aroundUser":
				sb.append(ud.getAroundUsers());
				break;

			case "/getDifContribution":
				sb.append(ud.getDifContribution(Encoder.encodeUTF_8(request.getParameter("date")), Encoder.encodeUTF_8(request.getParameter("otherIdList"))));
				break;

			case "/postEvent":
				ud.event(Encoder.encodeUTF_8(request.getParameter("title")), Encoder.encodeUTF_8(request.getParameter("location")), Encoder.encodeUTF_8(request.getParameter("date")), Encoder.encodeUTF_8(request.getParameter("about")));
				break;

			case "/vote":
				ud.vote(Encoder.encodeUTF_8(request.getParameter("date")), Encoder.encodeUTF_8(request.getParameter("cont")));
				break;

			case "/deleteEvent":
				ud.deleteEvent(Encoder.encodeUTF_8(request.getParameter("date")));
				break;

			case "/participants":
				sb.append(ud.getParticipantsList(Encoder.encodeUTF_8(request.getParameter("date"))));
				break;

			case "/getAd":
				list = ud.getAd();
				for(int i = 0; i < list.size(); i++){
					sb.append(list.get(i).toString());
					sb.append("\n");
				}
				break;

			case "/contact":
				ud.contact(Encoder.encodeUTF_8(request.getParameter("id")), Encoder.encodeUTF_8(request.getParameter("con")));
				sb.append("true\n");
				break;


			}

			out.println(sb.toString());
			out.flush();

		} catch (IOException e){
			e.printStackTrace();
			try {
				errlog.log(e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace(out);
				e1.printStackTrace();
			}
			out.println(e.getMessage());
		} catch (LoginException | NumberFormatException | SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			try {
				errlog.log(e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace(out);
				e1.printStackTrace();
			}
			out.println(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			try {
				errlog.log(e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace(out);
				out.println();
				e1.printStackTrace();
			}
			out.println(e.getMessage());
		}finally{
			try {
				ud.close();
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					errlog.log(e.getMessage());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			out.close();
		}
	}

	/**
	 *
	 */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response){

		PrintWriter out = null;
		Logger errlog = new Logger("/usr/share/tomcat7/webapps/SNS/WEB-INF/log/errlog.csv");

		try{

	        response.setHeader("Access-Control-Allow-Origin", "*");
			out = response.getWriter();


			UserData ud = new UserData();

			ud.login(Encoder.encodeUTF_8(request.getParameter("id")), Encoder.encodeUTF_8(request.getParameter("pass")));

			String fileName = null;

			switch(request.getServletPath()){

			case "/postIcon":
				fileName = "/usr/share/tomcat7/webapps/SNS/WEB-INF/icon/" + request.getParameter("otherId") + ".txt";
				break;

			case "/postImage":
				fileName = "/usr/share/tomcat7/webapps/SNS/WEB-INF/image/" + request.getParameter("otherId") + request.getParameter("date") + ".txt";
				break;

			}

			File path = new File(fileName);

			FileWriter fw = new FileWriter(path);

			Runtime runtime = Runtime.getRuntime();
			runtime.exec("chmod 777 " + fileName);

			fw.write(request.getParameter("post_image"));

			fw.flush();

			fw.close();

		}catch(Exception e){
			try {
				out.println(e.getMessage());
				//e.printStackTrace();
				errlog.log(e.getMessage());
			} catch (IOException e1) {
				//e1.printStackTrace();
			}
		}

	}

}

