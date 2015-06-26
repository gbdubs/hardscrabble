package api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Problem;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class ChatAPI {
	
	private static UserService userService = UserServiceFactory.getUserService();
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private static AsyncDatastoreService asyncDatastore = DatastoreServiceFactory.getAsyncDatastoreService();
	
	private static Map<String, Collection<String>> chatrooms;
	/**
	 * Initializes the Chat phase for all users currently signed in. Uses the pairings
	 * to create chat rooms.
	 */
	public static void initializeChatPhase(){				
		Map<String, List<String>> groups = PairingAPI.getCurrentGroupMapping();
		for (List<String> group : groups.values()){
			while(group.remove(null)){}
			createChat(group);
		}
	}
	
	/**
	 * Constructs a Chat Room for a given Collection of Users, and saves that definition.
	 * @param userIds
	 * @return
	 */
	private static String createChat(Collection<String> userIds){
		// Updates the chat Switchboard to direct users to the correct chat.
		String uuid = UUID.randomUUID().toString();
		Entity e = getCurrentChatSwitchboard();
		for (String userId : userIds){
			e.setUnindexedProperty(userId, uuid);
		}
		datastore.put(e);
		
		String welcomeMessage = "ADMIN: Welcome to chat. Please discuss the comments that you made on one another's code.";
		if (userIds.size() > 2){
			welcomeMessage += "MESSAGEBREAK Please note that there are more than two people in this chat. Each person be identified by a consistent number.";
		}
		
		// Constructs the new chatroom entities where messages will be stored. 
		// NOTE that each chatroom is not an entity, but a sum of multiple inbox enities.
		// Messages are directed to each inbox by the static variable chatrooms, initialized from
		// the pairing API.
		for (String userId : userIds){
			Entity chatroomInbox = new Entity(KeyFactory.createKey("ChatroomInbox", uuid + "-" + userId));	
			List<String> messages = new ArrayList<String>();
			messages.add(welcomeMessage);
			chatroomInbox.setProperty("messages", messages);
			asyncDatastore.put(chatroomInbox);
		}
		return uuid;
	}

	private static Entity getCurrentChatSwitchboard(){
		Problem p;
		try {
			p = new Problem(CurrentAPI.getCurrentProblem());
		} catch (EntityNotFoundException e1) {
			return null;
		}
		String key = p.uuid + "-" + p.problemRun;
		Entity e;
		try {
			e = datastore.get(KeyFactory.createKey("ChatroomSwitchboard", key));
		} catch (EntityNotFoundException enfe) {
			e = new Entity(KeyFactory.createKey("ChatroomSwitchboard", key));
		}
		return e;
	}
	
	public static String getMyChatRoom(){
		if (userService.isUserLoggedIn()){
			Entity e = getCurrentChatSwitchboard();
			return (String) e.getProperty(userService.getCurrentUser().getUserId());
		} else {
			return null;
		}		
	}
	
	public static void sendMessageToChatRoom(String chatRoomUuid, String userId, String message){
		Collection<String> pairedUsers = chatrooms.get(userId);
		pairedUsers.remove(userId);
		for (String otherUser : pairedUsers){
			Entity e;
			try {
				e = datastore.get(KeyFactory.createKey("ChatroomInbox", chatRoomUuid + "-" + otherUser));
				List<String> messages = (List<String>) e.getProperty("messages");
				messages.add(message);
				e.setUnindexedProperty("messages", messages);
				asyncDatastore.put(e);
			} catch (EntityNotFoundException e1) {
				// Skip this if we don't get here.
			}
		}
	}
	
	public static List<String> getMessagesForMe(String chatRoomId, String userId, int truncate){
		Entity chatroomInbox;
		try {
			chatroomInbox = datastore.get(KeyFactory.createKey("ChatroomInbox", chatRoomId + "-" + userId));
		} catch (EntityNotFoundException e) {
			System.out.println("NO CHATROOM INBOX FOUND WITH ID = " + chatRoomId + "-" + userId);
			return new ArrayList<String>();
		}
		List<String> allMessages = (List<String>) chatroomInbox.getProperty("messages");
		if (allMessages.size() >= truncate){
			return allMessages.subList(truncate, allMessages.size());
		} else {
			return new ArrayList<String>();
		}
	}
	
}
