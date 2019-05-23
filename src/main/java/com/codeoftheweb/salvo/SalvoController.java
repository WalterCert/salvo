package com.codeoftheweb.salvo;

import org.apache.coyote.Response;
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
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;

    @RequestMapping("/gamesId")
    public List<Long> getGameId(){
        return gameRepository
                .findAll()
                .stream()
                .map(b -> b.getId())
                .collect(toList());
    }

    //C5.T1: Metodo de registro de nuevo jugador
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String username,
                                           @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Falta informacion", HttpStatus.FORBIDDEN);
        }else
        if (playerRepository.findByUsername(username) !=  null) {
            return new ResponseEntity<>("Nombre en uso", HttpStatus.FORBIDDEN);
        }else
            playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
    }

    //C5 T2: Metodos "Create Game" y "Join Game"
    // GET api/games/{Id}/players
    @RequestMapping(path = "/game/{Id}/players")
    private List<Map<String, Object>> getPlayersGames(@PathVariable Long Id){
        return (gameRepository
                .findById(Id)).get().getPlayers()
                .stream()
                .map(this::makePlayerDTO)
                .collect(toList());
    }


    //-------------------------Target: Join Game-Completo--------------------------------------------------------------|
    @RequestMapping(path = "/game/{Id}/players", method = RequestMethod.POST)
    private ResponseEntity<Object> newLoggedPlayerInGame(@PathVariable Long Id,
                                                         Authentication authentication){
        Player loggedPlayer = getAuthentication(authentication);
        Game existingGame = gameRepository.findById(Id).orElse(null);

        if(loggedPlayer == null)
            return new ResponseEntity<>("Error: Usuario no loggeado", HttpStatus.UNAUTHORIZED);
        else if(existingGame == null)
            return new ResponseEntity<>("Error: El juego no existe" ,HttpStatus.FORBIDDEN);
        else if (existingGame.getGamePlayers().size() >= 2)
            return new ResponseEntity<>("Error:Juego Lleno" ,HttpStatus.FORBIDDEN);
        else {
            GamePlayer gpAux = new GamePlayer(new Date(), existingGame, loggedPlayer);
            gamePlayerRepository.save(gpAux);
            return new ResponseEntity<>(Map.of("gpid", gpAux.getId()),HttpStatus.CREATED);
        }
    }
    /*private Map<String,Object> makeErrorMap(String message){
        Map<String,Object> msgMap = new LinkedHashMap<>();
        msgMap.put("Error :",message);
        return msgMap;
    }//Permite generar un ResponseEntity<Map<String, Object>>*/

    //-------------------------Target: Create Game---------------------------------------------------------------------|
    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Object> newGame(Authentication authentication) {

        Player loggedPlayer = getAuthentication(authentication);
        if (loggedPlayer == null)
            return new ResponseEntity<>("No esta loggeado", HttpStatus.FORBIDDEN);
        else{
            Game gAux = new Game(new Date());
            gameRepository.save(gAux);
            GamePlayer gpAux = new GamePlayer(new Date(), gAux, loggedPlayer);
            gamePlayerRepository.save(gpAux);
            return new ResponseEntity<>(Map.of("gpid",gpAux.getId()),HttpStatus.CREATED);
        }
    }
    //---------------------------------GET /api/games------------------------------------------------------------------|
    @RequestMapping("/games")
    private Map<String, Object> makeLoggedPlayer (Authentication authentication){
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
    }//Metodo ver si un player esta autenticado.

    //-------------------------Target: GET & POST Place Ships----------------------------------------------------------|
    //C5 T3: POST Place Ships & GET Placed Ships
    @RequestMapping(path = "/games/players/{gpid}/ships", method = RequestMethod.POST)
    private ResponseEntity<Object> setShipsPlayer (@PathVariable long gpid,
                                                               @RequestBody Set<Ship> ships,
                                                               Authentication authentication){

        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpActual = gamePlayerRepository.findById(gpid);

        if(loggedPlayer == null)
            return new ResponseEntity<>(Map.of("Error:", "No esta loggeado"), HttpStatus.UNAUTHORIZED);
        else if (gpActual == null)
            return new ResponseEntity<>(Map.of("Error:", "No existe el juego"),HttpStatus.UNAUTHORIZED);
        else if(wrongGamePlayer(gpActual,loggedPlayer))
            return new ResponseEntity<>(Map.of("Error", "Juego Incorrecto"),HttpStatus.UNAUTHORIZED);
        else if(gpActual.getShips().isEmpty()){
            ships.stream().forEach(b -> b.setGamePlayer(gpActual));
            gpActual.setShips(ships);
            shipRepository.saveAll(ships);
            return new ResponseEntity<>("Ships creados", HttpStatus.CREATED);
        }
        else
            return new ResponseEntity<>(Map.of("Error:", "El jugador ya tiene ships."), HttpStatus.FORBIDDEN);
    }
    //GET SHIPS
    @RequestMapping("/games/players/{gpid}/ships")
    private Map<String, Object> getPlacedShips (@PathVariable long gpid,
                                                Authentication authentication){
        Map<String, Object> dto = new HashMap<>();
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if (loggedPlayer == null)
            dto.put("player", "Guest");
        else if(gpAux == null)
            dto.put("Error","No existe gameplayer");
        else if(wrongGamePlayer(gpAux,loggedPlayer))
            dto.put("Error","Player equivocado");
        else
            dto.put("ships", makeShipList(gpAux.getShips()));

        return dto;
    }
    /*
    @RequestMapping("/games/players/{gpid}/ships")
    private ResponseEntity<Map<String,Object>> getShipsPlaced(Authentication authentication, @RequestParam long gpid){
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if(loggedPlayer == null)
            return new ResponseEntity<>(Map.of("Error:", "No esta loggeado"), HttpStatus.UNAUTHORIZED);
        else if (gpAux == null)
            return new ResponseEntity<>(Map.of("Error:", "No existe el juego"),HttpStatus.UNAUTHORIZED);
        else if(wrongGamePlayer(gpAux,loggedPlayer))
            return new ResponseEntity<>(Map.of("Error", "Juego Incorrecto"),HttpStatus.UNAUTHORIZED);
        else{
            return new ResponseEntity<>(Map.of("Ships:",gpAux.getShips()),HttpStatus.ACCEPTED);
        }
    }*/

    //C5 T4: Add Firing Salvoes

    //----------------------Target: Agregar Salvoes--------------------------------------------------------------------|
    @RequestMapping(path = "/games/players/{gpid}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes (@RequestBody Salvo salvo,
                                             @PathVariable long gpid,
                                             Authentication authentication  ) {
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if(loggedPlayer == null)
            return new ResponseEntity<>(Map.of("Error:", "No esta loggeado"), HttpStatus.UNAUTHORIZED);
        else if (gpAux == null)
            return new ResponseEntity<>(Map.of("Error:", "No existe el juego"),HttpStatus.UNAUTHORIZED);
        else if(wrongGamePlayer(gpAux,loggedPlayer))
            return new ResponseEntity<>(Map.of("Error", "Juego Incorrecto"),HttpStatus.UNAUTHORIZED);
        else {
            if (!hasTurnedSalvo(salvo, gpAux.getSalvoes())) {
                gpAux.addSalvo(salvo);
                salvo.setGamePlayer(gpAux);
                salvoRepository.save(salvo);
                return new ResponseEntity<>("Salvo saved", HttpStatus.CREATED);
            } else
                return new ResponseEntity<>("Error: El jugador ya tiene salvoes.", HttpStatus.FORBIDDEN);
        }
    }
    private boolean hasTurnedSalvo(Salvo newSalvo,Set<Salvo> salvosGameplayer) {
        boolean hasSalvoes = false;
        for (Salvo salvo : salvosGameplayer) {
            if (salvo.getTurn() == newSalvo.getTurn())
                hasSalvoes = true;
        }
        return hasSalvoes;
    }
    //GET SALVOES
    @RequestMapping("/games/players/{gpid}/salvoes")
    private Map<String, Object> getSalvoes (@PathVariable long gpid,
                                                Authentication authentication){
        Map<String, Object> dto = new HashMap<>();
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if (loggedPlayer == null)
            dto.put("player", "Guest");
        else if(gpAux == null)
            dto.put("Error","No existe gameplayer");
        else if(wrongGamePlayer(gpAux,loggedPlayer))
            dto.put("Error","Player equivocado");
        else
            dto.put("salvoes", makeSalvoList(gpAux.getSalvoes()));

        return dto;
    }

    //--------------------GAME VIEW/ID-----------------------------------------------------------|
    @RequestMapping("/game_view/{Id}")
    private Map<String, Object> getGameView(@PathVariable long Id){
        GamePlayer gamePlayer = gamePlayerRepository.findById(Id);
        return this.makeGameDTO(gamePlayer.getGame());
    }

    @RequestMapping("/leaderBoard")
    private List<Map<String, Object>> getLeaderBoard(){
        return playerRepository
                .findAll()
                .stream()
                .map(this::makePlayerDTO)
                .collect(toList());
    }

    //------------------------------ZONA DE DTO's----------------------------------|
    public List<Object> getGames() {
        return gameRepository
                .findAll()
                .stream()
                .map(this::makeGameDTO)
                .collect(toList());
    }
    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getId());
        dto.put("creationDate", game.getCreationDate());
        dto.put("gamePlayers", makeGamePlayerList(game.getGamePlayers()));
        dto.put("ships", getShipList(game));
        dto.put("salvoes", getSalvoesList(game));
        dto.put("hits","getHitsPerTurn");
        dto.put("scores", getScoreList(game.getScores()));

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
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("joinDate", gamePlayer.getDate());

        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("email", player.getUsername());
        return dto;
    }
    /*private List<Map<String, Object>> makePlayerList(Set<Player> players){
            return players
                    .stream()
                    .map(this::makePlayerDTO)
                    .collect(toList());
        }*/

    private List<Map<String, Object>> getShipList(Game game){
        List<Map<String,Object>> myList = new ArrayList<>();
        game.getGamePlayers().stream().anyMatch(gp -> myList.addAll(makeShipList(gp.getShips()) ) );
        return myList;
    }
    private List<Map<String, Object>> makeShipList(Set<Ship> ships){
        return ships
                .stream()
                .map(this::makeShipDTO)
                .collect(toList());
    }//Obtiene una Lista de los ships de este Player unicamente.
    private Map<String, Object> makeShipDTO (Ship ship){
        Map <String,Object> dto = new HashMap<>();
        dto.put("type",ship.getType());
        dto.put("locations",ship.getLocations());
        return dto;
    }

    private List<Map<String, Object>> getSalvoesList(Game game){
        List<Map<String,Object>> myList = new ArrayList<>();
        game
                .getGamePlayers()
                .forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
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
        //dto.put("id", salvo.getId());
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    private List<Map<String,Object>> getScoreList(Set<Score> scores){
        return scores
                .stream()
                .map(this::makeScoreDTO)
                .collect(toList());
    }
    private Map<String,Object> makeScoreDTO(Score score){
        Map<String, Object> dto = new HashMap<>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("email",score.getPlayer().getUsername());
        //dto.put("finishDate", score.getFinishDate());
        return dto;
    }
    private Map<String, Object> makeScorePlayer(Player player){
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", player.getUsername());
        dto.put("total", player.getScoreTotal());
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost",player.getLoses(player.getScores()));
        dto.put("tied",player.getTied(player.getScores()));

        return dto;
    }//Muestra dto de name, total, won, lost, tied.

    //Comprueba si el gameplayer es el correcto par ese juego
    private boolean wrongGamePlayer(GamePlayer gamePlayer, Player player){
        boolean corretGP = gamePlayer.getPlayer().getId() != player.getId();
        return corretGP;
    }

    //C5 T5: Target: Hits & Sink

    private Map<String, Object> makeHitsDTO (Game game){
        Map<String, Object> dto = new HashMap<>();

        Set<Ship> shipsAux = new HashSet<>();
        Set<Salvo> salvoesAux = new HashSet<>();

        /* poner comprobaciones*/
        if(game.getGamePlayers().size() == 2) {
            for (GamePlayer gp : game.getGamePlayers()) {
                shipsAux.addAll(gp.getShips());
                salvoesAux.addAll(gp.getSalvoes());
            }
        }

        for(Salvo salvo:salvoesAux){
            for(Ship ship:shipsAux){

                if(salvo.getGamePlayer().getId() != ship.getGamePlayer().getId())

            }
        }


        return dto;
    }
}
