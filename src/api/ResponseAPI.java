package api;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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
		e.setProperty("problemRun", CurrentAPI.getCurrentProblemRun());
		datastore.put(e);
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

}
