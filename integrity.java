import java.awt.RenderingHints.Key;
import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class integrity {
	public static void main(String[] args) {
		File monitoredFolder=new File("/home/eren/workspace/FileIntegrity/MonitoredFolder");
		Start start=new Start();
		start.readFolder(monitoredFolder);
		start.createRegistryFile("/home/eren/workspace/FileIntegrity/registry");
		KeyPair keyPair=start.generateKeyPair();
		PrivateKey prk=keyPair.getPrivate();
		PublicKey puk=keyPair.getPublic();
		start.sign("/home/eren/workspace/FileIntegrity/registry", prk);
		if(start.verify("/home/eren/workspace/FileIntegrity/registry", puk, start.readSignature("/home/eren/workspace/FileIntegrity/registry"))){
			System.out.println("verification success");
		}else{
			System.out.println("verification failed");
		}
	}

}
