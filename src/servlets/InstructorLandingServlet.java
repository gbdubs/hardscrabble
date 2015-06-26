package servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import api.AuthenticationAPI;
import api.ProblemAPI;

@SuppressWarnings("serial")
public class InstructorLandingServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		if (AuthenticationAPI.isUserInstructor()){
			
			req.setAttribute("allProblems", ProblemAPI.getAllProblems());
			
			resp.setContentType("text/html");
			RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/instructor-landing.jsp");	
			jsp.forward(req, resp);
			
		} else {
			resp.sendRedirect("/404");
		}
	}
}
