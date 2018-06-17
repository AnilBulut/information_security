import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {

	private String inputFile;
	private String outputFile;
	private String algorithm;
	private String mode;
	private int numberOfThread;
	private String keyFile;
	private String vector;
	private String key;
	private Scanner wordScanner;
	private String type;

	public void parseCommand(String input) {

		String tokens[] = input.split(" ");
		if (input.contains("-e")) {

			type = "encryption";
			if (input.contains("CTR")) {
				String temp = tokens[3];
				numberOfThread = Integer.parseInt(temp);

				inputFile = tokens[5];
				outputFile = tokens[7];
				algorithm = tokens[8];
				mode = tokens[9];
				keyFile = tokens[10];
			} else {
				inputFile = tokens[3];
				outputFile = tokens[5];
				algorithm = tokens[6];
				mode = tokens[7];
				keyFile = tokens[8];

			}

		}

		else if (input.contains("-d")) {
			type = "decryption";

			if (input.contains("CTR")) {
				String temp = tokens[3];
				numberOfThread = Integer.parseInt(temp);
				inputFile = tokens[5];
				outputFile = tokens[7];
				algorithm = tokens[8];
				mode = tokens[9];
				keyFile = tokens[10];
			} else {
				inputFile = tokens[3];
				outputFile = tokens[5];
				algorithm = tokens[6];
				mode = tokens[7];
				keyFile = tokens[8];

			}

		}
	}

	public void openAndParseKeyFile(String keyFile) {

		ArrayList<String> temp = new ArrayList<String>();
		Scanner scan = null;
		try {
			scan = new Scanner(new File(keyFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (scan.hasNextLine()) {
			wordScanner = new Scanner(scan.nextLine());
			while (wordScanner.hasNext()) {
				String str = wordScanner.next();
				temp.add(str);
			}
		}

		vector = temp.get(0);
		key = temp.get(2);

	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public int getNumberOfThread() {
		return numberOfThread;
	}

	public void setNumberOfThread(int numberOfThread) {
		this.numberOfThread = numberOfThread;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public String getVector() {
		return vector;
	}

	public void setVector(String vector) {
		this.vector = vector;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}