package com.antonella;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class CheckInServlet extends HttpServlet{

	private static UserService userService = UserServiceFactory.getUserService();
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	private static final int POLL_INTERVAL = 10;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		List<String> userEmails = getCurrentlyLoggedIn();

		Collections.sort(userEmails);
		
		PrintWriter pw = resp.getWriter();
		
		pw.println(userEmails.size() + " Active Users:");
		
		for (String email : userEmails){
			pw.println(email + ",");
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp){
		recordCheckIn();
	}
	
	public static void recordCheckIn(){
		User user = userService.getCurrentUser();
		if (user != null){
			String userId = user.getUserId();
			String userEmail = user.getEmail();
			Entity e;
			try {
				e = datastore.get(KeyFactory.createKey("LastCheckIn", userId));
			} catch (EntityNotFoundException e1) {
				e = new Entity(KeyFactory.createKey("LastCheckIn", userId));
				e.setUnindexedProperty("userEmail", userEmail);
				e.setUnindexedProperty("userId", userId);
			}
			e.setProperty("lastCheckIn", System.currentTimeMillis());
			datastore.put(e);
		}
	}
	
	public static List<String> getCurrentlyLoggedIn(){
		Query q = new Query("LastCheckIn");
		FilterPredicate f = new FilterPredicate("lastCheckIn", Query.FilterOperator.GREATER_THAN_OR_EQUAL, System.currentTimeMillis() - 1000 * 2 * POLL_INTERVAL);
		q.setFilter(f);
		PreparedQuery pq = datastore.prepare(q);
		List<String> results = new ArrayList<String>();
		for (Entity e : pq.asIterable()){
			results.add((String) e.getProperty("userEmail"));
		}
		return results;
	}
	
	public static List<String> getCurrentlyLoggedInUserIds(){
		Query q = new Query("LastCheckIn");
		FilterPredicate f = new FilterPredicate("lastCheckIn", Query.FilterOperator.GREATER_THAN_OR_EQUAL, System.currentTimeMillis() - 1000 * 2 * POLL_INTERVAL);
		q.setFilter(f);
		PreparedQuery pq = datastore.prepare(q);
		List<String> results = new ArrayList<String>();
		for (Entity e : pq.asIterable()){
			results.add((String) e.getProperty("userId"));
		}
		return results;
	}
	
}
