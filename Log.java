import java.io.FileWriter;
import java.io.IOException;

public class Log {

	public void createLog(String input, String output, String type, String algorithm, String mode, long duration){

		try {
			String filename = "run.log";
			FileWriter fw = new FileWriter(filename, true);
			fw.write(input);
			fw.write(" ");
			fw.write(output);
			fw.write(" ");
			fw.write(type);
			fw.write(" ");
			fw.write(algorithm);
			fw.write(" ");
			fw.write(mode);
			fw.write(" ");
			fw.write(Long.toString(duration));
			fw.write(" ms\n");
			fw.write("\r\n");
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}

	}

}