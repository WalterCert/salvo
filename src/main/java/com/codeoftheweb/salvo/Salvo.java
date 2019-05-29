package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer = new GamePlayer();

    @ElementCollection
    @Column(name="locations")
    private List<String> locations = new ArrayList<>();

    private Integer turn;

    public Salvo(){}

    public Salvo(Integer turn, GamePlayer gamePlayer, List<String> locations){
        this.setTurn(turn);
        this.setGamePlayer(gamePlayer);
        this.setSalvoLocations(locations);
    }

    public Integer getTurn() {
        return turn;
    }
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public List<String> getSalvoLocations() {
        return locations;
    }
    public void setSalvoLocations(List<String> locations) {
        this.locations = locations;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
}
