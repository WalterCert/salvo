package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<Score> scores = new HashSet<>();

    private String userName;
    private String password;

    public Player() { }

    public Player(String name, String pass) {
        this.setUserName(name);
        this.setPassword(pass);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", userName='" + this.getUserName() + '\'' +
                //", password='" + this.getPassword() + '\'' +
                '}';
    }

    @JsonIgnore
    public List<Game> getGames() {
        return gamePlayers.stream().map(sub -> sub.getGame()).collect(toList());
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

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }
    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }
    public void addGamePlayers(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);//A el arg gP le agrego el player.
        gamePlayers.add(gamePlayer);//Al SET de gPlayers le agrego el gp con el Player seteado.
    }

    public Set<Score> getScores() {
        return scores;
    }
    public void setScores(Set<Score> score) {
        this.scores = score;
    }
/*
    public Map<String, Object> getScore(Player player){
        Map<String, Object> dto = new HashMap<>();
        dto.put("Wins", player.getWins(this.scores));
        dto.put("Tied" ,player.getTied(this.scores));
        dto.put("Loses", player.getLoses(this.scores));
        return dto;
    }*/

    public Score getScore (Game game){
        return scores
                .stream()
                .filter(score -> score.getGame().getId() == game.getId())
                .findAny()
                .orElse(null);
    }

    public float getScore(){
        return this.getWins(this.getScores())
                + this.getTied(this.getScores())*(float)0.5
                + this.getLoses(this.getScores())*0;

    }

    public float getWins(Set<Score> scores){
        return scores.stream()
                .filter(s -> s.getScore() == 1.0)
                .count();
    }
    public float getTied(Set<Score> scores){
        return scores.stream()
                .filter(s -> s.getScore() == 0.5)
                .count();
    }
    public float getLoses(Set<Score> scores){
        return scores.stream()
                .filter(s -> s.getScore() == 0.0)
                .count();
    }
}