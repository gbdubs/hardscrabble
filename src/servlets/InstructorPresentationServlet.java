package servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Problem;
import tools.UuidTools;
import api.AuthenticationAPI;
import api.ChatAPI;
import api.CurrentAPI;
import api.PairingAPI;

import com.google.appengine.api.datastore.EntityNotFoundException;

@SuppressWarnings("serial")
public class InstructorPresentationServlet extends HttpServlet{

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (AuthenticationAPI.isUserInstructor()){
			String uuid = UuidTools.parseUuidFromRequestUrl(req);
			if (uuid == null){
				uuid = (String) req.getAttribute("uuid");
			}
			if (uuid == null || uuid.length() == 0){
				resp.getWriter().println("Problem with UUID=" + uuid + " not found. Sorry!");
			} else {
				try {
					req.setAttribute("problem", new Problem(uuid));
					CurrentAPI.setCurrentProblem(uuid);
				} catch (EntityNotFoundException e) {
					resp.getWriter().println("Problem with UUID=" + uuid + " not found. Sorry!");
				}
				
				resp.setContentType("text/html");
				RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/instructor-presentation.jsp");	
				jsp.forward(req, resp);
			}
		} else {
			resp.sendRedirect("/404");
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (AuthenticationAPI.isUserInstructor() && req.getParameter("advance") != null){
			String uuid = req.getParameter("uuid");
			try {
				Problem p = new Problem(uuid);
				p.advance();
				if (p.currentPhase.equals("comment")){
					PairingAPI.constructPairings(uuid);
				} else if (p.currentPhase.equals("chat")){
					ChatAPI.initializeChatPhase();
				}
				resp.sendRedirect("/problem/" + uuid);
			} catch (EntityNotFoundException e) {
				// If it does not exist, ignore the request.
			}
		}
	}
}
