package com.antonella;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.problem.Problem;
import com.problem.ProblemAPI;

@SuppressWarnings("serial")
public class EditProblemServlet extends HttpServlet{
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		String uuid = UuidTools.getUuidFromUrl(req.getRequestURI());
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
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if (req.getParameter("delete") != null){
			ProblemAPI.deleteProblem(req.getParameter("uuid"));
			resp.sendRedirect("/antonella");
			return;
		}
		
		ProblemAPI.updateProblemFromRequest(req);
		resp.sendRedirect("/antonella");
	}
}
