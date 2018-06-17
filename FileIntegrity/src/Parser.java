import java.util.HashMap;
import java.util.Map.Entry;

public class Parser {
	private HashMap<String, String> commandMap;

	public Parser() {
		this.commandMap = new HashMap<>();
	}

	public void parseCommand(String[] args) {
		if (args.length == 14 || args.length == 1) {
			commandMap.put("state", args[0]);
			for (int i = 1; i < args.length - 1; i++) {
				if (args[i].equals("-k")) {
					commandMap.put(args[i], args[i + 1] + " " + args[i + 2]);
					i = i + 2;
				} else {
					commandMap.put(args[i], args[i + 1]);
					i=i+1;
				}
			}
		} else if(args.length==10){
			commandMap.put("state", "check");
			for (int i = 0; i < args.length - 1; i++) {
				commandMap.put(args[i], args[i + 1]);
				i=i+1;
			}
		}
	}

	public void printCommandMap() {
		for (Entry<String, String> entry : commandMap.entrySet()) {
			System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());
		}
	}

	public HashMap<String, String> getCommandMap() {
		return commandMap;
	}

	public void setCommandMap(HashMap<String, String> commandMap) {
		this.commandMap = commandMap;
	}

}
