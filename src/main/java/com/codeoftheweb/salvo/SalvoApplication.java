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
                                      ShipRepository sRepo,
                                      SalvoRepository svRepo,
                                      ScoreRepository scRepo) {
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
            loc1.add("H5");
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
            loc5.add("B3");
            loc5.add("B4");

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

            List<String> loc6 = new ArrayList<>();
            loc6.add("A4");
            loc6.add("B4");
            loc6.add("C4");
            loc6.add("D4");
            loc6.add("E4");
            List<String> loc7 = new ArrayList<>();
            loc7.add("G1");
            loc7.add("G2");
            loc7.add("G3");
            loc7.add("G4");
            List<String> loc8 = new ArrayList<>();
            loc8.add("F5");
            loc8.add("F6");
            loc8.add("F7");
            List<String> loc9 = new ArrayList<>();
            loc9.add("H3");
            loc9.add("H2");
            loc9.add("H1");
            List<String> loc10 = new ArrayList<>();
            loc10.add("J4");
            loc10.add("J5");

            Ship s6 = new Ship("Carrier1",gp2,loc6);
            Ship s7 = new Ship("Battleship1",gp2,loc7);
            Ship s8 = new Ship("Submarine1",gp2,loc8);
            Ship s9 = new Ship("Destroyer1",gp2,loc9);
            Ship s10 = new Ship("Patrol Boat1",gp2,loc10);

            sRepo.save(s6);
            sRepo.save(s7);
            sRepo.save(s8);
            sRepo.save(s9);
            sRepo.save(s10);

            List<String> salvoLoc1 = new ArrayList<>();
            salvoLoc1.add("A1");
            salvoLoc1.add("A4");
            List<String> salvoLoc2 = new ArrayList<>();
            salvoLoc2.add("B2");
            salvoLoc2.add("B4");

            List<String> salvoLoc3 = new ArrayList<>();
            salvoLoc3.add("C4");
            salvoLoc3.add("C8");
            List<String> salvoLoc4 = new ArrayList<>();
            salvoLoc4.add("D7");
            salvoLoc4.add("D1");

            Salvo sv1 = new Salvo(1, gp1, salvoLoc1);
            Salvo sv2 = new Salvo(1,gp2,salvoLoc2);
            Salvo sv3 = new Salvo(2,gp1,salvoLoc3);
            Salvo sv4 = new Salvo(2,gp2, salvoLoc4);

            svRepo.save(sv1);
            svRepo.save(sv2);
            svRepo.save(sv3);
            svRepo.save(sv4);

            Score sc1 = new Score (g1, p1, 1.0f, date);
            scRepo.save(sc1);

        };
    }
}