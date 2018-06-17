import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_CTR extends AESCryptoFactory {
	private int numberOfThreads;
	private List<ThreadCTR> threads;
	private String configutation;

	public AES_CTR() {
		this.threads = new ArrayList<>();
		System.out.println("AES_CTR");
		this.configutation = "AES/CTR/NoPadding";
	}

	public void divideDataForThreads(FileInputStream inputStream, File inputFile, Key secretKey, IvParameterSpec iv,
			int opmode) {
		int divider = (int) inputFile.length() % numberOfThreads;
		try {
			for (int i = 0; i < numberOfThreads; i++) {
				if (i == numberOfThreads - 1) {
					byte[] blockBuffer = new byte[(((int) inputFile.length()) / numberOfThreads) + divider];
					inputStream.read(blockBuffer);
					ThreadCTR tctr = new ThreadCTR(secretKey, iv, opmode, blockBuffer, this.configutation);
					threads.add(tctr);
				} else {
					byte[] blockBuffer = new byte[((int) inputFile.length() - divider) / numberOfThreads];
					inputStream.read(blockBuffer);
					ThreadCTR tctr = new ThreadCTR(secretKey, iv, opmode, blockBuffer, this.configutation);
					threads.add(tctr);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runThreads() {
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).start();
		}
	}

	public void writeDataToFile(FileOutputStream outputStream) {
		for (int i = 0; i < threads.size(); i++) {
			try {
				// System.out.println("writer: "+threads.get(i).getId());
				outputStream.write(threads.get(i).getDataOut());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isAllThreadsFinished() {
		int counter = 0;
		for (int i = 0; i < threads.size(); i++) {
			if (threads.get(i).isFinished()) {
				counter++;
			}
		}
		if (counter == numberOfThreads) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void encrypt(String key, String initializationVector, File inputFile, File outputFile) {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes("UTF-8"));
			FileInputStream inputStream = new FileInputStream(inputFile);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			// divide data for threads
			synchronized (inputStream) {
				divideDataForThreads(inputStream, inputFile, secretKey, iv, Cipher.ENCRYPT_MODE);
			}
			// run threads
			runThreads();
			// write crypted data
			while (true) {
				if (isAllThreadsFinished()) {
					writeDataToFile(outputStream);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void decrypt(String key, String initializationVector, File inputFile, File outputFile) {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes("UTF-8"));
			FileInputStream inputStream = new FileInputStream(inputFile);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			// divide data for threads
			synchronized (inputStream) {
				divideDataForThreads(inputStream, inputFile, secretKey, iv, Cipher.DECRYPT_MODE);
			}
			// run threads
			runThreads();
			// write crypted data
			while (true) {
				if (isAllThreadsFinished()) {
					writeDataToFile(outputStream);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	@Override
	public int setNumberOfThreads(int num){
		this.numberOfThreads=num;
		return this.numberOfThreads;
	}


}
