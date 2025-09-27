package org.springframework.samples.petclinic.system;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuditInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = (auth != null ? auth.getName() : "anonymous");
		String method = req.getMethod();
		String path = req.getRequestURI();
		if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
			log.info("AUDIT user={} method={} path={}", user, method, path);
		}
		return true;
	}

}
