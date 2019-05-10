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
    public CommandLineRunner initData(PlayerRepository pRepo,
                                      GameRepository gRepo,
                                      GamePlayerRepository gpRepo,
                                      ShipRepository sRepo) {
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

            p2.addGamePlayers(gp1);

            List<String> loc1 = new ArrayList<>();
            loc1.add("H4");
            loc1.add("H6");
            loc1.add("H7");
            loc1.add("H8");
            loc1.add("H9");
            List<String> loc2 = new ArrayList<>();
            loc2.add("D1");
            loc2.add("D2");
            loc2.add("D3");
            loc2.add("D4");
            List<String> loc3 = new ArrayList<>();
            loc3.add("E2");
            loc3.add("E3");
            loc3.add("E4");
            List<String> loc4 = new ArrayList<>();
            loc4.add("A1");
            loc4.add("A2");
            loc4.add("A3");
            List<String> loc5 = new ArrayList<>();
            loc2.add("B3");
            loc2.add("B4");

            Ship s1 = new Ship("Carrier",gp1,loc1);
            Ship s2 = new Ship("Battleship",gp1,loc2);
            Ship s3 = new Ship("Submarine",gp1,loc3);
            Ship s4 = new Ship("Destroyer",gp1,loc4);
            Ship s5 = new Ship("Patrol Boat",gp1,loc5);
            sRepo.save(s1);
            sRepo.save(s2);
            sRepo.save(s3);
            sRepo.save(s4);
            sRepo.save(s5);

        };


    }



}
