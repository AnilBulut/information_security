import java.io.File;


public class integrity {
	public static void main(String[] args) {
		//start: start -p /home/eren/workspace/FileIntegrity/MonitoredFolder -r /home/eren/workspace/FileIntegrity/registry -l /home/eren/workspace/FileIntegrity/log -h MD5 -k /home/eren/workspace/FileIntegrity/private_key_pkcs8.pem /home/eren/workspace/FileIntegrity/public_key.pem -i 10
		//check: -p /home/eren/workspace/FileIntegrity/MonitoredFolder -r /home/eren/workspace/FileIntegrity/registry -l /home/eren/workspace/FileIntegrity/log -h MD5 -k /home/eren/workspace/FileIntegrity/public_key.pem
		Parser parser=new Parser();
		parser.parseCommand(args);
		if(parser.getCommandMap().get("state").equals("start")){
			File monitoredFolder=new File(parser.getCommandMap().get("-p"));
			File registryFile=new File(parser.getCommandMap().get("-r"));
			File logFile=new File(parser.getCommandMap().get("-l"));
			String hashFunction=parser.getCommandMap().get("-h");
			String[] keyFilesPath=parser.getCommandMap().get("-k").split(" ");
			File publicKeyFile=new File(keyFilesPath[1]);
			File privateKeyFile=new File(keyFilesPath[0]);
			String intervalTime=parser.getCommandMap().get("-i");
			Start start=new Start(monitoredFolder, registryFile, logFile, publicKeyFile, privateKeyFile, hashFunction, intervalTime);
			start.addPeriodicalTask(parser.getCommandMap());
		}
		else if(parser.getCommandMap().get("state").equals("check")){
			File monitoredFolder=new File(parser.getCommandMap().get("-p"));
			File registryFile=new File(parser.getCommandMap().get("-r"));
			File logFile=new File(parser.getCommandMap().get("-l"));
			File publicKeyFile=new File(parser.getCommandMap().get("-k"));
			String hashFunction=parser.getCommandMap().get("-h");
			Check check=new Check(monitoredFolder, registryFile, hashFunction, logFile, publicKeyFile);
			//check.printMap();
		}
		else if(parser.getCommandMap().get("state").equals("stop")){
			Start s=new Start();
			s.removePeriodicalTask();
		}
	}

}
