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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    //
    @RequestMapping(path = "/player", method = RequestMethod.POST)
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


}

