package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.UuidTools;
import api.ChatAPI;

import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class ChatServlet extends HttpServlet {

	public static final String MESSAGE_BREAK = "MESSAGEBREAK";
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		PrintWriter pw = resp.getWriter();
		// The chatroomUuid chat inintializaiton call.
		if (req.getParameter("getChatRoomUuid") != null){
			pw.print(ChatAPI.getMyChatRoom());
		} 
		
		// The userId chat inintializaiton call.
		else if (req.getParameter("getUserId") != null){
			pw.print(UserServiceFactory.getUserService().getCurrentUser().getUserId());
		} 
		
		// Otherwise, we return them the messages that they are looking for. 
		else {
			String chatRoomUuid = req.getParameter("chatRoomUuid");
			String userId = req.getParameter("userId");
			if (chatRoomUuid == null || userId == null){
				pw.print("[ERROR: the chat was not initialized]");
			}
			int mostRecentMessage = 0;
			if (req.getParameter("mostRecentMessage") != null){
				mostRecentMessage = Integer.parseInt(req.getParameter("mostRecentMessage"));
			}
			List<String> messages = ChatAPI.getMessagesForUser(chatRoomUuid, userId, mostRecentMessage);
			
			// Prints the current system time before any transmission.  
			// Imagine this scenario:  We have an ajax request which for some reason is not 
			// processed for .8 seconds. Before the response can be sent back to the user,
			// the javascript issues another ajax request for the same data.
			
			// This gives us a way of comparing when different requests were processed.
			// If we get a duplicate on the JS side, we can ignore it (but you can still say
			// the same message twice, just couldn't if they were 100% synchronous calls.
			
			// This is a messy way of doing this; but it works, and was simple to implement
			pw.println(System.currentTimeMillis() / 1000);
			for(String message : messages){
				// The delimeter we use to seperate out messages can be anything, and using
				// this kind of naieve implementation allows us to write quick, simple and
				// reasonably efficent code.
				pw.print(message + MESSAGE_BREAK);
			}
		}
	}
	
	/**
	 * Sending a chat message.  Note that users need not be logged in for this, we only require
	 * that their chat be initialized with the propper info (i.e. they could log out between then and
	 * now)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String chatRoomUuid = UuidTools.parseUuidFromString(req.getParameter("chatRoomUuid"));
		String userId = req.getParameter("userId");
		String message = req.getParameter("message");
		if (chatRoomUuid == null || userId == null || message == null){
			return;
		}
		ChatAPI.sendMessageToChatRoom(chatRoomUuid, userId, message);
	}
	
}
