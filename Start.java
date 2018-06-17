import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Start {
	private List<File> files;

	public Start() {
		files = new ArrayList<File>();
	}

	public void readFolder(File monitoredFolder) {
		for (File file : monitoredFolder.listFiles()) {
			if (file.isDirectory()) {
				readFolder(file);
			} else {
				files.add(file);
			}
		}
	}

	public void createRegistryFile(String path) {
		File registryFile = new File(path);
		try {
			PrintWriter printWriter = new PrintWriter(registryFile, "UTF-8");
			for (int i = 0; i < files.size(); i++) {
				printWriter
						.println(files.get(i).getAbsolutePath() + " " + calculateHashValueOfFile(files.get(i), "MD5"));
			}
			printWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String calculateHashValueOfFile(File file, String hashFunction) {
		byte[] digest = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(hashFunction);
			InputStream inputStream = Files.newInputStream(file.toPath());
			DigestInputStream dis = new DigestInputStream(inputStream, messageDigest);
			while (dis.read() != -1)
				;
			dis.close();
			digest = messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Base64.getEncoder().encodeToString(digest);
	}

	public KeyPair generateKeyPair() {
		KeyPair keyPair = null;
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048, new SecureRandom());
			keyPair = generator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return keyPair;
	}

	public void writeKeyPairsToFile(String path) {

	}

	public void sign(String pathOfRegistryFile, PrivateKey privateKey) {
		try {
			File registryFile = new File(pathOfRegistryFile);
			byte[] fileBytes = new byte[(int) registryFile.length()];
			FileInputStream inputStream = new FileInputStream(registryFile);
			inputStream.read(fileBytes);
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(fileBytes);
			String signedHashValue = Base64.getEncoder().encodeToString(signature.sign());
			FileWriter fileWriter = new FileWriter(registryFile.getName(), true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("##signature: " + signedHashValue);
			bufferedWriter.close();
			inputStream.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
	}

	public String readSignature(String pathOfRegistryFile) {
		String signatureLine = "";
		String line="";
		String signature="";
		try {

			File registryFile = new File(pathOfRegistryFile);
			FileReader fileReader = new FileReader(registryFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null){
				signatureLine=line;
			}
			signature=signatureLine.replaceAll("##signature: ", "");
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return signature;
	}

	public boolean verify(String pathOfRegistryFile, PublicKey publicKey,String signature) {
		boolean returnValue=false;
		String lastLine="##signature: "+signature;
		try{
			File registryFile = new File(pathOfRegistryFile);
			byte[] fileBytes = new byte[(int) registryFile.length()-lastLine.length()];
			FileInputStream inputStream = new FileInputStream(registryFile);
			inputStream.read(fileBytes);
			Signature publicSignature=Signature.getInstance("SHA256withRSA");
			publicSignature.initVerify(publicKey);
			publicSignature.update(fileBytes);
			byte[] signatureBytes =Base64.getDecoder().decode(signature);
			returnValue = publicSignature.verify(signatureBytes);
			inputStream.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return returnValue;
	}

}
