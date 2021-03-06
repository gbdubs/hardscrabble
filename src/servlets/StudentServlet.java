package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Problem;
import api.CurrentAPI;
import api.PairingAPI;
import api.ResponseAPI;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class StudentServlet extends HttpServlet{

	private static UserService userService = UserServiceFactory.getUserService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		
		String problemUuid = CurrentAPI.getCurrentProblem();
				
		if (returnSimpleDataIfAsked(problemUuid, req, resp)){
			return;
		}
	
		User user = UserServiceFactory.getUserService().getCurrentUser();
		String loginUrl = UserServiceFactory.getUserService().createLoginURL("/home");
		String logoutUrl = UserServiceFactory.getUserService().createLogoutURL("/home");
		
		req.setAttribute("user", user);
		req.setAttribute("loginUrl", loginUrl);
		req.setAttribute("logoutUrl", logoutUrl);

		
		resp.setContentType("text/html");
		RequestDispatcher jsp;
		if (problemUuid != null){
			jsp = req.getRequestDispatcher("/WEB-INF/pages/student.jsp");	
		} else {
			jsp = req.getRequestDispatcher("/WEB-INF/pages/no-current-problem.jsp");	
		}
		
		jsp.forward(req, resp);
		return;
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		User user = UserServiceFactory.getUserService().getCurrentUser();
		if (user == null){ return; }
		
		String userId = user.getUserId();
		String problemUuid = CurrentAPI.getCurrentProblem();
		String responseType = req.getParameter("currentPhase");
		String response = req.getParameter("response");
		
		ResponseAPI.saveResponse(problemUuid, userId, responseType, response);
	}
	
	public boolean returnSimpleDataIfAsked(String problemUuid, HttpServletRequest req, HttpServletResponse resp) throws IOException{
		PrintWriter pw = resp.getWriter();
		
		if (req.getParameter("getPhase") != null){
			pw.print(CurrentAPI.getCurrentPhase());
			return true;
		}
		Problem p;
		try {
			p = new Problem(problemUuid);
		} catch (EntityNotFoundException e) {
			return false;
		}
		
		if (req.getParameter("getPreQuestion") != null){
			pw.print(p.getPreQuestion());
			return true;
		} else if (req.getParameter("getPostQuestion") != null){
			pw.print(p.getPostQuestion());
			return true;
		} else if (req.getParameter("getQuestion") != null){
			pw.print(p.getQuestion());
			return true;
		} else if (req.getParameter("getTimer") != null){
			pw.print(p.getSecondsLeftInPhase());
			return true;
		} else if (req.getParameter("getSolution") != null){
			pw.print(p.getSolution());
			return true;
		} else if (req.getParameter("getMyResponse") != null){
			if (userService.isUserLoggedIn()){
				String userId = userService.getCurrentUser().getUserId();
				pw.print(ResponseAPI.getQuestionResponse(CurrentAPI.getCurrentProblem(), userId));
				return true;
			}
		} else if (req.getParameter("getPartnerQuestionResponse") != null){
			if (userService.isUserLoggedIn()){
				String userId = userService.getCurrentUser().getUserId();
				pw.print(PairingAPI.getPartnersQuestionResponse(p.getUuid(), userId));
				return true;
			}
		} else if (req.getParameter("getPartnerCommentResponse") != null){
			if (userService.isUserLoggedIn()){
				String userId = userService.getCurrentUser().getUserId();
				pw.print(PairingAPI.getPartnersCommentResponse(p.getUuid(), userId));
				return true;
			}
		} else if (req.getParameter("getMyComments") != null){
			if (userService.isUserLoggedIn()){
				String userId = userService.getCurrentUser().getUserId();
				pw.print(ResponseAPI.getCommentResponse(CurrentAPI.getCurrentProblem(), userId));
				return true;
			}
		}
		
		return false;
	}
}
