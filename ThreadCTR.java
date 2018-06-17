import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class ThreadCTR extends Thread {
	private int opmode;
	private byte[] dataIn;
	private byte[] dataOut;
	private IvParameterSpec iv;
	private Key secretKey;
	private String configuration;
	private boolean isFinished;

	public ThreadCTR(Key secretKey, IvParameterSpec iv, int opmode, byte[] dataIn, String configuration) {
		this.secretKey = secretKey;
		this.configuration = configuration;
		this.iv = iv;
		this.opmode = opmode;
		this.dataIn = dataIn;
		this.dataOut = new byte[dataIn.length];
		this.isFinished = false;

	}

	public byte[] generateIV() {
		SecureRandom randomSecureRandom = null;
		byte[] iv = null;
		try {
			randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (configuration.contains("AES")) {
			iv = new byte[16];
		} else if (configuration.contains("DES")) {
			iv = new byte[8];
		}
		randomSecureRandom.nextBytes(iv);
		return iv;
	}

	@Override
	public void run() {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(configuration);
			cipher.init(opmode, secretKey, iv);
			dataOut = cipher.doFinal(dataIn);
			isFinished = true;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}

	public byte[] getDataIn() {
		return dataIn;
	}

	public void setDataIn(byte[] dataIn) {
		this.dataIn = dataIn;
	}

	public byte[] getDataOut() {
		return dataOut;
	}

	public void setDataOut(byte[] dataOut) {
		this.dataOut = dataOut;
	}

	public int getOpmode() {
		return opmode;
	}

	public void setOpmode(int opmode) {
		this.opmode = opmode;
	}

	public IvParameterSpec getIv() {
		return iv;
	}

	public void setIv(IvParameterSpec iv) {
		this.iv = iv;
	}

	public Key getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(Key secretKey) {
		this.secretKey = secretKey;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

}
