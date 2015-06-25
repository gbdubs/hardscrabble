package api;

import models.Problem;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

public class CurrentAPI {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	
	private static int currentProblemUpdateInterval = 10000;
	private static long currentProblemLastCheck = 0L;
	private static String currentProblem = null;
	private static int currentProblemRun = -1;
	
	
	// Sets the current problem to the specified UUID
	public static void setCurrentProblem(String uuid){
		Entity e = new Entity(KeyFactory.createKey("Current", "Problem"));
		e.setProperty("uuid", uuid);
		datastore.put(e);
	}

	// See checkForUpdateToCurrentProblem()
	public static String getCurrentProblem(){
		checkForUpdateToCurrentProblem();
		return currentProblem;
	}

	// See checkForUpdateToCurrentProblem()
	public static int getCurrentProblemRun() {
		checkForUpdateToCurrentProblem();
		return currentProblemRun;
	}

	/*
	 * Since we store the definitions for currentProblem and currentProblemRun in the
	 * instantiated class (to prevent repetitive calls to read it), we need to make sure
	 * that it does not get stale.  This checks for an update and only updates once per
	 * ten seconds.  This is a solid way of avoiding hundreds of database reads.
	 */
	static void checkForUpdateToCurrentProblem(){
		if (currentProblemLastCheck == 0L || 
			currentProblem == null ||
			currentProblemRun == -1 ||
			System.currentTimeMillis() - currentProblemLastCheck < currentProblemUpdateInterval){
			
			
			Entity e;
			try {
				e = datastore.get(KeyFactory.createKey("Current", "Problem"));
			} catch (EntityNotFoundException e1) {
				return;
			}
			String uuid = (String) e.getProperty("uuid");
			
			Problem p;
			try {
				p = new Problem(uuid);
			} catch (EntityNotFoundException e1) {
				return;
			}
			
			currentProblem = p.getUuid();
			currentProblemRun = p.getProblemRun();
			currentProblemLastCheck = System.currentTimeMillis();
		}
	}
}
