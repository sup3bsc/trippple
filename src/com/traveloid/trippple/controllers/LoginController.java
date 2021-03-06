package com.traveloid.trippple.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.traveloid.trippple.dao.jpa.JpaUserDao;
import com.traveloid.trippple.entity.User;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String home = request.getContextPath();

		long id;
		try {
			id = Long.parseLong(request.getParameter("id"));
		} catch(NumberFormatException exception) {
			response.setStatus(400);
			return;
		}

		String password = request.getParameter("password");
		HttpSession session = request.getSession(); // Recupere la session associee a la requete, ou en cree une s'il n'y a pas de session

		JpaUserDao userdao = new JpaUserDao();
		User userInDb = userdao.findById(id);
		if(userInDb == null) {
			session.setAttribute("flash", "Unknown user");
			response.sendRedirect(home);
			return;
		}

		try {
			if(userInDb.comparePassword(password)) {
				session.setAttribute("user", id);
			} else {
				session.setAttribute("flash", "Incorrect password");
			}
		} catch(NoSuchAlgorithmException e) {
			// Java trouve pas l'algo de cryptage == Il y a plus rien a faire a ce point
		}

		response.sendRedirect(home);
	}
}
