package com.codeoftheweb.salvo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository pRepo, GameRepository gRepo, GamePlayerRepository gpRepo) {
        return (args) -> {
            // save a couple of player

            Player p1 = new Player("j.bauer@ctu.gov", "24");
            Player p2 = new Player("c.obrian@ctu.gov","42");
            Player p3 = new Player("kim_bauer@gmail.com","kb");
            Player p4 = new Player("t.almeida@ctu.gov","mole");
            pRepo.save(p1);
            pRepo.save(p2);
            pRepo.save(p3);
            pRepo.save(p4);

            Date date = new Date();
            Game g1 = new Game(date);
            Game g2 = new Game(Date.from(date.toInstant().plusSeconds(3600)));
            Game g3 = new Game(Date.from(date.toInstant().plusSeconds(3600*2)));
            gRepo.save(g1);
            gRepo.save(g2);
            gRepo.save(g3);

            GamePlayer gp1 = new GamePlayer(date, g1, p1);
            GamePlayer gp2 = new GamePlayer(date, g1, p2);
            gpRepo.save(gp1);
            gpRepo.save(gp2);

            List<String> loc1 = new ArrayList<String>();
            loc1.add("H1");
            loc1.add("H2");
            loc1.add("H3");

            Ship s1 = new Ship("cruisier",gp1,loc1);



            /*
            pRepo.save(new Player("j.bauer@ctu.gov", "24"));
            pRepo.save(new Player("c.obrian@ctu.gov","42"));
            pRepo.save(new Player("kim_bauer@gmail.com","kb"));
            pRepo.save(new Player("t.almeida@ctu.gov","mole"));

            Date date = new Date();

            gRepo.save(new Game(date));
            gRepo.save(new Game(Date.from(date.toInstant().plusSeconds(3600))));
            gRepo.save(new Game(Date.from(date.toInstant().plusSeconds(3600*2))));

            gpRepo.save(new GamePlayer(new Date(), new Game(), new Player()));
            */

        };


    }



}
