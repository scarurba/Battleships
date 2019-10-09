package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import sun.security.util.Password;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class SalvoApplication {
	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,
									  GameRepository gameRepository,
									  GamePlayerRepository gpRepository,
									  ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
									  ScoreRepository scoreRepository) {
		return (args) -> {

			Player p1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24"));
			Player p2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
			Player p3 = new Player("kim_bauer@ctu.gov", passwordEncoder().encode("kb"));
			Player p4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));

			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p3);
			playerRepository.save(p4);

			Date date = new Date();
			Date date2 = Date.from(date.toInstant().plusSeconds(3600));
			Date date3 = Date.from(date2.toInstant().plusSeconds(3600));

			Game g1 = new Game(date);
			Game g2 = new Game(date2);
			Game g3 = new Game(date3);

			gameRepository.save(g1);
			gameRepository.save(g2);
			gameRepository.save(g3);

			// game1
			GamePlayer gp1 = new GamePlayer(g1, p1);
			GamePlayer gp2 = new GamePlayer(g1, p2);

			// game2
			GamePlayer gp3 = new GamePlayer(g2, p1);
			GamePlayer gp4 = new GamePlayer(g2, p2);

			// game3
			GamePlayer gp5 = new GamePlayer(g3, p2);
			GamePlayer gp6 = new GamePlayer(g3, p4);

			gpRepository.save(gp1);
			gpRepository.save(gp2);
			gpRepository.save(gp3);
			gpRepository.save(gp4);
			gpRepository.save(gp5);
			gpRepository.save(gp6);

			// ship
			Set<String> shipLocationgp1uno = new HashSet(Arrays.asList("H2","H3","H4"));
			Set<String> shipLocationgp1dos = new HashSet(Arrays.asList("E1","F1","G1"));
			Set<String> shipLocationgp1tres = new HashSet(Arrays.asList("B4","B5"));
			Set<String> shipLocationgp1cuatro = new HashSet(Arrays.asList("B5","C5","D5"));
			Set<String> shipLocationgp1cinco = new HashSet(Arrays.asList("F1","F2"));

			Ship shipUno = new Ship("Destroyer", gp1,shipLocationgp1uno);
			Ship shipDos = new Ship("Submarine", gp1, shipLocationgp1dos);
			Ship shipTres = new Ship("Patrol Boat", gp1, shipLocationgp1tres);
			Ship shipCuatro = new Ship("Destroyer", gp2, shipLocationgp1cuatro);
			Ship shipCinco = new Ship("Patrol Boat", gp2, shipLocationgp1cinco);

			shipRepository.save(shipUno);
			shipRepository.save(shipDos);
			shipRepository.save(shipTres);
			shipRepository.save(shipCuatro);
			shipRepository.save(shipCinco);

			// Salvoes
			Salvo salvo1 = new Salvo (1,shipLocationgp1uno,gp1);
			Salvo salvo2 = new Salvo(1,shipLocationgp1dos,gp2);
			Salvo salvo3 = new Salvo(1,shipLocationgp1tres,gp3);
			Salvo salvo4 = new Salvo(1,shipLocationgp1cuatro,gp4);
			Salvo salvo5 = new Salvo(1,shipLocationgp1cinco,gp5);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);

			// Score

			Score score1 = new Score((float) 0.5, date, p1, g1);
			Score score2 = new Score((float) 0.5, date, p2, g2);
			Score score3 = new Score((float) 1, date, p3, g3);

			scoreRepository.saveAll(Arrays.asList(score1, score2, score3));

		};
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByUserName(inputName).orElse(null);
			if (player != null) {
				return new User(player.getUserName(),player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/game.html").permitAll()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/games").permitAll()
				.antMatchers("/api/players").permitAll()
				.antMatchers("/api/game_view/*").hasAuthority("USER")
				.antMatchers("/rest").denyAll()
				.anyRequest().permitAll();
		http.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}