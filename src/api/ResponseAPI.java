package api;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class ResponseAPI {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	/**
	 * Saves a response (to any kind of user activity), and places it in the databse for retrieval.
	 * @param problemUuid The problem that the user is saving their response to
	 * @param userId The User's unique identifier
	 * @param responseType One of the phase types (i.e. 'pre','question','post','chat','comment')
	 * @param response The user's response to be saved.
	 */
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
		e.setProperty("problemRun", CurrentAPI.getCurrentProblemRun());
		datastore.put(e);
	}
	
	public static String getQuestionResponse(String problemUuid, String userId){
		return getResponse(problemUuid, userId, "question");
	}
	
	public static String getCommentResponse(String problemUuid, String userId){
		return getResponse(problemUuid, userId, "comment");
	}
	
	// Retrieves a requested response from the database
	private static String getResponse(String problemUuid, String userId, String responseType){
		Key key = KeyFactory.createKey("Response", userId + "|" + problemUuid + "|" + responseType);
		try {
			Entity e = datastore.get(key);
			return (String) e.getProperty("response");
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

}
