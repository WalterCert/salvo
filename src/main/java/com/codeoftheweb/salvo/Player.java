package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers = new HashSet<>();

    private String userName;
    private String password;

    public Player() { }

    public Player(String name, String pass) {
        this.setUserName(name);
        this.setPassword(pass);
    }

    public void addGamePlayers(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);//A el arg gP le agrego el player.
        gamePlayers.add(gamePlayer);//Al SET de gPlayers le agrego el gp con el Player seteado.
    }

    @JsonIgnore
    public List<Game> getGames() {
        return gamePlayers.stream().map(sub -> sub.getGame()).collect(toList());
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", userName='" + this.getUserName() + '\'' +
                //", password='" + this.getPassword() + '\'' +
                '}';
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }



}