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
		
		


}
