package api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
	
	public static void updateProblemFromRequest(HttpServletRequest req){
		
		// Create a new problem if the UUID is not specified.
		Problem p = new Problem();
		if (req.getParameter("uuid") != null && req.getParameter("uuid").length() > 0){
			try {
				p = new Problem((String) req.getParameter("uuid"));
			} catch (EntityNotFoundException e) {
				return; // Simply return if the problem is not found.
			}
		}
		
		p.title = req.getParameter("title");
		p.preQuestion = req.getParameter("preQuestion");
		p.question = req.getParameter("question");
		p.postQuestion = req.getParameter("postQuestion");
		p.solution = req.getParameter("solution");
		p.preTime = safeParseInt(req.getParameter("preTime"));
		p.qTime = safeParseInt(req.getParameter("qTime"));
		p.postTime = safeParseInt(req.getParameter("postTime"));
		p.commentAlgorithm = req.getParameter("commentAlgorithm");
		p.cTime = safeParseInt(req.getParameter("cTime"));
		p.chatTime = safeParseInt(req.getParameter("chatTime"));
		
		p.save();
	}
	
	public static List<Problem> getAllProblems(){
		List<Problem> results = new ArrayList<Problem>();
		Query q = new Query("Problem").addSort("lastEdit", SortDirection.DESCENDING);
		PreparedQuery pq = datastore.prepare(q);
		for (Entity e : pq.asIterable()){
			results.add(new Problem(e));
		}
		return results;
	}

	public static void deleteProblem(String uuid) {
		datastore.delete(KeyFactory.createKey("Problem", uuid));
	}

	public static int safeParseInt(String s){
		try{
			return Integer.parseInt(s);
		} catch (java.lang.NumberFormatException nfe){
			return 0;
		}
	}
	
	public static void setCurrentProblem(String uuid){
		Entity e = new Entity(KeyFactory.createKey("Current", "Problem"));
		e.setProperty("uuid", uuid);
		datastore.put(e);
	}
	
	public static String getCurrentProblem(){
		Entity e;
		try {
			e = datastore.get(KeyFactory.createKey("Current", "Problem"));
		} catch (EntityNotFoundException e1) {
			return null;
		}
		return (String) e.getProperty("uuid");
	}

	public static int getCurrentProblemRun() {
		Problem p;
		try {
			p = new Problem(getCurrentProblem());
		} catch (EntityNotFoundException e) {
			return -1;
		}
		return p.problemRun;
	}
}
