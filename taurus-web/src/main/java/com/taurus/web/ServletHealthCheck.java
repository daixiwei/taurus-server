package com.taurus.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.taurus.core.util.StringUtil;


@WebServlet("/health")
public class ServletHealthCheck extends HttpServlet {

	private static final long	serialVersionUID	= 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println(StringUtil.Empty);
	}

}
