package gov.nasa.jpf.listener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.annotation.JPFOptions;
import gov.nasa.jpf.search.*;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.report.*;

@JPFOptions({
	  @JPFOption(type = "String", key = "distributedTester.ServerIP", defaultValue= "127.0.0.1", comment = "IP address of JPF server instance"),
	  @JPFOption(type = "String", key = "distributedTester.TCPPort", defaultValue = "1492", comment = "Server TCP connection port"),
	  
	})

public class DistributedTester_V2 extends PropertyListenerAdapter {

	String ServerIP;
	String ClientIP;
	String ClientName;
	int TCPPort;
	Socket connectionSocket;
	int Test = 0;
	int Depth = 0;
	Date start, finish;
	Reporter myReporter;
	
	int newCount = 0;
	int visitedCount = 0;
	int endCount = 0;
	
	public DistributedTester_V2 (Config config, JPF jpf) {
		 
		this.ServerIP = config.getString("distributedTester.ServerIP");
		System.out.println("|"+config.getString("distributedTester.TCPPort")+"|");
		String TCPString = config.getString("distributedTester.TCPPort");
		this.TCPPort = Integer.parseInt(TCPString);
		System.out.println("TCPPort:"+TCPPort);
		Search mySearch = jpf.getSearch();
		myReporter = new Reporter(config, jpf);
		mySearch.setReporter(myReporter);
	}
	
	@Override
	public void searchStarted (Search search){
		System.out.println("Search Started");
		System.out.println("ServerIP:"+ServerIP);
		System.out.println("ServerTCPPORT:"+TCPPort);
		
		
		try {
			this.ClientIP = InetAddress.getLocalHost().getHostAddress();
			System.out.println("MyIP:"+InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.connectionSocket = new Socket(ServerIP, TCPPort);
			DataOutputStream outToServer = 
					new DataOutputStream(connectionSocket.getOutputStream()); 
			BufferedReader inFromServer = 
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			
			String sendText = "HELO "+this.ClientIP;
			System.out.println("SendText: "+sendText);
			String returnText;
			outToServer.writeBytes(sendText+"\n"); 
				
			returnText = inFromServer.readLine(); 
			
			String msgType = returnText.substring(0, 4);
			String[] parsedMSG = returnText.split(" ");
			switch(msgType){
				case "ACPT":
					ClientName = parsedMSG[1];
					System.out.println("Connected as:"+ClientName);
					System.out.println("Connected on Port:"+connectionSocket.getLocalPort());
					break;
				case "RJCT":
					System.out.println("Rejected Connection");
					System.exit(0);
					break;
			}
			returnText = inFromServer.readLine();
			msgType = returnText.substring(0, 4);
			switch(msgType){
			case "STRT":
				this.start = new Date();
				System.out.println("Start Time: "+start);
				
				break;
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("stateStart UnknownHost Exception:");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("stateStart IOException:");
			e.printStackTrace();
		}
	}
	
	@Override
	public void stateAdvanced (Search search){
		//System.out.println("stateAdvanced");
		
		
		int step = 0;
		VM vm = search.getVM();
		ChoiceGenerator<?> choice = vm.getChoiceGenerator();
		ChoiceGenerator<?> lastchoice = vm.getChoiceGenerator().getPreviousChoiceGenerator();
		ChoiceGenerator<?>[] execPath = choice.getAll();
		this.Depth++;
		if (search.getDepth()!=this.Depth){
			System.out.println("Search Depth:"+search.getDepth()+" expectedDepth:"+this.Depth);
		}
		while(this.Depth < search.getDepth()){
			this.Depth++;
			String advance = "FRWD {"+lastchoice.getId()+";";
			if (lastchoice.getPreviousChoiceGenerator()!=null){
				advance = advance+lastchoice.getPreviousChoiceGenerator().getProcessedNumberOfChoices()+";";
			} else{
				advance = advance+"0;";
			}
			advance = advance+lastchoice.getProcessedNumberOfChoices()+";";
			advance = advance+lastchoice.getTotalNumberOfChoices()+";";
			advance = advance+lastchoice.isCascaded()+";";
			advance = advance+"false;";
			advance = advance+vm.getThreadName()+";";
			advance = advance+"Test:"+Test+"}\n";
			//System.out.println(advance);
			System.out.println("Test "+Test);
			System.out.println("Depth:"+this.Depth+"/"+search.getDepth());
			Test++;
			try{
				DataOutputStream outToServer = 
					new DataOutputStream(connectionSocket.getOutputStream()); 
				BufferedReader inFromServer = 
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				String returnText;
				outToServer.writeBytes(advance); 
					
				returnText = inFromServer.readLine();
				String[] parsedMSG = returnText.split(" ");
				
				switch(parsedMSG[0]){
				case "CONT":
					break;
				case "SKIP":
					//System.out.println("Skip Recieved");
					choice.advance();
					break;
				}
			}catch (Exception e){
				System.out.println("stateAdvanced Exception:");
				e.printStackTrace();
				System.out.println("Connection Failed");
			}
		}
		
		boolean skip = false;
		do{
			boolean isNew = search.isNewState();
			boolean isEnd = search.isEndState() ;
			boolean isVisited = search.isVisitedState();
			
			if(isNew){
				newCount++;
			}
			if(isEnd){
				endCount++;
			}
			if(isVisited){
				visitedCount++;
			}
			//skip = false;
			String advance = "ADVC {"+choice.getId()+";";
			if (choice.getPreviousChoiceGenerator()!=null){
				advance = advance+choice.getPreviousChoiceGenerator().getProcessedNumberOfChoices()+";";
			} else{
				advance = advance+"0;";
			}
			advance = advance+choice.getProcessedNumberOfChoices()+";";
			advance = advance+choice.getTotalNumberOfChoices()+";";
			advance = advance+choice.isCascaded()+";";
			advance = advance+skip+";";
			advance = advance+vm.getThreadName()+";";
			advance = advance+isNew+";";
			advance = advance+isEnd+";";
			advance = advance+isVisited+";";
			advance = advance+0+";";
			advance = advance+"Test:"+Test+"}\n";
			Test++;
			try{
				DataOutputStream outToServer = 
					new DataOutputStream(connectionSocket.getOutputStream()); 
				BufferedReader inFromServer = 
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				String returnText;
				outToServer.writeBytes(advance); 
					
				returnText = inFromServer.readLine();
				String[] parsedMSG = returnText.split(" ");
				
				switch(parsedMSG[0]){
				case "CONT":
					skip = false;
					break;
				case "SKIP":
					skip = true;
					System.out.println("Skip Recieved");
					choice.advance();
					break;
				}
			}catch (Exception e){
				System.out.println("stateAdvanced Exception:");
				e.printStackTrace();
				System.out.println("Connection Failed");
			}
		}while(skip == true);
		execPath = choice.getAll();
		
	}
	
	@Override
	public void stateBacktracked (Search search){
		boolean isNew = search.isNewState();
		boolean isEnd = search.isEndState() ;
		boolean isVisited = search.isVisitedState();
		
		if(isNew){
			newCount++;
			System.out.println("BackTrackNew");
		}
		if(isEnd){
			endCount++;
		}
		if(isVisited){
			visitedCount++;
		}
		
		this.Depth--;
		if (search.getDepth()!=this.Depth){
			System.out.println("Search Depth:"+search.getDepth()+" expectedDepth:"+this.Depth);
		}
		int step = 0;
		VM vm = search.getVM();
		ChoiceGenerator<?> choice = vm.getChoiceGenerator();
		ChoiceGenerator<?>[] execPath = choice.getAll();
		Test++;
		String back = "BTRK\n";
		try{
			DataOutputStream outToServer = 
				new DataOutputStream(connectionSocket.getOutputStream()); 
			BufferedReader inFromServer = 
				new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			String returnText;
			outToServer.writeBytes(back); 
			returnText = inFromServer.readLine();
			String[] parsedMSG = returnText.split(" ");
			
			switch(parsedMSG[0]){
			case "CONT":
				break;
			case "SKIP":
				break;
			}
		}catch (Exception e){
			System.out.println("stateBackTrack Exception:");
			e.printStackTrace();
			System.out.println("Connection Failed");
		}
	}
	
	@Override
	public void searchFinished (Search search){
		System.out.println("Search Finished "+this.ClientName);
		finish = new Date();
		long t = finish.getTime() - this.start.getTime();
		long second = (t / 1000) % 60;
		long minute = (t / (1000 * 60)) % 60;
		long hour = (t / (1000 * 60 * 60)) % 24;

		String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, t%1000);
		
		Statistics clientStats = myReporter.getStatistics();
		List<gov.nasa.jpf.Error> clientErrors =myReporter.getErrors();
		
		String result = showResults(time, clientStats, clientErrors);
		System.out.println("New: "+newCount+" Visited:"+visitedCount+" End:"+endCount);
		try{
			DataOutputStream outToServer = 
				new DataOutputStream(connectionSocket.getOutputStream()); 
			BufferedReader inFromServer = 
				new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			String sendText = "EXIT "+result+"\n";
			//System.out.println("SendText: "+sendText);
			String returnText;
			outToServer.writeBytes(sendText); 
				
			returnText = inFromServer.readLine();
			String[] parsedMSG = returnText.split(" ");
			
			switch(parsedMSG[0]){
			case "BYE!":
				//System.out.println("Continue Recieved");
				System.out.println("Socket Disconnected");
				System.out.println(this.ClientName);
				break;
			}
			
		}catch (Exception e){
			System.out.println("searchFinished Exception:");
			e.printStackTrace();
			System.out.println("Connection Failed");
			
		}
	}
	
	public String showResults(String time, Statistics stat, List<gov.nasa.jpf.Error> errors){
		String results = "[";
		System.out.println("====================================================== results");
		for(gov.nasa.jpf.Error temp : errors){
			if (temp.getId()>1){
				results = results+";";
			}
			System.out.println("error #"+temp.getId() +": "+temp.getDescription()+temp.getDescription() +temp.getDetails());
			results = results + temp.getId() +"!"+temp.getDescription()+"!"+temp.getDetails().replaceAll("\r\n|\n", "<br>");
		}
		results = results +"][";
		System.out.println("====================================================== statistics");
		System.out.println("elapsed time:       " + time);
		results = results + time +";";
		System.out.println("states:             new=" + stat.newStates + ",visited=" + stat.visitedStates
		     + ",backtracked=" + stat.backtracked + ",end=" + stat.endStates);
		results = results + stat.newStates+";"+stat.visitedStates+";"+stat.backtracked+";"+stat.endStates+";";
		System.out.println("search:             maxDepth=" + stat.maxDepth + ",constraints=" + stat.constraints);
		results = results + stat.maxDepth+";"+stat.constraints+";";
		System.out.println("choice generators:  thread=" + stat.threadCGs
		     + " (signal=" + stat.signalCGs + ",lock=" + stat.monitorCGs + ",sharedRef=" + stat.sharedAccessCGs
		     + ",threadApi=" + stat.threadApiCGs + ",reschedule=" + stat.breakTransitionCGs
		     + "), data=" + stat.dataCGs);
		results = results + stat.threadCGs+";"+stat.signalCGs+";"+stat.monitorCGs+";"+stat.sharedAccessCGs+";"+
		     stat.threadApiCGs+";"+stat.breakTransitionCGs+";"+stat.dataCGs+";";
		System.out.println("heap:               " + "new=" + stat.nNewObjects
		     + ",released=" + stat.nReleasedObjects
		     + ",maxLive=" + stat.maxLiveObjects
		     + ",gcCycles=" + stat.gcCycles);
		results = results + stat.nNewObjects+";"+stat.nReleasedObjects+";"+stat.maxLiveObjects+";"+stat.gcCycles+";";
		System.out.println("instructions:       " + stat.insns);
		results = results + stat.insns +";";
		System.out.println("max memory:         " + (stat.maxUsed >> 20) + "MB");
		results = results + stat.maxUsed+";";
	    System.out.println("loaded code:        classes=" + ClassLoaderInfo.getNumberOfLoadedClasses() + ",methods="
            + MethodInfo.getNumberOfLoadedMethods());
	    results = results + ClassLoaderInfo.getNumberOfLoadedClasses()+";"+MethodInfo.getNumberOfLoadedMethods()+"]";
		return results;  
	}
	
}


