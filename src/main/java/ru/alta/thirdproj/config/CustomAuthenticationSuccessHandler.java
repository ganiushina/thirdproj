package ru.alta.thirdproj.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.alta.thirdproj.entites.UserLogin;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.services.UserLoginServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		String userName = authentication.getName();

	//	UserLogin theUser = loginService.findLoginUserByUserName(userName);
		HttpSession session = request.getSession();
		session.setAttribute("user", userName);

		if(!request.getHeader("referer").contains("login")) {
			response.sendRedirect(request.getHeader("referer"));
		} else {
			response.sendRedirect(request.getContextPath() + "/user");
		}
	}
}
