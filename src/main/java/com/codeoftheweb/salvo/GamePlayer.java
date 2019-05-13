package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="Player_id")
    private Player player = new Player();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="Game_id")
    private Game game = new Game();

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private Set<Ship> ships = new HashSet<>();
<<<<<<< HEAD

    @OneToMany(mappedBy="gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvoes = new HashSet<>();
=======
>>>>>>> 62aa259c30b0a1d7126113f7a668d2f6d146ebf6

    private Date date = new Date();

    public GamePlayer(){ }

    public GamePlayer(Date d, Game g, Player p){
        this.setDate(d);
        this.setGame(g);
        this.setPlayer(p);
<<<<<<< HEAD
    }

    public Set<Salvo> getSalvoes() {
        return salvoes;
    }
    public void addSalvo(Salvo salvo) {
        salvo.setGamePlayer(this);
        salvoes.add(salvo);
=======
    }

    public Set<Ship> getShips() {
        return ships;
    }
    public void setShips(Set<Ship> ships) {
        this.ships = ships;
>>>>>>> 62aa259c30b0a1d7126113f7a668d2f6d146ebf6
    }

    public Set<Ship> getShips() {
        return ships;
    }
    public void addShip(Ship ship){
            ship.setGamePlayer(this);
            ships.add(ship);
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }
    public void setGame(Game game) {
        this.game = game;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}
