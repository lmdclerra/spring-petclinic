package org.springframework.samples.petclinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize if you want to use it later
public class SecurityConfig {

	// demo users — replace with real users later
	@Bean
	UserDetailsService users() {
		return new InMemoryUserDetailsManager(
				User.withUsername("admin").password("{noop}admin123").roles("ADMIN", "USER").build(),
				User.withUsername("user").password("{noop}user123").roles("USER").build());
	}

	@Bean
	SecurityFilterChain web(HttpSecurity http) throws Exception {
		http
			// 1) authorize requests
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/oups", "/vets/**", "/webjars/**", "/resources/**", "/css/**", "/js/**",
						"/images/**")
				.permitAll()
				// H2 console: dev only; keep it behind ADMIN for safety
				.requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
				.hasRole("ADMIN")
				// everything else needs login
				.anyRequest()
				.authenticated())

			// 2) login/logout
			.formLogin(form -> form.loginPage("/login")
				.permitAll() // you'll add a simple login view in step 5
				.defaultSuccessUrl("/", true))
			.logout(logout -> logout.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID"))

			// 3) CSRF — keep it ON, but relax for H2 console only
			.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))

			// 4) security headers (CSP, clickjacking, etc.)
			.headers(headers -> headers
				// H2 console uses frames — allow same origin so it renders
				.frameOptions(frame -> frame.sameOrigin())
				// Content-Security-Policy — loosened for demo; tighten later
				.contentSecurityPolicy(csp -> csp.policyDirectives(
						"default-src 'self'; img-src 'self' data:; " + "style-src 'self' 'unsafe-inline'; "
								+ "script-src 'self' 'unsafe-inline'; " + "font-src 'self' data:"))
				// prevent MIME sniffing
				.contentTypeOptions(Customizer.withDefaults())
				// XSS protection
				.xssProtection(Customizer.withDefaults())
			// HSTS only makes sense on HTTPS — skip for localhost dev
			);

		return http.build();
	}

}
