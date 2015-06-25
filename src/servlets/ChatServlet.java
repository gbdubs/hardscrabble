package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import api.ChatAPI;

import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class ChatServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		PrintWriter pw = resp.getWriter();
		if (req.getParameter("getChatRoomUuid") != null){
			pw.print(ChatAPI.getMyChatRoom());
		} else if (req.getParameter("getUserId") != null){
			pw.print(UserServiceFactory.getUserService().getCurrentUser().getUserId());
		} else {
			String chatRoomUuid = req.getParameter("chatRoomUuid");
			String userId = req.getParameter("userId");
			int mostRecentMessage = Integer.parseInt(req.getParameter("mostRecentMessage"));
			List<String> messages = ChatAPI.getMessagesForMe(chatRoomUuid, userId, mostRecentMessage);
			pw.println(System.currentTimeMillis() / 1000);
			for(String message : messages){
				pw.print(message + "MESSAGEBREAK");
			}
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String chatRoomUuid = req.getParameter("chatRoomUuid");
		String userId = req.getParameter("userId");
		String message = req.getParameter("message");
		ChatAPI.sendMessageToChatRoom(chatRoomUuid, userId, message);
	}
	
}
