package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Devuelve el Id de cada objeto Game
    @RequestMapping("/gamesId")
    public List<Long> getGameId(){
        return gameRepository
                .findAll()
                .stream()
                .map(b -> b.getId())
                .collect(toList());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String username, @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Falta informacion", HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByUsername(username) !=  null) {
            return new ResponseEntity<>("Nombre en uso", HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
    }

    @RequestMapping("/game_view/{Id}")
    private Map<String, Object> getGPID(@PathVariable Long Id){
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(Id);
        return this.GameDTO(gamePlayer.get().getGame());
    }

    @RequestMapping("/leaderBoard")
    private List<Map<String, Object>> getLeaderBoard(){
        return playerRepository
                .findAll()
                .stream()
                .map(this::makePlayerDTO)
                .collect(toList());
    }

    @RequestMapping("/games")
    public Map<String, Object> makeLoggedPlayer (Authentication authentication){
        Map<String, Object> dto = new HashMap<>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        if (authenticatedPlayer == null)
            dto.put("player", "Guest");
        else
            dto.put("player", makePlayerDTO(authenticatedPlayer));
        dto.put("games" , this.getGames());

        return dto;
    }
    private Player getAuthentication(Authentication authentication){
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }else{
            return(playerRepository.findByUsername(authentication.getName()));
        }
    }

    public List<Object> getGames() {
        return gameRepository
                .findAll()
                .stream()
                .map(this::GameDTO)
                .collect(toList());
    }

    private Map<String, Object> GameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate());
        dto.put("scores", getScoreList(game.getScores()));
        dto.put("gamePlayers", makeGamePlayerList(game.getGamePlayers()));

        //dto.put("salvoes", getSalvoesList(game));
        return dto;
    }

    private List<Map<String, Object>> makeGamePlayerList(Set<GamePlayer> gamePlayers){
        return gamePlayers
                .stream()
                .map(this::makeGamePlayerDTO)
                .collect(toList());
    }
    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", gamePlayer.getId());
        //dto.put("created", gamePlayer.getDate());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("email", player.getUsername());
        //dto.put("scores", makeScoreDTO(player));
        return dto;
    }

    private List<Map<String,Object>> getScoreList(Set<Score> scores){
        return scores
                .stream()
                .map(this::scoreDTO)
                .collect(toList());
    }
    private Map<String,Object> scoreDTO (Score score){
        Map<String, Object> dto = new HashMap<>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("email",score.getPlayer().getUsername());
        //dto.put("finishDate", score.getFinishDate());
        return dto;
    }
    private Map<String, Object> makeScoreDTO(Player player){
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", player.getUsername());
        dto.put("total", player.getScoreTotal());
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost",player.getLoses(player.getScores()));
        dto.put("tied",player.getTied(player.getScores()));

        return dto;
    }//Muestra dto de name, total, won, lost, tied. Solamente.

    private List<Map<String, Object>> getSalvoesList(Game game){
        List<Map<String,Object>> myList = new ArrayList<>();
        game
        .getGamePlayers()
        .forEach(gamePlayer -> myList
        .addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }
    private List<Map<String, Object>> makeSalvoList(Set<Salvo> salvoes){
        return salvoes
                .stream()
                .map(this::makeSalvoDTO)
                .collect(toList());
    }
    private Map<String, Object> makeSalvoDTO(Salvo salvo){
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", salvo.getId());
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }






}
