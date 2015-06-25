package com.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class ResponseAPI {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public static void saveResponse(String problemUuid, String userId, String responseType, String response){
		Key key = KeyFactory.createKey("Response", userId + "|" + problemUuid + "|" + responseType);
		Entity e;
		try {
			e = datastore.get(key);
		} catch (EntityNotFoundException error) {
			e = new Entity(key);
		}
		e.setProperty("userId", userId);
		e.setProperty("problemUuid", problemUuid);
		e.setProperty("responseType", responseType);
		e.setProperty("response", response);
		e.setProperty("problemRun", ProblemAPI.getCurrentProblemRun());
		datastore.put(e);
	}
	
	public static String getPairedQuestionResponse(String problemUuid, String userId){
		return getQuestionResponse(problemUuid, getPairedUserId(problemUuid, userId));
	}
	
	public static String getPairedUserId(String problemUuid, String userId){
		Entity e;
		try {
			e = datastore.get(KeyFactory.createKey("Pairing", problemUuid));
		} catch (EntityNotFoundException enfe) {
			return "[The Requested Pairing did not exist.]";
		}
		
		String pairedUserId = (String) e.getProperty(userId);
		return pairedUserId;
	}

	public static String getPairedCommentResponse(String problemUuid, String userId){
		Entity e;
		try {
			e = datastore.get(KeyFactory.createKey("Pairing", problemUuid));
		} catch (EntityNotFoundException enfe) {
			return "[The Requested Problem did not exist.]";
		}
		
		String pairedUserId = (String) e.getProperty("INVERSE-" + userId);
		return getCommentResponse(problemUuid, pairedUserId);
	}

	public static void constructPairings(String problemUuid){
		Problem p;
		try {
			p = new Problem(problemUuid);
		} catch (EntityNotFoundException e) {
			return;
		}
		
		Query q = new Query("Response");
		Collection<Filter> filters = new ArrayList<Filter>();
		filters.add(new FilterPredicate("problemUuid", FilterOperator.EQUAL, problemUuid));
		filters.add(new FilterPredicate("responseType", FilterOperator.EQUAL, "question"));
		filters.add(new FilterPredicate("problemRun", FilterOperator.EQUAL, ProblemAPI.getCurrentProblemRun()));
		CompositeFilter filter = new CompositeFilter(CompositeFilterOperator.AND, filters);
		q.setFilter(filter);
		
		PreparedQuery pq = datastore.prepare(q);
		Map<String, String> userResponses = new HashMap<String, String>(); 
		for (Entity e : pq.asIterable()){
			userResponses.put((String) e.getProperty("userId"), (String) e.getProperty("response"));
		}
		Map<String, String> userMapping = constructPairingsByAlgorithm(userResponses, p.getCommentAlgorithm());
		saveProblemCommentPairingMapping(problemUuid, userMapping);
	}

	public static String getQuestionResponse(String problemUuid, String userId){
		return getResponse(problemUuid, userId, "question");
	}
	
	public static String getCommentResponse(String problemUuid, String userId){
		return getResponse(problemUuid, userId, "comment");
	}
	
	private static String getResponse(String problemUuid, String userId, String responseType){
		Key key = KeyFactory.createKey("Response", userId + "|" + problemUuid + "|" + responseType);
		try {
			Entity e = datastore.get(key);
			return (String) e.getProperty("response");
		} catch (EntityNotFoundException e) {
			return null;
		}
	}
	
	private static void saveProblemCommentPairingMapping(String problemUuid, Map<String, String> userPairing){
		Entity e = new Entity(KeyFactory.createKey("Pairing", problemUuid));
		for(String user1 : userPairing.keySet()){
			String user2 = userPairing.get(user1);
			e.setProperty(user1, user2);
			e.setProperty("INVERSE-" + user2, user1);
		}
		datastore.put(e);
	}
	
	
	private static Map<String, String> constructPairingsByAlgorithm(Map<String, String> userResponses, String algorithm){
		if (algorithm.equals("random")){
			return randomAlgorithm(userResponses);
		} else if (algorithm.equals("length")){
			return lengthAlgorithm(userResponses);
		} else if (algorithm.equals("edit-distance")){
			return editDistanceAlgorithm(userResponses);
		} else if (algorithm.equals("inv-edit-distance")){
			return invEditDistanceAlgorithm(userResponses);
		} else if (algorithm.equals("inv-length")){
			return invLengthAlgorithm(userResponses);
		}
		return null;
	}

	private static Map<String, String> invLengthAlgorithm(Map<String, String> userResponses) {
		// DUMMY
		return randomAlgorithm(userResponses);
	}

	private static Map<String, String> invEditDistanceAlgorithm(Map<String, String> userResponses) {
		// DUMMY
		return randomAlgorithm(userResponses);
	}

	private static Map<String, String> editDistanceAlgorithm(Map<String, String> userResponses) {
		// DUMMY
		return randomAlgorithm(userResponses);
	}

	private static Map<String, String> lengthAlgorithm(Map<String, String> userResponses) {
		// DUMMY
		return randomAlgorithm(userResponses);
	}

	private static Map<String, String> randomAlgorithm(Map<String, String> userResponses) {
		Map<String, String> resulting = new HashMap<String, String>();
		List<String> userIds = new ArrayList<String>(userResponses.keySet());
		if (userIds.size() == 1){
			resulting.put(userIds.get(0), userIds.get(0));
			return resulting;
		}
		if (userIds.size() % 2 == 1){
			String id1 = userIds.remove(0);
			String id2 = userIds.remove(0);
			String id3 = userIds.remove(0);
			resulting.put(id1, id2);
			resulting.put(id2, id3);
			resulting.put(id3, id1);
		}
		
		while(userIds.size() > 0){
			String id1 = userIds.remove(0);
			String id2 = userIds.remove(0);
			resulting.put(id1, id2);
			resulting.put(id2, id1);
		}
	
		return resulting;
	}

}
