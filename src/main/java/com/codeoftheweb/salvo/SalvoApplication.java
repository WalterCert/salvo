package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

    @Autowired
    PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository pRepo,
                                      GameRepository gRepo,
                                      GamePlayerRepository gpRepo,
                                      ShipRepository sRepo,
                                      SalvoRepository svRepo,
                                      ScoreRepository scRepo) {
        // A partir de aca es MUCHO copy-paste para instanciar y guardar player y demás cosas.
        return (args) -> {


            Player p1 = new Player("j.bauer@ctu.gov", passwordEncoder.encode("24"));
            Player p2 = new Player("c.obrian@ctu.gov",passwordEncoder.encode("42"));
            Player p3 = new Player("kim_bauer@gmail.com",passwordEncoder.encode("kb"));
            Player p4 = new Player("t.almeida@ctu.gov",passwordEncoder.encode("mole"));

            pRepo.save(p1);
            pRepo.save(p2);
            pRepo.save(p3);
            pRepo.save(p4);

            Date date = new Date();
            Game g1 = new Game(date);
            gRepo.save(g1);
            GamePlayer gp1 = new GamePlayer(date, g1, p1);
            GamePlayer gp2 = new GamePlayer(date, g1, p2);
            gpRepo.save(gp1);
            gpRepo.save(gp2);
            /*Game g2 = new Game(Date.from(date.toInstant().plusSeconds(3600)));
            Game g3 = new Game(Date.from(date.toInstant().plusSeconds(3600*2)));
            Game g4 = new Game(Date.from(date.toInstant().plusSeconds(3600*3)));

            gRepo.save(g2);
            gRepo.save(g3);
            gRepo.save(g4);


            GamePlayer gp3 = new GamePlayer(date, g2, p1);
            GamePlayer gp4 = new GamePlayer(date, g2, p2);
            gpRepo.save(gp3);
            gpRepo.save(gp4);
            GamePlayer gp5 = new GamePlayer(date, g3, p3);
            GamePlayer gp6 = new GamePlayer(date, g3, p4);
            gpRepo.save(gp5);
            gpRepo.save(gp6);
            GamePlayer gp7 = new GamePlayer(date, g4, p2);
            GamePlayer gp8 = new GamePlayer(date, g4, p1);
            gpRepo.save(gp7);
            gpRepo.save(gp8);

            List <String> loc1 = new ArrayList<>();
            loc1.add("H5");
            loc1.add("H6");
            loc1.add("H7");
            loc1.add("H8");
            loc1.add("H9");
            List <String> loc2 = new ArrayList<>();
            loc2.add("D1");
            loc2.add("D2");
            loc2.add("D3");
            loc2.add("D4");
            List <String> loc3 = new ArrayList<>();
            loc3.add("E2");
            loc3.add("E3");
            loc3.add("E4");
            List <String> loc4 = new ArrayList<>();
            loc4.add("A1");
            loc4.add("A2");
            loc4.add("A3");
            List <String> loc5 = new ArrayList<>();
            loc5.add("B3");
            loc5.add("B4");

            Ship s1 = new Ship("carrier",gp1,loc1);
            Ship s2 = new Ship("battleship",gp1,loc2);
            Ship s3 = new Ship("submarine",gp1,loc3);
            Ship s4 = new Ship("destroyer",gp1,loc4);
            Ship s5 = new Ship("patrolboat",gp1,loc5);

            sRepo.save(s1);
            sRepo.save(s2);
            sRepo.save(s3);
            sRepo.save(s4);
            sRepo.save(s5);

            List <String> loc6 = new ArrayList<>();
            loc6.add("A4");
            loc6.add("B4");
            loc6.add("C4");
            loc6.add("D4");
            loc6.add("E4");
            List <String> loc7 = new ArrayList<>();
            loc7.add("G1");
            loc7.add("G2");
            loc7.add("G3");
            loc7.add("G4");
            List <String> loc8 = new ArrayList<>();
            loc8.add("F5");
            loc8.add("F6");
            loc8.add("F7");
            List <String> loc9 = new ArrayList<>();
            loc9.add("H3");
            loc9.add("H2");
            loc9.add("H1");
            List <String> loc10 = new ArrayList<>();
            loc10.add("I4");
            loc10.add("J4");

            Ship s6 = new Ship("carrier",gp2,loc6);
            Ship s7 = new Ship("battleship",gp2,loc7);
            Ship s8 = new Ship("submarine",gp2,loc8);
            Ship s9 = new Ship("destroyer",gp2,loc9);
            Ship s10 = new Ship("patrolboat",gp2,loc10);

            sRepo.save(s6);
            sRepo.save(s7);
            sRepo.save(s8);
            sRepo.save(s9);
            sRepo.save(s10);

            List <String> salvoLoc1 = new ArrayList<>();
            salvoLoc1.add("A1");
            salvoLoc1.add("A2");
            salvoLoc1.add("A3");
            salvoLoc1.add("A4");
            salvoLoc1.add("A5");

            List <String> salvoLoc3 = new ArrayList<>();
            salvoLoc3.add("F9");
            salvoLoc3.add("G9");
            salvoLoc3.add("H9");
            salvoLoc3.add("I9");
            salvoLoc3.add("J9");

            Salvo sv1 = new Salvo(1, gp1, salvoLoc1);
            Salvo sv2 = new Salvo(1,gp2,salvoLoc3);

            svRepo.save(sv1);
            svRepo.save(sv2);

            Score sc1 = new Score (g1, p1, 1.0f, date);
            Score sc2 = new Score (g2, p1, 0.5f, date);
            Score sc3 = new Score (g4, p1, 1.0f, date);
            scRepo.save(sc1);
            scRepo.save(sc2);
            scRepo.save(sc3);

            Score sc4 = new Score(g1, p2, 0.0f, date);
            Score sc5 = new Score(g2, p2, 0.5f, date);
            Score sc6 = new Score(g3, p2, 1.0f, date);
            Score sc7 = new Score(g4, p2, 0.5f, date);
            scRepo.save(sc4);
            scRepo.save(sc5);
            scRepo.save(sc6);
            scRepo.save(sc7);

            Score sc8 = new Score(g3, p3, 0.0f, date);
            scRepo.save(sc8);*/
        };
    }
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username ->
        {
            Player player = playerRepository.findByUsername(username);
            if (player != null) {
                return new User(player.getUsername(),
                        player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + username);
            }
        });
    }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers( "/web/games_3.html").permitAll()
                .antMatchers( "/web/**").permitAll()
                .antMatchers( "/api/games.").permitAll()
                .antMatchers( "/api/players").permitAll()
                .antMatchers( "/api/game_view/*").hasAuthority("USER")
                .antMatchers( "/rest/*").denyAll()
                .anyRequest().permitAll();

        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        //permite entrar a la Database.
        http.headers().frameOptions().sameOrigin();

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // si el usuario no esta autenticado, se envía una respuesta de "Falla de autenticación"
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // si el logueo es satisfacctorio, se limpian las banderas (flags) que preguntan por autenticación
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // si el logueo falla, se envia un fallo de autenticación
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // si el LOGOUT es satisfacctorio, se envía una respuesta de logOUT satisfactorio.
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
/*
    //Comando de consola para activar jQuery
    // ... give time for script to load, then type (or see below for non wait option)

    var jq = document.createElement('script');
    jq.src = "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js";
    document.getElementsByTagName('head')[0].appendChild(jq);
    jQuery.noConflict();

    $.post("/api/login", { userName: "j.bauer@ctu.gov", password: "24" }).done(function() { console.log("logged in!"); })
*/