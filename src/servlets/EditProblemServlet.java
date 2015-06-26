package servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.UuidTools;
import models.Problem;
import api.AuthenticationAPI;
import api.ProblemAPI;

import com.google.appengine.api.datastore.EntityNotFoundException;

@SuppressWarnings("serial")
public class EditProblemServlet extends HttpServlet{
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (AuthenticationAPI.isUserInstructor()){
			String uuid = UuidTools.parseUuidFromRequestUrl(req);
			if (uuid != null){
				try {
					Problem p = new Problem(uuid);
					req.setAttribute("problem", p);
				} catch (EntityNotFoundException e) {
					resp.sendRedirect("/404");
					return;
				}
			}
			
			resp.setContentType("text/html");
			RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/editor.jsp");	
			jsp.forward(req, resp);
		} else {
			resp.sendRedirect("/404");
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (AuthenticationAPI.isUserInstructor()){
			if (req.getParameter("delete") != null){
				ProblemAPI.deleteProblem(req.getParameter("uuid"));
				resp.sendRedirect("/instructor");
			} else {
				ProblemAPI.updateProblemFromRequest(req);
				resp.sendRedirect("/instructor");
			}
		} else {
			resp.sendRedirect("404");
		}
	}
}
