package api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Problem;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;

public class PairingAPI {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public static Map<String, List<String>> getCurrentGroupMapping(){
		Entity e = getCurrentPairingDefinition();
		String jsonData = ((Text) e.getProperty("groupMapping")).getValue();
		Gson gson = new Gson();
		Map<String, List<String>> template = new HashMap<String, List<String>>();
		Map<String, List<String>> result = gson.fromJson(jsonData, template.getClass());
		return result;
	}
	
	public static String getPartnersQuestionResponse(String problemUuid, String userId){
		return ResponseAPI.getQuestionResponse(problemUuid, getPartnersUserId(problemUuid, userId));
	}

	public static String getPartnersUserId(String problemUuid, String userId){
		Entity e = getPairingDefinition(problemUuid);
		if (e == null) return null;
		String pairedUserId = (String) e.getProperty(userId);
		return pairedUserId;
	}

	public static String getPartnersCommentResponse(String problemUuid, String userId){
		Entity e = getPairingDefinition(problemUuid);
		if (e == null){
			return "[ERROR: Your partner's comments could not be retrieved.]";
		}
		String pairedUserId = (String) e.getProperty("INVERSE-" + userId);
		return ResponseAPI.getCommentResponse(problemUuid, pairedUserId);
	}

	/**
	 * Constructs pairings before the comment phase.
	 * 
	 * Some notes:
	 *  - In odd cases, we create one group of three A comments on B comments on C comments on A.
	 *  - In singelton cases, we create one group of one.
	 *  - We have five different kinds of algorithms that can be used to create these pairings.
	 *  	(random) -> Random assignment
	 *  	(length) -> Pairs users with assignments of similar length
	 *  	(inv-length) -> Pairs users with inversely long assignments
	 *  	(edit-distance) -> Pairs users whose assignments are similar to one another through Levenshtein comaprison
	 *  	(inv-edit-distance) -> Pairs users whose assignments are not similar through Levenshtein comparison
	 * @param problemUuid The problem to construct parings for.
	 */
	public static void constructPairings(){
		String problemUuid = CurrentAPI.getCurrentProblem();
		
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
		filters.add(new FilterPredicate("problemRun", FilterOperator.EQUAL, p.getProblemRun()));
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

	private static Entity getPairingDefinition(String problemUuid){
		try {
			return datastore.get(KeyFactory.createKey("Pairing", problemUuid));
		} catch (EntityNotFoundException enfe) {
			return null;
		}
	}

	/**
	 * Saves the problem comment pairings for later retreival and inference. 
	 * Note that we store this in a "Pairing" entity, and we JSON the map of userIds to groups
	 * for later extraction during the chat phase.
	 * @param problemUuid The Problem to save the comment pairings for.
	 * @param userPairing The Map from a user's id to the userId of the person who they will be commenting on.
	 */
	private static void saveProblemCommentPairingMapping(String problemUuid, Map<String, String> userPairing){
		Map<String, List<String>> groups = new HashMap<String, List<String>>();
		Entity e = new Entity(KeyFactory.createKey("Pairing", problemUuid));
		for(String user1 : userPairing.keySet()){
			String user2 = userPairing.get(user1);
			if (groups.containsKey(user2)){
				if (!groups.get(user2).contains(user1)){
					groups.get(user2).add(user1);
					groups.put(user1, groups.get(user2));
				}
			} else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(user1);
				if (!user1.equals(user2)){
					list.add(user2);
				}
				groups.put(user1, list);
				groups.put(user2, list);
			}
			e.setUnindexedProperty(user1, user2);
			e.setUnindexedProperty("INVERSE-" + user2, user1);
		}
		Gson gson = new Gson();
		e.setUnindexedProperty("groupMapping", new Text(gson.toJson(groups)));
		datastore.put(e);
	}

	private static Entity getCurrentPairingDefinition(){
		return getPairingDefinition(CurrentAPI.getCurrentProblem());
	}

	// Invokes the children methods which will construct a map between a userId, and the userId of the
	// other use that the first user will be commenting on.
	private static Map<String, String> constructPairingsByAlgorithm(Map<String, String> userResponses, String algorithm){
		if (userResponses.size() == 1){
			Map<String, String> result = new HashMap<String, String>();
			String userId = userResponses.keySet().iterator().next();
			result.put(userId, userId);
			return result;
		}
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

	// Pairs users randomly.
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

	private static Map<String, String> lengthAlgorithm(Map<String, String> userResponses) {
		Map<String, String> invertedResponses = invertMap(userResponses);
		List<String> responses = new ArrayList<String>(invertedResponses.keySet());
		Collections.sort(responses, new LengthComparator());
		
		Map<String, String> resulting = new HashMap<String, String>();
				
		if (responses.size() == 1){
			String loneUser = invertedResponses.get(responses.get(0));
			resulting.put(loneUser, loneUser);
			return resulting;
		}
		
		if (responses.size() % 2 == 1){
			String user1 = invertedResponses.get(responses.remove(0));
			String user2 = invertedResponses.get(responses.remove(0));
			String user3 = invertedResponses.get(responses.remove(0));
			resulting.put(user1, user2);
			resulting.put(user2, user3);
			resulting.put(user3, user1);
		}
		
		while (responses.size() > 0){
			String user1 = invertedResponses.get(responses.remove(0));
			String user2 = invertedResponses.get(responses.remove(0));
			resulting.put(user1, user2);
			resulting.put(user2, user1);
		}
		
		return resulting;
	}

	private static Map<String, String> invLengthAlgorithm(Map<String, String> userResponses) {
		Map<String, String> invertedResponses = invertMap(userResponses);
		List<String> responses = new ArrayList<String>(invertedResponses.keySet());
		Collections.sort(responses, new LengthComparator());
		
		Map<String, String> resulting = new HashMap<String, String>();
				
		if (responses.size() == 1){
			String loneUser = invertedResponses.get(responses.get(0));
			resulting.put(loneUser, loneUser);
			return resulting;
		}
		
		if (responses.size() % 2 == 1){
			String user1 = invertedResponses.get(responses.remove(0));
			String user2 = invertedResponses.get(responses.remove(responses.size() - 1));
			String user3 = invertedResponses.get(responses.remove(responses.size() - 1));
			resulting.put(user1, user2);
			resulting.put(user2, user3);
			resulting.put(user3, user1);
		}
		
		while (responses.size() > 0){
			String user1 = invertedResponses.get(responses.remove(0));
			String user2 = invertedResponses.get(responses.remove(responses.size() - 1));
			resulting.put(user1, user2);
			resulting.put(user2, user1);
		}
		
		return resulting;
	}

	private static Map<String, String> invertMap(Map<String, String> original){
		original = makeMapInvertible(original);
		HashMap<String, String> newMap = new HashMap<String, String>();
		for (String key : original.keySet()){
			newMap.put(original.get(key), key);
		}
		return newMap;
	}
	
	private static Map<String, String> makeMapInvertible(Map<String, String> original){
		if (original.keySet().size() == original.values().size()){
			return original;
		} else {
			Map<String, String> newMap = new HashMap<String, String>();
			Set<String> allValues = new HashSet<String>();
			for (String key : original.keySet()){
				String value = original.get(key);
				while (allValues.contains(value)){
					value = value + " ";
				}
				newMap.put(key, value);
				allValues.add(value);
			}
			return newMap;
		}
	}

	private static Map<String, String> editDistanceAlgorithm(Map<String, String> userResponses) {
		Map<String, String> responsesToUsers = invertMap(userResponses);
		Map<String, List<String>> responsePreferences = compileEditDistancePreferenceList(responsesToUsers.keySet());
		return generateNaieveStableSameSexMarriage(responsePreferences, responsesToUsers);
	}

	private static Map<String, String> invEditDistanceAlgorithm(Map<String, String> userResponses) {
		Map<String, String> responsesToUsers = invertMap(userResponses);
		Map<String, List<String>> responsePreferences = compileEditDistancePreferenceList(responsesToUsers.keySet());
		for (List<String> preferences : responsePreferences.values()){
			Collections.reverse(preferences);
		}
		return generateNaieveStableSameSexMarriage(responsePreferences, responsesToUsers);
	}

	private static Map<String, List<String>> compileEditDistancePreferenceList(Collection<String> allResponses){
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (String response : allResponses){
			List<String> others = new ArrayList<String>(allResponses);
			others.remove(response);
			Collections.sort(others, new LevenshteinComparator(response));
			result.put(response, others);
		}
		return result;
	}
	
	private static Map<String, String> generateNaieveStableSameSexMarriage(Map<String, List<String>> preferences, Map<String, String> responsesToUsers){
		Map<String, String> usersToResponses = invertMap(responsesToUsers);
		Map<String, String> userPairings = new HashMap<String, String>();
		
		for (String response : preferences.keySet()){
			String associatedUser1 = responsesToUsers.get(response);
			
			if (!userPairings.containsKey(associatedUser1)){
				List<String> similarResponses = preferences.get(response);
				for (String alreadyTaken : userPairings.keySet()){
					similarResponses.remove(usersToResponses.get(alreadyTaken));
				}
				
				// SINGLETON CASE
				if (preferences.keySet().size() == 1){
					userPairings.put(associatedUser1, associatedUser1);
				}
				
				// TRIPLET CASE
				else if (preferences.keySet().size() % 2 == 1 && userPairings.keySet().size() == 0){
					String associatedUser2 = responsesToUsers.get(similarResponses.remove(0));			
					String associatedUser3 = responsesToUsers.get(similarResponses.remove(0));
					userPairings.put(associatedUser1, associatedUser2);
					userPairings.put(associatedUser2, associatedUser3);
					userPairings.put(associatedUser3, associatedUser1);
				}
				
				// DUPLE CASES
				else {	
					String associatedUser2 = responsesToUsers.get(similarResponses.remove(0));
					userPairings.put(associatedUser1, associatedUser2);
					userPairings.put(associatedUser2, associatedUser1);
				}
			}
		}
		
		return userPairings;
	}
	
	
	private static class LengthComparator implements Comparator<String>{
	
		@Override
		public int compare(String s1, String s2) {
			if (s1.length() > s2.length()){
				return 1;
			} else if (s1.length() < s2.length()){
				return -1;
			} else {
				return 0;
			}
		}
	}

	private static class LevenshteinComparator implements Comparator<String>{
	
		private String target;
		
		public LevenshteinComparator(String target){
			this.target = target;
		}
		
		private static int levenshteinDistance(String a, String b) {
	        a = a.toLowerCase();
	        b = b.toLowerCase();
	        int [] costs = new int [b.length() + 1];
	        for (int j = 0; j < costs.length; j++)
	            costs[j] = j;
	        for (int i = 1; i <= a.length(); i++) {
	            costs[0] = i;
	            int nw = i - 1;
	            for (int j = 1; j <= b.length(); j++) {
	                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
	                nw = costs[j];
	                costs[j] = cj;
	            }
	        }
	        return costs[b.length()];
	    }
		
		@Override
		public int compare(String s1, String s2) {
			int d1 = levenshteinDistance(target, s1);
			int d2 = levenshteinDistance(target, s2);
			if (d1 > d2){
				return 1;
			} else if (d1 < d2){
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	
	

}
