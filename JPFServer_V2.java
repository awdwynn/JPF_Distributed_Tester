package jpfServer_V2;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class JPFServer_V2 {	
	
	public static void main (String args[]) throws Exception
	{
		int connectedClients = 0;
		Hashtable clientList = new Hashtable();
		HashSet pathList = new HashSet();
		clientList.clear();
		pathList.clear();
		int expectedClients = Integer.parseInt(args[1]);
		execTree pathTree = new execTree(expectedClients);
		HashMap[] resultArray = new HashMap[expectedClients];
		String[][] errorArray = new String[expectedClients][];
		
		
		
		if (args.length != 2){
			System.out.println("Run Program as\n \t java JPFServer_V2 <serverport>");
			System.exit(-1);
		}
		ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
		Thread[] threads = new Thread[expectedClients];
		while(connectedClients < expectedClients){
			Socket connectionSocket = welcomeSocket.accept();
			connectedClients++;
			String assignedName = "Client_"+connectedClients;
			Thread newThread;
			threads[connectedClients-1] = new Thread(new serverThread(connectionSocket, expectedClients, clientList, pathList, assignedName, pathTree, resultArray, errorArray),assignedName);
			threads[connectedClients-1].start();
		}
		
		for (int i = 0; i < expectedClients; i++){
			threads[i].join();
		}
		
		for (int i = 0; i < expectedClients; i++){
			System.out.println("Client_"+(i+1));
			showResults(resultArray[i], errorArray[i]);
		}
		System.out.println("Consolidated  (min/max)");
		showTotalStats(resultArray, pathTree);
		
	}
	
	public static void showResults(HashMap results, String[] errors){
		System.out.println("====================================================== results");
		for(String temp : errors){
			System.out.println(temp);
		}
		System.out.println("====================================================== statistics");
		System.out.println("elapsed time:       " + results.get("time"));
		System.out.println("states:             new=" + results.get("newStates") + ",visited=" + results.get("visitedStates")
		     + ",backtracked=" + results.get("backtracked") + ",end=" + results.get("endStates"));
		System.out.println("search:             maxDepth=" + results.get("maxDepth") + ",constraints=" + results.get("constraints"));
		System.out.println("choice generators:  thread=" + results.get("threadCGs")
		     + " (signal=" + results.get("signalCGs") + ",lock=" + results.get("monitorCGs") + ",sharedRef=" + results.get("sharedAccessCGs")
		     + ",threadApi=" + results.get("threadApiCGs") + ",reschedule=" + results.get("breakTransitionCGs")
		     + "), data=" + results.get("dataCGs"));
		System.out.println("heap:               " + "new=" + results.get("nNewObjects")
		     + ",released=" + results.get("nReleasedObjects")
		     + ",maxLive=" + results.get("maxLiveObjects")
		     + ",gcCycles=" + results.get("gcCycles"));
		System.out.println("instructions:       " + results.get("insns"));
		System.out.println("max memory:         " + (Integer.parseInt((String) results.get("maxUsed")) >> 20) + "MB");
		System.out.println("loaded code:        classes=" + results.get("classes") + ",methods="
            + results.get("methods")+"\n");
	    
	}
	
	public static void showTotalStats(HashMap[] resultArray, execTree pathTree){
		int arSize = resultArray.length;
		String minVal = null, maxVal = null, tempVal = null;
		System.out.println("====================================================== statistics");
		for(int i = 0; i<arSize; i++){
			tempVal = (String) resultArray[i].get("time");
			if (i==0){
				minVal = tempVal;
				maxVal = tempVal;
			} else{
				if(minVal.compareTo(tempVal)>0){
					minVal = tempVal;
				}
				if(minVal.compareTo(tempVal)<0){
					maxVal = tempVal;
				}
			}
		}
		System.out.println("elapsed time:      " + minVal+"/"+maxVal);		
		System.out.println("NodeSeq:"+pathTree.nodeSeq);
		System.out.println("total states:       new="+pathTree.runStat.getNew() +" visited="+pathTree.runStat.getVisited() +" backtracked="+pathTree.backTracked+" end="+pathTree.runStat.getEnd());
		System.out.print("states:             new=");
		int tempInt = 0, minInt = 0, maxInt = 0;
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("newStates"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print( minInt+"/"+maxInt + ",visited=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("visitedStates"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt+ ",backtracked=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("backtracked"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",end=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("endStates"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.println(minInt+"/"+maxInt);
		
		System.out.print("search:             maxDepth=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("maxDepth"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt+ ",constraints=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("constraints"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.println(minInt+"/"+maxInt);
		System.out.print("choice generators:  thread=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("threadCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt+ " (signal=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("signalCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+ maxInt + ",lock=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("monitorCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",sharedRef=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("sharedAccessCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",threadApi=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("threadApiCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",reschedule=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("breakTransitionCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minVal+"/"+maxVal+ "), data=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("dataCGs"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.println(minInt+"/"+maxInt);
		System.out.print("heap:               " + "new=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("nNewObjects"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",released=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("nReleasedObjects"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",maxLive=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("maxLiveObjects"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt + ",gcCycles=");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("gcCycles"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.print(minInt+"/"+maxInt);
		
		System.out.print("instructions:       ");
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("insns"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.println(minInt+"/"+maxInt);
		System.out.print("max memory:         " );
		for(int i = 0; i<arSize; i++){
			tempInt = Integer.parseInt((String) resultArray[i].get("maxUsed"));
			if (i==0){
				minInt = tempInt;
				maxInt = tempInt;
			} else{
				if(tempInt < minInt){
					minInt = tempInt;
				}
				if(tempInt>maxInt){
					maxInt = tempInt;
				}
			}
		}
		System.out.println((minInt >> 20) +"MB/"+ (maxInt >> 20)+"MB");
		
	}
	
}

class serverThread implements Runnable
{
	
	private String msgString;
	private String clientName;
	private Socket clientSocket;
	Thread myThread;
	Hashtable clients;
	HashSet paths;
	int connectedClients = 0;
	int stateCount = 0;
	execTree pathSet;
	int expectedClients;
	HashMap[] resultArray;
	String[][] errorArray;
	
	public serverThread (Socket sock, int expected, Hashtable clientList, HashSet pathList, String assignedName, execTree pathTree, HashMap[] resultSet, String[][] errorSet)
	{
		clientSocket = sock;
		clients = clientList;
		paths = pathList;
		expectedClients = expected;
		clientName = assignedName;
		pathSet = pathTree;
		this.resultArray = resultSet;
		this.errorArray = errorSet;
		System.out.println("serverThread Created");
		
	}
	
	public void start(){
		myThread = new Thread(this, clientName);
		myThread.start();
	}
	
	public void run()
	{
		boolean stop = false;
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			int clientNum = Integer.parseInt(clientName.substring(clientName.length()-1))-1;
			System.out.println("ClientNum:"+clientNum);
			while ((msgString = inFromClient.readLine()) != null){
				String[] parsedString = msgString.split(" ");
		        switch(parsedString[0]){
		        case "HELO":
		        	if(!clients.containsKey(clientName+":"+parsedString[1].trim())){
		        		System.out.println("Adding Client: |"+parsedString[1]+"|\n");
		        		clients.put(clientName, clientName+":"+parsedString[1].trim());
		        		String acptMsg = "ACPT "+clientName+"\n";
		        		outToClient.writeBytes(acptMsg);
		        		while (clients.size() < expectedClients){
		        			Thread.sleep(10);
		        		}
		        		outToClient.writeBytes("STRT\n");
		        	}else{
		        		System.out.println("Reject Client");
		        		String rjctMsg = "RJCT "+parsedString[1].trim()+"\n";
		        		System.out.println("Reject MSG:"+rjctMsg);
		        		outToClient.writeBytes(rjctMsg);
		        	}
		        	
		        	break;
		        case "ADVC":
		        	String advanceString = parsedString[1].substring(1, parsedString[1].length()-1);
		        	String[] advanceParsed = advanceString.split(";");
		        	boolean cont = false;
		        	String id = advanceParsed[0];
		        	int last = Integer.parseInt(advanceParsed[1]);
		        	int processed = Integer.parseInt(advanceParsed[2]);
		        	int possible = Integer.parseInt(advanceParsed[3]);
		        	boolean isCascaded =  Boolean.parseBoolean(advanceParsed[4]);
		        	boolean isSkipped =  Boolean.parseBoolean(advanceParsed[5]);
		        	boolean isNew = Boolean.parseBoolean(advanceParsed[7]);
		        	boolean isVisited = Boolean.parseBoolean(advanceParsed[8]);
		        	boolean isEnd = Boolean.parseBoolean(advanceParsed[9]);
		        	int stateHash = Integer.parseInt(advanceParsed[10]);
		        	cont = pathSet.advance(clientNum,id, last, processed, possible, isCascaded,isSkipped,isNew,isEnd,isVisited,stateHash);
		        	if(cont == false){
		        		outToClient.writeBytes("CONT\n");
		        		stateCount++;
		        	} else{
		        		outToClient.writeBytes("SKIP\n");
		        	}
		        	break;
		        case "FRWD":
		        	String forwardString = parsedString[1].substring(1, parsedString[1].length()-1);
		        	String[] forwardParsed = forwardString.split(";");
		        	cont = false;
		        	id = forwardParsed[0];
		        	last = Integer.parseInt(forwardParsed[1]);
		        	processed = Integer.parseInt(forwardParsed[2]);
		        	possible = Integer.parseInt(forwardParsed[3]);
		        	isCascaded =  Boolean.parseBoolean(forwardParsed[4]);
		        	isSkipped =  Boolean.parseBoolean(forwardParsed[5]);
		        	cont = pathSet.forward(clientNum,id, last, processed, possible, isCascaded,isSkipped);
		        	if(cont == false){
		        		outToClient.writeBytes("CONT\n");
		        	} else{
		        		outToClient.writeBytes("SKIP\n");
		        	}
		        	break;
		        case "BTRK":
		        	pathSet.back(clientNum);
		        	outToClient.writeBytes("CONT\n");
		        	break;
		        case "EXIT":
		        	stop = true;
		        	String exitMSG = "BYE!\n";
		        	clients.remove(clientName);
		        	outToClient.writeBytes(exitMSG);
		        	String resultString = msgString.substring(5);
		        	parseResults(resultString, clientNum);
		        	System.out.println("StateCount: "+stateCount);
		        	clientSocket.close();
		        	break;
		        }
		        if (stop){
					break;
				}
			}
			
		}
		catch (IOException e){
			System.out.println("Socket problem");
			System.out.println(e);
		} /*catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/ catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public void parseResults (String results, int client){
		String errorString = results.substring(1,results.indexOf("]"));
		String[] errorSplit = errorString.split(";");
		errorArray[client] = new String[errorSplit.length];
		for (int i = 0; i < errorSplit.length; i++){
			if(errorSplit[i].split("!").length == 3){
				errorArray[client][i] = "error #"+errorSplit[i].split("!")[0]+":"
					+errorSplit[i].split("!")[1]+errorSplit[i].split("!")[2];
				errorArray[client][i] = errorArray[client][i].replace("<br>", "\n");
			} else{
				errorArray[client][i] = "no errors detected";
			}
		}
		String statsString = results.substring(results.indexOf("]")+2,results.length()-1);
		String[] statSplit = statsString.split(";");
		resultArray[client]=new HashMap();
		resultArray[client].put("time", statSplit[0]);
		resultArray[client].put("newStates", statSplit[1]);
		resultArray[client].put("visitedStates", statSplit[2]);
		resultArray[client].put("backtracked", statSplit[3]);
		resultArray[client].put("endStates",statSplit[4]);
		resultArray[client].put("maxDepth", statSplit[5]);
		resultArray[client].put("constraints", statSplit[6]);
		resultArray[client].put("threadCGs", statSplit[7]);
		resultArray[client].put("signalCGs", statSplit[8]);
		resultArray[client].put("monitorCGs", statSplit[9]);
		resultArray[client].put("sharedAccessCGs", statSplit[10]);
		resultArray[client].put("threadApiCGs", statSplit[11]);
		resultArray[client].put("breakTransitionCGs", statSplit[12]);
		resultArray[client].put("dataCGs", statSplit[13]);
		resultArray[client].put("nNewObjects", statSplit[14]);
		resultArray[client].put("nReleasedObjects", statSplit[15]);
		resultArray[client].put("maxLiveObjects", statSplit[16]);
		resultArray[client].put("gcCycles", statSplit[17]);
		resultArray[client].put("insns", statSplit[18]);
		resultArray[client].put("maxUsed", statSplit[19]);
		resultArray[client].put("classes", statSplit[20]);
		resultArray[client].put("methods", statSplit[21]);
	}
	
	
}

class choiceNode{
	
	String id;
	int nodeID = 0;
	int processed;
	int possible;
	boolean isCascaded;
	choiceNode[] choice;
	choiceNode previous;
	int skip = 0;
	
	choiceNode (String id, int nodeID, int processed, int possible, boolean isCascaded, choiceNode previous){
		this.id = id;
		this.nodeID = nodeID;
		this.processed = processed;
		this.possible = possible;
		this.isCascaded = isCascaded;
		this.choice = new choiceNode[possible];
		this.previous = previous;
		
	}
}

class execTree{
	choiceNode root;
	choiceNode[] clientPositions;
	//choiceNode lastStep;
	int nodeSeq = 0;
	int[] depth;
	int newStates = 0;
	int backTracked = 0;
	Semaphore read = new Semaphore(1);
	List<graphEdge> edgeList = new ArrayList<graphEdge>();
	int stateCounter = 0;
	runStatistics runStat = new runStatistics();
	HashSet<Integer> stateSet = new HashSet();
	
	execTree(int clientCount){
		this.root = new choiceNode("ROOT",0,1,1,false,null);
		clientPositions = new choiceNode[clientCount];
		depth = new int[clientCount];
		for(int i = 0; i<clientCount; i++){
			clientPositions[i]=root;
			depth[i]= 0;
		}
		
	}
	
	public boolean advance(int client, String id,int last, int processed, int possible, boolean isCascaded, boolean skipped, boolean isNew, boolean isVisited, boolean isEnd,int stateHash){
		System.out.println("Client:"+client+" advanced");
		boolean result = false;
		if(!skipped){
			try {
				this.read.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		stateSet.add(stateHash);
		
		if(!id.equalsIgnoreCase("ROOT")){
			System.out.println("client:"+client+" last:"+last+" size:"+clientPositions[client].choice.length+
					" new_id:"+id+" currentID:"+clientPositions[client].id);
			if (clientPositions[client].choice[last-1]!= null){
				clientPositions[client] = clientPositions[client].choice[last-1];
				if(clientPositions[client].processed < clientPositions[client].possible){
					if(processed+clientPositions[client].skip <= clientPositions[client].processed){
						clientPositions[client].skip++;
						clientPositions[client] = clientPositions[client].previous;
						result = true;
					}else{
						if(clientPositions[client].processed < processed){
							clientPositions[client].processed = processed;
							if(skipped){
								clientPositions[client].skip--;
								//System.out.println("Skip dec:"+clientPositions[client].skip);
							}
						}
						
						
					}
				}else{
					clientPositions[client].processed = processed;
					
				}
			}
			else{
				this.nodeSeq++;
				clientPositions[client].choice[last-1] = new choiceNode(id, this.nodeSeq, processed, possible, isCascaded, clientPositions[client]);
				newStates++;
				clientPositions[client] = clientPositions[client].choice[last-1]; 
				
			}
		} 
		if (!result){
			this.read.release();
			if(!id.equalsIgnoreCase("ROOT")){
				this.edgeList.add(new graphEdge(client, clientPositions[client].previous.nodeID,clientPositions[client].nodeID)  );
			}
			depth[client]++;
			if(isNew){
				try {
					runStat.addNew();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(isVisited){
				try {
					runStat.addVisited();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(isEnd){
				try {
					runStat.addEnd();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if (result){
			stateCounter++;
			
		}
		return result;
	}
	
	public boolean forward(int client, String id,int last, int processed, int possible, boolean isCascaded, boolean skipped){
		boolean result = false;
		if(!id.equalsIgnoreCase("ROOT")){
			clientPositions[client] = clientPositions[client].choice[last-1];
		}
		return result;
	}
	
	public void back(int client){
		System.out.println("Client:"+client+" advanced");
		if( !clientPositions[client].id.equalsIgnoreCase("ROOT")){
			this.edgeList.add(new graphEdge(client, clientPositions[client].nodeID,clientPositions[client].previous.nodeID)  );
			clientPositions[client] = clientPositions[client].previous;
			depth[client]--;
			backTracked++;			
		}
	}
	
	public String toString(int client){
		String result = "";
		
		choiceNode printNode = this.root;
		
		//while(printNode != null){
		for(int i = 1; i<=depth[client]; i++){
			if(printNode != null){
				result = result+"Step_"+i+"{";
				result = result+"id:"+printNode.id+", ";
				result = result+"choices:"+printNode.processed+"/"+printNode.possible+", ";
				result = result+"isCascaded:"+printNode.isCascaded+"}\n";
				printNode = printNode.choice[printNode.processed-1];
			}
		}
		
		
		return result;
	}
}

class graphEdge{
	int client;
	int start;
	int end;
	
	graphEdge(int client,int start, int end){
		this.client = client;
		this.start = start;
		this.end = end;
	}
	
	public String toString(){
		return ("<"+this.client+";"+this.start+";"+this.end+">");
	}
}

class runStatistics{
	private int endCount;
	private int newCount;
	private int visitedCount;
	private Semaphore read = new Semaphore(1);
	
	runStatistics(){
		newCount = 0;
		visitedCount = 0;
		endCount = 0;
	}
	
	void addNew() throws InterruptedException{
		read.acquire();
		newCount++;
		read.release();
	}
	
	void addEnd() throws InterruptedException{
		read.acquire();
		endCount++;
		read.release();
	}
	
	void addVisited() throws InterruptedException{
		read.acquire();
		visitedCount++;
		read.release();
	}
	
	int getNew(){
		return newCount;
	}
	
	int getEnd(){
		return endCount;
	}
	
	int getVisited(){
		return visitedCount;
	}
	
}



