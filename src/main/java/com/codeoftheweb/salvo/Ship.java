package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer = new GamePlayer();

    @ElementCollection
    @Column(name="locations")
    private Set<String> locations = new HashSet<>();

    private String type;

    public Ship(){}

    public Ship(String type, GamePlayer gamePlayer, Set<String> locations){
        this.setType(type);
        this.setGamePlayer(gamePlayer);
        this.setLocations(locations);
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Set<String> getLocations() {
        return locations;
    }
    public void setLocations(Set<String> locations) {
        this.locations = locations;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
