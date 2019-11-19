package com.codeoftheweb.salvo;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.SqlResultSetMapping;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GamePlayerRepository gamePlayerRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    //Autenticar al usuario
    @RequestMapping("/games")
    public Map<String,Object> getGameAll(Authentication authentication){
        Map<String,  Object>  dto = new LinkedHashMap<>();
        if(isGuest(authentication)){
            dto.put("player", "Guest");
        }else{
            Player player  = playerRepository.findByUserName(authentication.getName()).get();
            dto.put("player", player.makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));
        return dto;
    }

    @RequestMapping("/game_view/{id}")
    public Map<String, Object> getGameViewByGamePlayerID (@PathVariable Long id) {
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();

        Map<String, Object> dto = new LinkedHashMap<>();
        Map<String, Object> hits = new LinkedHashMap<>();
        hits.put("self", new ArrayList<>());
        hits.put("opponent", new ArrayList<>());
        dto.put("id", gamePlayer.getGame().getId());
        dto.put("creationDate", gamePlayer.getGame().getCreationDate());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers()
                .stream()
                .map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO())
                .collect(Collectors.toList()));
        dto.put("ships", gamePlayer.getShips()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList()));
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes()
                        .stream()
                        .map(salvo -> salvo.makeSalvoDTO()))
                .collect(Collectors.toList()));
        dto.put("hits", hits);
        dto.put("gameState", getState(gamePlayer));

        return dto;
    }


    // Scores
    @RequestMapping("/leaderBoard")
    public List<Map<String, Object>> makeLeaderBoard(){
                        return playerRepository
                                .findAll()
                                .stream()
                                .map(player -> playerLeaderBoardDTO(player))
                                .collect(Collectors.toList());
    }

    private Map<String, Object> playerLeaderBoardDTO(Player player){
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", player.getId());
            dto.put("userName", player.getUserName());
            dto.put("score", getScoreList(player));
            return dto;
    }

    private Map<String, Object> getScoreList (Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("total", player.getTotalScore());
        dto.put("won", player.getWins(player.getScores()));
        dto.put("tied", player.getDraws(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        return dto;
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    // registrar nuevo jugador
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Object> register( // o tambien puede ser addPlayer
        @RequestParam String username, @RequestParam String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }
        if(playerRepository.findByUserName(username).orElse(null) != null){
            return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
        }
        playerRepository.save(new Player(username, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    // agregar nuevo juego
    @RequestMapping(path = "/game/{gameId}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> joinGame(@PathVariable Long gameId, Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(GameController.makeMap("error:", "No Autorizado usuario no logueado"), HttpStatus.UNAUTHORIZED);
        }
        Player player = playerRepository.findByUserName(authentication.getName()).orElse(null);
        Game game = gameRepository.getOne(gameId);

        if (game == null) {
            return new ResponseEntity<>(GameController.makeMap("error:", "juego no encontrado"), HttpStatus.FORBIDDEN);
        }
        if (player == null) {
            return new ResponseEntity<>(GameController.makeMap("error:", "jugador no encontrado"), HttpStatus.FORBIDDEN);
        }
        int gamePlayersCount = game.getGamePlayers().size();

        if (gamePlayersCount == 1) {
            if (game.getOneGamePlayer().getPlayer().getUserName() == player.getUserName()) {
                return new ResponseEntity<>(GameController.makeMap("error:", "El jugador ya se encuentra en el juego"), HttpStatus.UNAUTHORIZED);
            } else {
                GamePlayer gamePlayer = gamePlayerRepository.save(new GamePlayer(game, player));
                return new ResponseEntity<>(GameController.makeMap("gpId", gamePlayer.getId()), HttpStatus.CREATED);
            }
        } else {
            return new ResponseEntity<>(GameController.makeMap("error", "Juego completo"), HttpStatus.OK);
        }
    }

    public String   getState(GamePlayer gamePlayer){
        if(!gamePlayer.getShips().isEmpty()){
            return "WAIT";
        }
        return "PLACESHIPS";
    }


}

