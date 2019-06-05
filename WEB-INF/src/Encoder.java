package server;

import java.io.UnsupportedEncodingException;

public class Encoder {

	public static String encode(String data, String aftcode) throws UnsupportedEncodingException{

		return new String(data.getBytes(aftcode), aftcode);

	}

	public static String encodeUTF_8(String data) throws UnsupportedEncodingException{

		return new String(data.getBytes("UTF-8"), "UTF-8");

	}

}
