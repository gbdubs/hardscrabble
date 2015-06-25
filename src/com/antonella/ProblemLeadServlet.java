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
import com.problem.ResponseAPI;

@SuppressWarnings("serial")
public class ProblemLeadServlet extends HttpServlet{

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		String uuid = UuidTools.getUuidFromUrl(req.getRequestURI());
		if (uuid == null){
			uuid = (String) req.getAttribute("uuid");
		}
		if (uuid == null || uuid.length() == 0){
			resp.getWriter().println("Problem with UUID=" + uuid + " not found. Sorry!");
		} else {
			try {
				req.setAttribute("problem", new Problem(uuid));
				ProblemAPI.setCurrentProblem(uuid);
			} catch (EntityNotFoundException e) {
				resp.getWriter().println("Problem with UUID=" + uuid + " not found. Sorry!");
			}
			
			resp.setContentType("text/html");
			RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/problem-leader.jsp");	
			jsp.forward(req, resp);
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		if (req.getParameter("advance") != null){
			String uuid = req.getParameter("uuid");
			try {
				Problem p = new Problem(uuid);
				p.advance();
				if (p.currentPhase.equals("comment")){
					ResponseAPI.constructPairings(uuid);
				}
				resp.sendRedirect("/problem/lead/" + uuid);
			} catch (EntityNotFoundException e) {
				// If it does not exist, ignore the request.
			}
		}
	}
}
