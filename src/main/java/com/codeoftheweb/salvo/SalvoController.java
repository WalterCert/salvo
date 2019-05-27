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

import static java.util.stream.Collectors.*;

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
    public List<Long> getGameId() {
        return gameRepository
                .findAll()
                .stream()
                .map(Game::getId)
                .collect(toList());
    }

    //C5.T1: Metodo de registro de nuevo jugador
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestParam String username,
                                           @RequestParam String password) {

        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Falta informacion", HttpStatus.FORBIDDEN);
        } else if (playerRepository.findByUsername(username) != null) {
            return new ResponseEntity<>("Nombre en uso", HttpStatus.FORBIDDEN);
        } else
            playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
    }

    //C5 T2: Metodos "Create Game" y "Join Game"
    // GET api/games/{Id}/players
    @RequestMapping(path = "/game/{Id}/players")
    private List<Map<String, Object>> getPlayersGames(@PathVariable Long Id) {
        return (gameRepository
                .findById(Id)).get().getPlayers()
                .stream()
                .map(this::makePlayerDTO)
                .collect(toList());
    }


    //-------------------------Target: Join Game-Completo--------------------------------------------------------------|
    @RequestMapping(path = "/game/{Id}/players", method = RequestMethod.POST)
    private ResponseEntity<Object> newLoggedPlayerInGame(@PathVariable Long Id,
                                                         Authentication authentication) {
        Player loggedPlayer = getAuthentication(authentication);
        Game existingGame = gameRepository.findById(Id).orElse(null);

        if (loggedPlayer == null)
            return new ResponseEntity<>("Error: Usuario no loggeado", HttpStatus.UNAUTHORIZED);
        else if (existingGame == null)
            return new ResponseEntity<>("Error: El juego no existe", HttpStatus.FORBIDDEN);
        else if (existingGame.getGamePlayers().size() >= 2)
            return new ResponseEntity<>("Error:Juego Lleno", HttpStatus.FORBIDDEN);
        else {
            GamePlayer gpAux = new GamePlayer(new Date(), existingGame, loggedPlayer);
            gamePlayerRepository.save(gpAux);
            return new ResponseEntity<>(Map.of("gpid", gpAux.getId()), HttpStatus.CREATED);
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
        else {
            Game gAux = new Game(new Date());
            gameRepository.save(gAux);
            GamePlayer gpAux = new GamePlayer(new Date(), gAux, loggedPlayer);
            gamePlayerRepository.save(gpAux);
            return new ResponseEntity<>(Map.of("gpid", gpAux.getId()), HttpStatus.CREATED);
        }
    }

    //---------------------------------GET /api/games------------------------------------------------------------------|
    @RequestMapping("/games")
    private Map<String, Object> makeLoggedPlayer() {
        Map<String, Object> dto = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        if (authenticatedPlayer == null)
            dto.put("player", "Guest");
        else
            dto.put("player", makePlayerDTO(authenticatedPlayer));
        dto.put("games", this.getGames());

        return dto;
    }

    private Player getAuthentication(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return (playerRepository.findByUsername(authentication.getName()));
        }
    }//Metodo ver si un player esta autenticado.

    //-------------------------Target: GET & POST Place Ships----------------------------------------------------------|
    //C5 T3: POST Place Ships & GET Placed Ships
    @RequestMapping(path = "/games/players/{gpid}/ships", method = RequestMethod.POST)
    private ResponseEntity<Object> setShipsPlayer(@PathVariable long gpid, @RequestBody Set<Ship> ships,
                                                  Authentication authentication) {

        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpActual = gamePlayerRepository.findById(gpid);

        if (loggedPlayer == null)
            return new ResponseEntity<>(Map.of("Error:", "No esta loggeado"), HttpStatus.UNAUTHORIZED);
        else if (gpActual == null)
            return new ResponseEntity<>(Map.of("Error:", "No existe el juego"), HttpStatus.UNAUTHORIZED);
        else if (wrongGamePlayer(gpActual, loggedPlayer))
            return new ResponseEntity<>(Map.of("Error", "Juego Incorrecto"), HttpStatus.UNAUTHORIZED);
        else if (gpActual.getShips().isEmpty()) {
            ships.forEach(b -> b.setGamePlayer(gpActual));
            gpActual.setShips(ships);
            shipRepository.saveAll(ships);
            return new ResponseEntity<>("Ships creados", HttpStatus.CREATED);
        } else
            return new ResponseEntity<>(Map.of("Error:", "El jugador ya tiene ships."), HttpStatus.FORBIDDEN);
    }

    //GET SHIPS
    @RequestMapping("/games/players/{gpid}/ships")
    private Map<String, Object> getPlacedShips(@PathVariable long gpid, Authentication authentication) {
        Map<String, Object> dto = new HashMap<>();
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if (loggedPlayer == null)
            dto.put("player", "Guest");
        else if (gpAux == null)
            dto.put("Error", "No existe gameplayer");
        else if (wrongGamePlayer(gpAux, loggedPlayer))
            dto.put("Error", "Player equivocado");
        else
            dto.put("ships", makeShipList(gpAux.getShips()));

        return dto;
    }

    //C5 T4: Add Firing Salvoes

    //----------------------Target: Agregar Salvoes--------------------------------------------------------------------|
    @RequestMapping(path = "/games/players/{gpid}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Object> addSalvoes(@RequestBody Salvo salvo, @PathVariable long gpid,
                                             Authentication authentication) {
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if (loggedPlayer == null)
            return new ResponseEntity<>(Map.of("Error:", "No esta loggeado"), HttpStatus.UNAUTHORIZED);
        else if (gpAux == null)
            return new ResponseEntity<>(Map.of("Error:", "No existe el juego"), HttpStatus.UNAUTHORIZED);
        else if (wrongGamePlayer(gpAux, loggedPlayer))
            return new ResponseEntity<>(Map.of("Error", "Juego Incorrecto"), HttpStatus.UNAUTHORIZED);
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
    private boolean hasTurnedSalvo(Salvo newSalvo, Set<Salvo> salvosGameplayer) {
        boolean hasSalvoes = false;
        for (Salvo salvo : salvosGameplayer) {
            if (salvo.getTurn().equals(newSalvo.getTurn()))
                hasSalvoes = true;
        }
        return hasSalvoes;
    }

    //GET SALVOES
    @RequestMapping("/games/players/{gpid}/salvoes")
    private Map<String, Object> getSalvoes(@PathVariable long gpid, Authentication authentication) {
        Map<String, Object> dto = new HashMap<>();
        Player loggedPlayer = getAuthentication(authentication);
        GamePlayer gpAux = gamePlayerRepository.findById(gpid);

        if (loggedPlayer == null)
            dto.put("player", "Guest");
        else if (gpAux == null)
            dto.put("Error", "No existe gameplayer");
        else if (wrongGamePlayer(gpAux, loggedPlayer))
            dto.put("Error", "Player equivocado");
        else
            dto.put("salvoes", makeSalvoList(gpAux.getSalvoes()));

        return dto;
    }

    @RequestMapping("/leaderBoard")
    private List<Map<String, Object>> getLeaderBoard() {
        return playerRepository
                .findAll()
                .stream()
                .map(this::makePlayerDTO)
                .collect(toList());
    }

    //------------------------------ZONA DE DTO's----------------------------------|
    private List<Object> getGames() {
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
        dto.put("hits", getHitsList(game.getGamePlayers().iterator().next(),
                                    game.getGamePlayers().iterator().next()));
        dto.put("scores", getScoreList(game.getScores()));
        dto.put("gameState", "PLAY");

        return dto;
    }

    private List<Map<String, Object>> makeGamePlayerList(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                .map(this::makeGamePlayerDTO)
                .collect(toList());
    }

    private Map<String, Object> makeGamePlayerDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));

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

    private List<Map<String, Object>> getShipList(Game game) {
        List<Map<String, Object>> myList = new ArrayList<>();
        game.getGamePlayers().stream().anyMatch(gp -> myList.addAll(makeShipList(gp.getShips())));
        return myList;
    }

    private List<Map<String, Object>> makeShipList(Set<Ship> ships) {
        return ships
                .stream()
                .map(this::makeShipDTO)
                .collect(toList());
    }//Lista de los ships de este Player.

    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    private List<Map<String, Object>> getSalvoesList(Game game) {
        List<Map<String, Object>> myList = new ArrayList<>();
        game
                .getGamePlayers()
                .forEach(gamePlayer -> myList.addAll(makeSalvoList(gamePlayer.getSalvoes())));
        return myList;
    }

    private List<Map<String, Object>> makeSalvoList(Set<Salvo> salvoes) {
        return salvoes
                .stream()
                .map(this::makeSalvoDTO)
                .collect(toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new HashMap<>();
        //dto.put("id", salvo.getId());
        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getPlayer().getId());
        dto.put("locations", salvo.getSalvoLocations());
        return dto;
    }

    private List<Map<String, Object>> getScoreList(Set<Score> scores) {
        return scores
                .stream()
                .map(this::makeScoreDTO)
                .collect(toList());
    }

    private Map<String, Object> makeScoreDTO(Score score) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("email", score.getPlayer().getUsername());
        //dto.put("finishDate", score.getFinishDate());
        return dto;
    }

    private Map<String, Object> makeScorePlayer(Player player) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", player.getUsername());
        dto.put("total", player.getScoreTotal());
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLoses(player.getScores()));
        dto.put("tied", player.getTied(player.getScores()));

        return dto;
    }//Muestra dto de name, total, won, lost, tied.

    private Map<String, Object> getHitsList(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self", getHits(self, opponent));
        dto.put("opponent", getHits(opponent, self));
        return dto;
    }

    private List<Map> getHits(GamePlayer self, GamePlayer opponent){
        List<Map> dto = new ArrayList<>();

        Set<String> carrierLocations = new HashSet<>();
        Set<String> destroyerLocations = new HashSet<>();
        Set<String> submarineLocations = new HashSet<>();
        Set<String> patrolboatLocations = new HashSet<>();
        Set<String> battleshipLocations = new HashSet<>();

        //Selector de tipo de ship.
        for (Ship ship : self.getShips()) {
            switch (ship.getLocations().size()) {
                case 5:
                    carrierLocations = ship.getLocations();
                    break;
                case 4:
                    battleshipLocations = ship.getLocations();
                    break;
                case 3:
                    if (ship.getType().equals("destroyer")) {
                        destroyerLocations = ship.getLocations();
                        break;
                    } else {
                        submarineLocations = ship.getLocations();
                        break;
                    }
                case 2: {
                    patrolboatLocations = ship.getLocations();
                    break;
                }
            }
        }//Fin bucle selector de ships

        //Declaro acumuladores de daño total recibido.
        int carrierDamage = 0;
        int destroyerDamage = 0;
        int patrolboatDamage = 0;
        int submarineDamage = 0;
        int battleshipDamage = 0;

        //Funcion de contador de daño po turno.
        for (Salvo salvo : opponent.getSalvoes()) {

            //-----------------------------------------------Contadores de daño hecho por turno.
            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;
            //-----------------------------------------------Contador de tiros fallados.
            Integer missedShots = salvo.getSalvoLocations().size();

            List<String> salvoLocationsList = new ArrayList<>(salvo.getSalvoLocations());

            List<String> hitCellsList = new ArrayList<>();

            for (String salvoShot : salvoLocationsList) {
                if (carrierLocations.contains(salvoShot)) {
                    carrierDamage++;
                    carrierHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (battleshipLocations.contains(salvoShot)) {
                    battleshipDamage++;
                    battleshipHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (submarineLocations.contains(salvoShot)) {
                    submarineDamage++;
                    submarineHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (destroyerLocations.contains(salvoShot)) {
                    destroyerDamage++;
                    destroyerHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (patrolboatLocations.contains(salvoShot)) {
                    patrolboatDamage++;
                    patrolboatHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
            }

            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();

            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
            damagesPerTurn.put("carrier", carrierDamage);
            damagesPerTurn.put("battleship", battleshipDamage);
            damagesPerTurn.put("submarine", submarineDamage);
            damagesPerTurn.put("destroyer", destroyerDamage);
            damagesPerTurn.put("patrolboat", patrolboatDamage);
            hitsMapPerTurn.put("turn", salvo.getTurn());
            hitsMapPerTurn.put("hitLocations", hitCellsList);
            hitsMapPerTurn.put("damages", damagesPerTurn);
            hitsMapPerTurn.put("missed", missedShots);
            dto.add(hitsMapPerTurn);
        }
        return dto;
    }

    //Comprueba si el gameplayer es el correcto par ese juego
    private boolean wrongGamePlayer(GamePlayer gamePlayer, Player player) {
        return gamePlayer.getPlayer().getId() != player.getId();
    }

    private Optional<GamePlayer> getOpp(Game g, GamePlayer gp) {
        return g.getGamePlayers()
                .stream()
                .filter(b -> b.getId() != gp.getId())
                .findFirst();
    }
    //C5 T5: Target: Hits & Sink

    //--------------------GAME VIEW/ID-----------------------------------------------------------|
    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long id, Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();
        Player player = gamePlayer.getPlayer();
        //System.out.println(player);
        Player authenticationPlayer = getAuthentication(authentication);
        //System.out.println(authenticationPlayer);
        if(authenticationPlayer.getId() == player.getId()){
            return new ResponseEntity<>(makeGameDTO(gamePlayerRepository.findById(id).get().getGame()), HttpStatus.ACCEPTED);}
        else{
            return new ResponseEntity<>(Map.of("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
        }
    }
}
