import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Start {
	private List<File> files;
	private PrivateKey privateKey;
	private RSAPublicKey publicKey;
	private String hashFunction;

	public Start() {
		
	}
	
	public Start(File monitoredFolder,File registryFile,File logFile,File publicKeyFile, File privateKeyFile, String hashFunction,String intervalTime) {
		files = new ArrayList<File>();
		this.hashFunction=hashFunction;
		readFolder(monitoredFolder);
		createRegistryFile(registryFile);
		readKeys(publicKeyFile.toPath(), privateKeyFile.toPath());
		sign(registryFile.getPath(), privateKey);
		//sign(registryFile, privateKey);
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

	public void createRegistryFile(File registryFile) {
		try {
			PrintWriter printWriter = new PrintWriter(registryFile, "UTF-8");
			for (int i = 0; i < files.size(); i++) {
				printWriter
						.println(files.get(i).getAbsolutePath() + " " + calculateHashValueOfFile(files.get(i), hashFunction));
			}
			printWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//DatatypeConverter.parseBase64Binary()

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

	public void readKeys(Path publicKeyPath, Path privateKeyPath) {
		try {
			String publicKeyContent = new String(Files.readAllBytes(publicKeyPath));
			String privateKeyContent = new String(Files.readAllBytes(privateKeyPath));
			privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "")
					.replace("-----END PRIVATE KEY-----", "");
			publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "");

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
			PrivateKey privKey = keyFactory.generatePrivate(keySpecPKCS8);

			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
			RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
			

			this.privateKey=privKey;
			this.publicKey=pubKey;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
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
			FileWriter fileWriter = new FileWriter(registryFile.getAbsolutePath(), true);
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
	public void addPeriodicalTask(HashMap<String, String> commandMap){
		//String[] command={"/bin/sh","-c","crontab -l | { cat; echo \"*/2 * * * * java -cp /home/eren/workspace/FileIntegrity/src/ integrity -p /home/eren/workspace/FileIntegrity/MonitoredFolder -r /home/eren/workspace/FileIntegrity/registry -l /home/eren/workspace/FileIntegrity/log -h MD5 -k /home/eren/workspace/FileIntegrity/public_key.pem\"; } | crontab -"};
		String[] command={"/bin/sh","-c",generateCheckCommand(commandMap)};
		StringBuffer output = new StringBuffer();
		String line="";
		try {
			Process process=Runtime.getRuntime().exec(command);
			BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
			System.out.println(output.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	public void removePeriodicalTask(){
		String[] command={"/bin/sh","-c","crontab -r"};
		StringBuffer output = new StringBuffer();
		String line="";
		try {
			Process process=Runtime.getRuntime().exec(command);
			BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
			System.out.println(output.toString());

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	public String getWorkingDirectory(){
		String[] command = { "/bin/sh", "-c", "pwd ls -l" };
		StringBuffer output = new StringBuffer();
		String line="";
		String workingDirectory="";
		try {
			Process process=Runtime.getRuntime().exec(command);
			BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = reader.readLine())!= null) {
				output.append(line);
			}
			workingDirectory=output.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workingDirectory;
	}
	public String findMainJavaFile(String workingDirectory){
		String fileLocation="";
		String[] command = { "/bin/sh", "-c", "find "+workingDirectory+" -name integrity.java" };
		String line="";
		StringBuffer output = new StringBuffer();
		try {
			Process process=Runtime.getRuntime().exec(command);
			BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = reader.readLine())!= null) {
				output.append(line);
			}
			fileLocation=output.toString().replaceAll("integrity.java", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileLocation;
	}
	public String generateCheckCommand(HashMap<String, String> commandMap){
		StringBuffer stringBuffer=new StringBuffer("");
		String preCommand="crontab -l | { cat; echo \"*/"+commandMap.get("-i")+" * * * * java -cp "+findMainJavaFile(getWorkingDirectory())+" integrity";
		commandMap.remove("state");
		commandMap.remove("-i");
		String[] keyArray=commandMap.get("-k").split(" ");
		commandMap.remove("-k");
		commandMap.put("-k", keyArray[1]);
		for (Entry<String, String> entry : commandMap.entrySet()) {
			
			stringBuffer.append(" "+entry.getKey()+" "+entry.getValue());
		}
		String command=preCommand+stringBuffer.toString()+"\"; } | crontab -";
		return command;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(RSAPublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public String getHashFunction() {
		return hashFunction;
	}

	public void setHashFunction(String hashFunction) {
		this.hashFunction = hashFunction;
	}

}
