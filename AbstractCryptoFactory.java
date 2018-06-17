import java.io.File;

public abstract class AbstractCryptoFactory {

	public static AbstractCryptoFactory getCryptoFactory(String algorithm) {
		if (algorithm.equals("AES")) {
			return new AESCryptoFactory();
		} else if (algorithm.equals("DES")) {
			return new DESCryptoFactory();
		}
		return null;
	}

	public abstract AbstractCryptoFactory getCryptor(String mode);

	public abstract void encrypt(String key, String initializationVector, File inputFile, File outputFile);

	public abstract void decrypt(String key, String initializationVector, File inputFile, File outputFile);

	public abstract int setNumberOfThreads(int num);

}
