import java.io.File;

public class AESCryptoFactory extends AbstractCryptoFactory {

	
	@Override
	public AbstractCryptoFactory getCryptor(String mode) {
		if (mode.equals("CBC")) {
			return new AES_CBC();
		} else if (mode.equals("OFB")) {
			return new AES_OFB();
		} else if (mode.equals("CTR")) {
			return new AES_CTR();
		} else {
			return null;
		}
	}

	@Override
	public void encrypt(String key, String initializationVector, File inputFile, File outputFile) {

	}

	@Override
	public void decrypt(String key, String initializationVector, File inputFile, File outputFile) {

	}

	@Override
	public int setNumberOfThreads(int num) {
		return 0;
	}





}
