package com.smart.services;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import jakarta.servlet.http.HttpSession;

@Component
public class sessionHelper {

	public void removeMessageFromSession() {
		try {

			HttpSession session1 = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
			session1.removeAttribute("message");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
