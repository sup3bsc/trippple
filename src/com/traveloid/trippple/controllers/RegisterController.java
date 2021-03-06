package com.traveloid.trippple.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.traveloid.trippple.dao.CampusDao;
import com.traveloid.trippple.dao.UserDao;
import com.traveloid.trippple.dao.jpa.JpaCampusDao;
import com.traveloid.trippple.dao.jpa.JpaUserDao;
import com.traveloid.trippple.entity.Campus;
import com.traveloid.trippple.entity.User;
import com.traveloid.trippple.util.TextUtils;

/**
 * Servlet implementation class RegisterController
 */
@WebServlet("/register")
public class RegisterController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDao userDao;
	private CampusDao campusDao;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		userDao = new JpaUserDao();
		campusDao = new JpaCampusDao();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User) request.getAttribute("user");
		if(user != null) {
			response.sendRedirect(request.getContextPath());
			return;
		}

		request.setAttribute("campuses", this.campusDao.findAll());

		request.getRequestDispatcher("register.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String home = request.getContextPath();

		User user = (User) request.getAttribute("user");
		if(user != null) {
			response.setStatus(403);
			return;
		}

		user = new User();

		long id;
		try {
			id = Long.parseLong(request.getParameter("id"));
		} catch(NumberFormatException e) {
			request.getSession().setAttribute("flash", "Incorrect ID");
			response.sendRedirect(home + "/register");
			return;
		}

		user.setId(id);
		user.setEmail(TextUtils.escapeHTML(request.getParameter("email")));
		user.setFirstName(TextUtils.escapeHTML(request.getParameter("firstName")));
		user.setLastName(TextUtils.escapeHTML(request.getParameter("lastName")));

		long campusId;
		try {
			campusId = Long.parseLong(request.getParameter("campus"));
		} catch(NumberFormatException e) {
			request.getSession().setAttribute("flash", "Incorrect Campus");
			response.sendRedirect(home + "/register");
			return;
		}

		Campus campus = this.campusDao.findById(campusId);
		if(campus == null) {
			request.getSession().setAttribute("flash", "Incorrect Campus");
			response.sendRedirect(home + "/register");
			return;
		}

		String primPass = request.getParameter("primPassword"), secPass = request.getParameter("secPassword");

		if(primPass.equals(secPass)) {
			try {
				user.setPassword(primPass);
			} catch(NoSuchAlgorithmException e) {
				// Pas d'algo, pas de crypto
			}
		} else {
			request.getSession().setAttribute("flash", "Passwords do not match");
			response.sendRedirect(home + "/register");
			return;
		}

		try {
			userDao.addUser(user);
		} catch(Exception e) {
			request.getSession().setAttribute("flash", "Unkown error");
			response.sendRedirect(home + "/register");
			return;
		}

		response.sendRedirect(home);
	}

}
