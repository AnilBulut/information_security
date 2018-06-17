import java.io.File;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Parser parser = new Parser();
		//parser.parseCommand(args[0]);
		parser.parseCommand("FileCrypt -e -p 4 -i document2.txt -o document2.encrypted AES CTR key_file.txt");
		parser.openAndParseKeyFile(parser.getKeyFile());
        
        File inputFile = new File(parser.getInputFile());
        File outputFile = new File(parser.getOutputFile());
        
        long startTime=0;
        long endTime=0;
        long duration=0;
        
        AbstractCryptoFactory acf=AbstractCryptoFactory.getCryptoFactory(parser.getAlgorithm()).getCryptor(parser.getMode());
        acf.setNumberOfThreads(parser.getNumberOfThread());
        if(parser.getType().equals("encryption")){
        	startTime = System.nanoTime();
        	acf.encrypt(parser.getKey(), parser.getVector(), inputFile, outputFile);
        	endTime = System.nanoTime();
        	duration = (endTime - startTime)/1000000;
        	System.out.println(parser.getType()+" time: "+duration+" milliseconds");
        }
        else if(parser.getType().equals("decryption")){
        	startTime = System.nanoTime();
        	acf.decrypt(parser.getKey(), parser.getVector(), inputFile, outputFile);
        	endTime = System.nanoTime();
        	duration = (endTime - startTime)/1000000;
        	System.out.println(parser.getType()+" time: "+duration+" milliseconds");
        }
        Log log = new Log();
        log.createLog(parser.getInputFile(), parser.getOutputFile(), parser.getType(), parser.getAlgorithm(), parser.getMode(), duration);
		
		
		
		//String key = "Bar12345Bar12345"; // 128 bit key
		//String initializationVector = "RandomInitVector"; // 16 bytes IV
		
		//String key2="12345678";
		//String initializationVector2 = "RandomIn";
		
		//String algorithm="AES";
		//String mode="CTR";

		//File inputFile = new File(parser.getInputFile());
		//File encryptedFile = new File("document2.encrypted");
		//File decryptedFile = new File("document2.decrypted");
        
		//AbstractCryptoFactory acf=AbstractCryptoFactory.getCryptoFactory(algorithm).getCryptor(mode);
		//long startTime = System.nanoTime();
		//acf.encrypt(key, initializationVector, inputFile, encryptedFile);
		//long endTime = System.nanoTime();
		//long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
		//System.out.println("enc duration: "+duration);
		
		
		//startTime=System.nanoTime();
		//acf.decrypt(key, initializationVector, encryptedFile,decryptedFile);
		//endTime=System.nanoTime();
		//duration=(endTime-startTime)/1000000;
		//System.out.println("dec duration: "+duration);
	}

}
