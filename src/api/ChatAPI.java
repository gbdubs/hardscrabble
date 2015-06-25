package api;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import models.Problem;
import servlets.CheckInServlet;

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

	public static void initializeChatPhase(){
		List<String> allLoggedInUsers = CheckInServlet.getCurrentlyCheckedInUserIds();
		Map<String, List<String>> groups = new HashMap<String, List<String>>();
		for (String userId : allLoggedInUsers){
			if (!groups.containsKey(userId)){
				List<String> chatGroup = new ArrayList<String>();
				String current = userId;
				while(!chatGroup.contains(current)){
					chatGroup.add(current);
					current = PairingAPI.getPairedUserId(CurrentAPI.getCurrentProblem(), current);
				}
				for (String chatter : chatGroup){
					groups.put(chatter, chatGroup);
				}
			}
		}
		for (List<String> group : groups.values()){
			while(group.remove(null)){}
			createChat(group);
		}
	}
	
	private static String createChat(List<String> userIds){
		String uuid = UUID.randomUUID().toString();
		Entity e = getCurrentChatSwitchboard();
		for (String userId : userIds){
			e.setUnindexedProperty(userId, uuid);
		}
		datastore.put(e);
		
		Entity chatroom = new Entity(KeyFactory.createKey("Chatroom", uuid));
		chatroom.setUnindexedProperty("members", userIds);
		String welcomeMessage = "ADMIN: Welcome to Chat.  Please Discuss the Comments that Each of you Made on One Another's Code.";
		if (userIds.size() > 2){
			welcomeMessage += "\n Please note that there are more than two people in this chat.  It is recommended that you say your name before each of your messages.";
		}
		List<String> messages = new ArrayList<String>();
		messages.add(welcomeMessage);
		for (String userId : userIds){
			chatroom.setUnindexedProperty("messagesFor" + userId, messages);
		}
		datastore.put(chatroom);
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
	
	public static void sendMessageToChatRoom(String chatRoomId, String userId, String message){
		Entity chatroom;
		try {
			chatroom = datastore.get(KeyFactory.createKey("Chatroom", chatRoomId));
		} catch (EntityNotFoundException e) {
			// Failed.  Log the exception and move on.
			return;
		}
		List<String> userIds = (List<String>) chatroom.getProperty("members");
		if (userIds == null){
			userIds = new ArrayList<String>();
			Map<String, Object> propertiesOfEntity = chatroom.getProperties();
			for (String s : propertiesOfEntity.keySet()){
				if (s.contains("messagesFor")){
					String inferredMemberId = s.substring(11);
					userIds.add(inferredMemberId);
				}
			}
		}
		userIds.remove(userId);
		for (String otherUserId : userIds){
			List<String> messagesForOther = (List<String>) chatroom.getProperty("messagesFor" + otherUserId);
			messagesForOther.add(message);
			chatroom.setUnindexedProperty("messagesFor" + otherUserId, messagesForOther);
		}
		datastore.put(chatroom);
	}
	
	public static List<String> getMessagesForMe(String chatRoomId, String userId){
		return getMessagesForMe(chatRoomId, userId, 0);
	}
	
	public static List<String> getMessagesForMe(String chatRoomId, String userId, int truncate){
		Entity chatroom;
		try {
			chatroom = datastore.get(KeyFactory.createKey("Chatroom", chatRoomId));
		} catch (EntityNotFoundException e) {
			System.out.println("NO CHATROOM FOUND WITH ID = " + chatRoomId);
			return new ArrayList<String>();
		}
		List<String> allMessages = (List<String>) chatroom.getProperty("messagesFor" + userId);
		if (allMessages.size() >= truncate){
			return allMessages.subList(truncate, allMessages.size());
		} else {
			return new ArrayList<String>();
		}
	}
	
}
