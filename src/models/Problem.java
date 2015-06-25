package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import api.ChatAPI;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

public class Problem {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public String currentPhase;
	public long phaseStartedAt;
	
	public String uuid;
	public String title;
	public String preQuestion;
	public int preTime;
	public String question;
	public int qTime;
	public String postQuestion;
	public int postTime;
	public String solution;
	public long lastEdit;
	public String commentAlgorithm;
	public int cTime;
	public int problemRun;
	public int chatTime;
	
	private Entity e;

	
	
	public Problem(){
		uuid = UUID.randomUUID().toString();
		e = new Entity(KeyFactory.createKey("Problem", uuid));
		currentPhase = "waiting";
		phaseStartedAt = System.currentTimeMillis();
		problemRun = 1;
	}
	
	public Problem(String uuid) throws EntityNotFoundException{
		this(datastore.get(KeyFactory.createKey("Problem", uuid)));	
	}
	
	public Problem(Entity e){
		this.e = e;
		uuid = (String) e.getProperty("uuid");
		title = (String) e.getProperty("title");
		preQuestion = ((Text) e.getProperty("preQuestion")).getValue();
		question = ((Text) e.getProperty("question")).getValue();
		postQuestion = ((Text) e.getProperty("postQuestion")).getValue();
		solution = ((Text) e.getProperty("solution")).getValue();
		preTime = ((Long) e.getProperty("preTime")).intValue();
		qTime = ((Long) e.getProperty("qTime")).intValue();
		postTime = ((Long) e.getProperty("postTime")).intValue();
		lastEdit = (Long) e.getProperty("lastEdit");
		commentAlgorithm = (String) e.getProperty("commentAlgorithm");
		cTime = ((Long) e.getProperty("cTime")).intValue();
		currentPhase = (String) e.getProperty("currentPhase");
		phaseStartedAt = (Long) e.getProperty("phaseStartedAt");
		problemRun = ((Long) e.getProperty("problemRun")).intValue();
		chatTime = ((Long) e.getProperty("chatTime")).intValue();
	}
	
	public void save(){
		e.setUnindexedProperty("uuid", uuid);
		e.setUnindexedProperty("title", title);
		e.setProperty("lastEdit", System.currentTimeMillis());
		e.setUnindexedProperty("preQuestion", new Text(preQuestion));
		e.setUnindexedProperty("question", new Text(question));
		e.setUnindexedProperty("postQuestion", new Text(postQuestion));
		e.setUnindexedProperty("solution", new Text(solution));
		e.setUnindexedProperty("preTime", new Long(preTime));
		e.setUnindexedProperty("postTime", new Long(postTime));
		e.setUnindexedProperty("qTime", new Long(qTime));
		e.setUnindexedProperty("cTime", new Long(cTime));
		e.setUnindexedProperty("commentAlgorithm", commentAlgorithm);
		e.setUnindexedProperty("currentPhase", currentPhase);
		e.setUnindexedProperty("phaseStartedAt", phaseStartedAt);
		e.setUnindexedProperty("problemRun", new Long(problemRun));
		e.setUnindexedProperty("chatTime", new Long(chatTime));
		datastore.put(e);
	}
	
	public String getUuid(){
		return uuid;
	}

	public String getTitle(){
		return title;
	}
	
	public String getPreQuestion(){
		return preQuestion;
	}
	
	public String getQuestion(){
		return question;
	}
	
	public String getPostQuestion(){
		return postQuestion;
	}
	
	public String getSolution(){
		return solution;
	}
	
	public String getLastEdit(){
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm MM/dd/YYYY");
		Date date = new Date(lastEdit);
		return sdf.format(date);
	}
	
	public int getPreTime(){
		return preTime;
	}
	
	public int getqTime(){
		return qTime;
	}
	
	public int getPostTime(){
		return postTime;
	}
	
	public String getCommentAlgorithm(){
		return commentAlgorithm;
	}
	
	public int getcTime(){
		return cTime;
	}
	
	public String getCurrentPhase(){
		return currentPhase;
	}
	
	public long getPhaseStartedAt(){
		return phaseStartedAt;
	}
	
	public int getProblemRun(){
		return problemRun;
	}
	
	public int getChatTime(){
		return chatTime;
	}
	
	public int getSecondsSincePhaseStarted(){
		return (int) (System.currentTimeMillis() - phaseStartedAt) / 1000;
	}
	
	public int getSecondsLeftInPhase(){
		int secondsInPhase = 0;
		if (getCurrentPhase().equals("pre")){
			secondsInPhase = getPreTime();
		} else if (getCurrentPhase().equals("question")){
			secondsInPhase = getqTime();
		} else if (getCurrentPhase().equals("comment")){
			secondsInPhase = getcTime();
		} else if (getCurrentPhase().equals("chat")){
			secondsInPhase = getChatTime();
		} else if (getCurrentPhase().equals("post")){
			secondsInPhase = getPostTime();
		}
		if (secondsInPhase == 0){
			return -1; // Indicates that this phase does not have a timer.
		} else {
			return secondsInPhase - getSecondsSincePhaseStarted();
		}
	}
	
	public List<String> getPhases(){
		List<String> phases = new ArrayList<String>();
		phases.add("waiting");
		if (preQuestion.length() > 0) phases.add("pre");
		if (question.length() > 0) phases.add("question");
		if (!commentAlgorithm.equals("none")) phases.add("comment");
		if (chatTime > 0) phases.add("chat");
		if (solution.length() > 0) phases.add("solution");
		if (postQuestion.length() > 0) phases.add("post");
		phases.add("done");
		return phases;
	}
	
	public String getNextPhase(){
		return getPhaseAfter(this.currentPhase);
	}
	
	public String getPhaseAfter(String phase){
		List<String> phases = getPhases();
		int location = phases.indexOf(phase);
		return phases.get((location + 1)%phases.size());
	}
	
	public void advance(){
		this.currentPhase = getPhaseAfter(this.currentPhase);
		if (currentPhase.equals("waiting")){
			problemRun++;
		}
		if (currentPhase.equals("chat")){
			ChatAPI.initializeChatPhase();
		}
		this.phaseStartedAt = System.currentTimeMillis();
		this.save();
	}
}
