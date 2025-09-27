package org.springframework.samples.petclinic.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Global Exception Handler to catch application errors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Catch all exceptions and redirect to a friendly error page.
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception ex, Model model) {
		// Log the full error (stack trace) for developers
		logger.error("Unhandled exception caught by GlobalExceptionHandler", ex);

		// Send a safe message to the user
		ModelAndView mav = new ModelAndView();
		mav.setViewName("error"); // this maps to error.html in templates
		mav.addObject("errorMessage", "Something went wrong. Please try again later.");
		return mav;
	}

}
