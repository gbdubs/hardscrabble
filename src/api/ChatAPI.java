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
	
	private static boolean testMode = false;
	private static UserService userService = UserServiceFactory.getUserService();
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private static AsyncDatastoreService asyncDatastore = DatastoreServiceFactory.getAsyncDatastoreService();
	
	private static Map<String, List<String>> chatrooms = null;
	/**
	 * Initializes the Chat phase for all users currently signed in. Uses the pairings
	 * to create chat rooms.
	 */
	public static void initializeChatPhase(){				
		Map<String, List<String>> groups = PairingAPI.getCurrentGroupMapping();
		chatrooms = groups;
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
		String chatroomUuid = UUID.randomUUID().toString();
		Entity e = getCurrentChatSwitchboard();
		for (String userId : userIds){
			e.setUnindexedProperty(userId, chatroomUuid);
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
			Entity chatroomInbox = new Entity(KeyFactory.createKey("ChatroomInbox", chatroomUuid + "-" + userId));	
			List<String> messages = new ArrayList<String>();
			messages.add(welcomeMessage);
			chatroomInbox.setProperty("messages", messages);
			if (!testMode){
				asyncDatastore.put(chatroomInbox);
			} else {
				datastore.put(chatroomInbox);
			}
		}
		return chatroomUuid;
	}

	/**
	 * Returns the chat switchboard, which is the mapping between users and their chatroomUuid
	 * @return An entity storing the chat room switchboard definition.
	 */
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
	
	/**
	 * Returns the chat room Uuid that the user is assigned to.
	 * @return
	 */
	public static String getMyChatRoom(){
		return getMyChatRoom(userService.getCurrentUser().getUserId());	
	}
	
	/**
	 * A stripped down version of the above function to allow testing
	 * @param userId The requested User's ID
	 * @return their assigned chatroom uuid
	 */
	public static String getMyChatRoom(String userId) {
		if (userService.isUserLoggedIn() || testMode){
			Entity e = getCurrentChatSwitchboard();
			return (String) e.getProperty(userId);
		} else {
			return null;
		}	
	}

	/**
	 * Sends a message from one user to all others in the chat room.
	 * 
	 * Accomplishes this by placing a message in each user's message queue, which is stored 
	 * in the datastore as ChatroomInbox entities.  Since it does each of these operations
	 * Asynchronously, we have no guarantees about collisions, but they are relatively unlikely,
	 * given that we have users pipelining all requests from single requests (with the exception
	 * of a single, 3 way chat, which is going to have some issues in any naieve system)
	 * @param chatRoomUuid The Chat Room Unique Identifier
	 * @param userId The user's unique identifier
	 * @param message The message to pass
	 */
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
				if (!testMode){
					asyncDatastore.put(e);
				} else {
					datastore.put(e);
				}
			} catch (EntityNotFoundException e1) {
				// Skip this if we don't get here.
			}
		}
	}
	
	/**
	 * Finds and returns the messages for the requesting user (given an offset of how many the user has already
	 * read).
	 * @param chatRoomUuid The unique chatroom identifier
	 * @param userId The User's ID
	 * @param alreadyRead The number of messages that the user has already recieved successfully.
	 * @return A list of all unread messages for the user.
	 */
	public static List<String> getMessagesForUser(String chatRoomUuid, String userId, int alreadyRead){
		Entity chatroomInbox;
		try {
			chatroomInbox = datastore.get(KeyFactory.createKey("ChatroomInbox", chatRoomUuid + "-" + userId));
		} catch (EntityNotFoundException e) {
			System.out.println("NO CHATROOM INBOX FOUND WITH ID = " + chatRoomUuid + "-" + userId);
			return new ArrayList<String>();
		}
		List<String> allMessages = (List<String>) chatroomInbox.getProperty("messages");
		if (allMessages.size() >= alreadyRead){
			return allMessages.subList(alreadyRead, allMessages.size());
		} else {
			return new ArrayList<String>();
		}
	}
	
	public static void runInTestMode(){
		testMode = true;
	}
	
}
