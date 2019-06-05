package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Logger {

	private File file = null;

	Logger(String fileName){
		this(new File(fileName));
	}

	Logger(File file){
		this.file = file;
	}

	public void log(String log) throws IOException{

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

		pw.print(LocalDate.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ":" + log + "\n");

		pw.flush();
		pw.close();

	}

}
