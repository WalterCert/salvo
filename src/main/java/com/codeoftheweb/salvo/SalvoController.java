package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    //Devuelve el Id de cada objeto Game
    @RequestMapping("/gamesId")
    public List<Long> getGameId(){
        return gameRepository.findAll().stream().map(b -> b.getId()).collect(toList());
    }

    //Devuelve el los Objetos Game
    @RequestMapping("/games")
    public List<Object> getGames() {
        return gamePlayerRepository.findAll().stream().map(game -> GameDTO(game)).collect(toList());
    }

    @RequestMapping("/game_view/{Id}")
    private Map<String, Object> getGPID(@PathVariable Long Id){
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(Id);
        return this.GameDTO(gamePlayer.get());
    }

    private List<Map<String, Object>> getGamePlayerList(Set<GamePlayer> gamePlayers){
        return gamePlayers
                .stream()
                .map(gamePlayer -> GamePlayerDTO(gamePlayer))
                .collect(toList());
    }

    private Map<String, Object> GameDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("creationDate", gamePlayer.getGame().getCreationDate());
        dto.put("gamePlayers", getGamePlayerList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", gamePlayer.getShips());

        return dto;
    }

    private Map<String, Object> GamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getId());
        dto.put("created", gamePlayer.getDate());
        dto.put("player", PlayerDTO(gamePlayer.getPlayer()));
        return dto;
    }

    private Map<String, Object> PlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("email", player.getUserName());
        return dto;
    }


}
