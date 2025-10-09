package org.springframework.samples.petclinic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/vets.html", "/resources/**", "/webjars/**")
			.permitAll()
			.requestMatchers("/h2-console/**")
			.hasRole("ADMIN")
			.requestMatchers("/owners/**", "/pets/**", "/visits/**")
			.authenticated()
			.anyRequest()
			.authenticated())
			.formLogin(form -> form.loginPage("/login").permitAll())
			.logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
			.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
			.headers(headers -> headers.frameOptions().sameOrigin());

		return http.build();
	}

	@Bean
	public UserDetailsService users() {
		UserDetails admin = User.builder().username("admin").password("{noop}admin123").roles("ADMIN").build();

		UserDetails user = User.builder().username("user").password("{noop}user").roles("USER").build();

		return new InMemoryUserDetailsManager(admin, user);
	}

}