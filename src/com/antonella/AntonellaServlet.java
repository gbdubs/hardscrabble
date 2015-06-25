package com.antonella;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.problem.ProblemAPI;

@SuppressWarnings("serial")
public class AntonellaServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		req.setAttribute("allProblems", ProblemAPI.getAllProblems());
		
		resp.setContentType("text/html");
		RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/antonella-landing.jsp");	
		jsp.forward(req, resp);
	}
}
