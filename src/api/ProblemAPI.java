package api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import tools.UuidTools;
import models.Problem;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

public class ProblemAPI {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private static int currentProblemUpdateInterval = 10000;
	private static long currentProblemLastCheck = 0L;
	private static String currentProblem = null;
	private static int currentProblemRun = -1;
	
	
	/**
	 * Attempts to update (or create) a problem from a Servlet Request. If
	 * no such update is possible it returns false.
	 * @param req A Request to update a problem definition
	 * @return Whether or not the operation was completed successfully.
	 */
	public static boolean updateProblemFromRequest(HttpServletRequest req){
		
		// Create a new problem (with a new UUID) if a UUID is not specified by the request.
		Problem p = new Problem();
		
		if (req.getParameter("uuid") != null && req.getParameter("uuid").length() > 0){
			String uuid = UuidTools.parseUuidFromUrl((String) req.getParameter("uuid"));
			if (uuid != null){
				try {
					p = new Problem(uuid);
				} catch (EntityNotFoundException e) {
					return false; // Simply return if the problem is not found.
				}
			} else {
				return false;
			}
		}
		
		p.title = req.getParameter("title");
		p.preQuestion = req.getParameter("preQuestion");
		p.question = req.getParameter("question");
		p.postQuestion = req.getParameter("postQuestion");
		p.solution = req.getParameter("solution");
		p.preTime = safeParseInt(req.getParameter("preTime"));
		p.questionTime = safeParseInt(req.getParameter("questionTime"));
		p.postTime = safeParseInt(req.getParameter("postTime"));
		p.commentAlgorithm = req.getParameter("commentAlgorithm");
		p.commentTime = safeParseInt(req.getParameter("commentTime"));
		p.chatTime = safeParseInt(req.getParameter("chatTime"));
		
		p.save();
		return true;
	}
	
	/**
	 * Returns a list of all problems defined in the database, 
	 * in descending order of when the were last edited.
	 */
	public static List<Problem> getAllProblems(){
		List<Problem> results = new ArrayList<Problem>();
		Query q = new Query("Problem").addSort("lastEdit", SortDirection.DESCENDING);
		PreparedQuery pq = datastore.prepare(q);
		for (Entity e : pq.asIterable()){
			results.add(new Problem(e));
		}
		return results;
	}

	/**
	 * Deletes a problem from the database
	 */
	public static void deleteProblem(String uuid) {
		datastore.delete(KeyFactory.createKey("Problem", uuid));
	}

	// Utility method that parses and int and defaults to zero.
	private static int safeParseInt(String s){
		try{
			return Integer.parseInt(s);
		} catch (java.lang.NumberFormatException nfe){
			return 0;
		}
	}

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
	private static void checkForUpdateToCurrentProblem(){
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
			Problem p = new Problem(e);
			
			currentProblem = p.getUuid();
			currentProblemRun = p.getProblemRun();
			currentProblemLastCheck = System.currentTimeMillis();
		}
	}
}
