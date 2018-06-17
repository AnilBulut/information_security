import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
//import java.security.KeyStore.Entry;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;

public class Check {
	private HashMap<String, String> registryMap;
	private HashMap<String, String> currentFilesMap;
	private String hashFunction;
	private RSAPublicKey publicKey;
	private File logFile;
	
	public Check() {
	}

	public Check(File monitoredFolder, File registryFile, String hashFunction, File logFile, File publicKeyFile) {
		this.registryMap = new HashMap<>();
		this.currentFilesMap = new HashMap<>();
		this.hashFunction = hashFunction;
		this.logFile = logFile;
		readPublicKey(publicKeyFile.toPath());
		if(verify(registryFile.getAbsolutePath(), this.publicKey, readSignature(registryFile.getAbsolutePath()))){
			writeLogFile("verification success");
			readRegistryFile(registryFile);
			readFolder(monitoredFolder);
			determineChanges();
		}
		else{
			writeLogFile("verification failed");
		}
	}

	public void readFolder(File monitoredFolder) {
		for (File file : monitoredFolder.listFiles()) {
			if (file.isDirectory()) {
				readFolder(file);
			} else {
				String hashValue = calculateHashValueOfFile(file, hashFunction);
				currentFilesMap.put(file.getAbsolutePath(), hashValue);
			}
		}
	}

	public void determineChanges() {
		for (Entry<String, String> registryEntry : registryMap.entrySet()) {
			if (!currentFilesMap.containsKey(registryEntry.getKey())) {
				//System.out.println(registryEntry.getKey() + " deleted");
				writeLogFile(registryEntry.getKey() + " deleted");

			} else {
				if (registryEntry.getValue().equals(currentFilesMap.get(registryEntry.getKey()))) {
					//System.out.println(registryEntry.getKey() + " not changed");
					//writeLogFile(registryEntry.getKey() + " not changed");
				} else {
					//System.out.println(registryEntry.getKey() + " altered");
					writeLogFile(registryEntry.getKey() + " altered");
				}
			}
		}
		for (Entry<String, String> filesEntry : currentFilesMap.entrySet()) {
			if (!registryMap.containsKey(filesEntry.getKey())) {
				//System.out.println(filesEntry.getKey() + " created");
				writeLogFile(filesEntry.getKey() + " created");
			}
		}
	}

	public void readRegistryFile(File registryFile) {
		String line = "";
		try {
			FileReader fileReader = new FileReader(registryFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				String[] pair = line.split(" ");
				registryMap.put(pair[0], pair[1]);
			}
			bufferedReader.close();
			registryMap.remove("##signature:");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printMap() {
		System.out.println("rregistry:");
		for (Entry<String, String> entry : registryMap.entrySet()) {
			System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());
		}
		System.out.println("ccurrent files:");
		for (Entry<String, String> entry : currentFilesMap.entrySet()) {
			System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());
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

	public void readPublicKey(Path publicKeyPath) {
		try {
			String publicKeyContent = new String(Files.readAllBytes(publicKeyPath));
			publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "");

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
			RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);

			this.publicKey = pubKey;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public boolean verify(String pathOfRegistryFile, RSAPublicKey publicKey, String signature) {
		boolean returnValue = false;
		String lastLine = "##signature: " + signature;
		try {
			File registryFile = new File(pathOfRegistryFile);
			byte[] fileBytes = new byte[(int) registryFile.length() - lastLine.length()];
			FileInputStream inputStream = new FileInputStream(registryFile);
			inputStream.read(fileBytes);
			Signature publicSignature = Signature.getInstance("SHA256withRSA");
			publicSignature.initVerify(publicKey);
			publicSignature.update(fileBytes);
			byte[] signatureBytes = Base64.getDecoder().decode(signature);
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

	public String readSignature(String pathOfRegistryFile) {
		String signatureLine = "";
		String line = "";
		String signature = "";
		try {

			File registryFile = new File(pathOfRegistryFile);
			FileReader fileReader = new FileReader(registryFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				signatureLine = line;
			}
			signature = signatureLine.replaceAll("##signature: ", "");
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return signature;
	}

	public void writeLogFile(String content) {
		try {
			String timeStamp = new SimpleDateFormat("dd-MM-yyy HH:mm:ss").format(new java.util.Date());
			//File file=new File(logFile.getAbsolutePath());
			FileWriter fileWriter = new FileWriter(logFile.getAbsolutePath(), true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(timeStamp+": "+content);
			bufferedWriter.newLine();
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
