package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.*;
import java.util.List;

@Entity
public class Salvo{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @JoinColumn(name = "salvoLocations")
    private List<String> locations;

    private int turn;

    public Salvo() {}

    public Salvo(List<String> locations, int turn) {
        this.locations = locations;
        this.turn = turn;
    }

    public Salvo(GamePlayer gamePlayer, List<String> locations, int turn) {
        this.gamePlayer = gamePlayer;
        this.locations = locations;
        this.turn = turn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}