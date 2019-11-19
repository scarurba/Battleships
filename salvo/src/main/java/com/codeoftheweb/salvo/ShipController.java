package com.codeoftheweb.salvo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ShipController {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GamePlayerRepository gamePlayerRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    ShipRepository shipRepository;

    @RequestMapping("/games/players/{gpid}/ships")
    public ResponseEntity<Map> addShip(@PathVariable long gpid, @RequestBody Set<Ship> ships, Authentication authentication){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error", "No esta autorizado"), HttpStatus.UNAUTHORIZED);
        }

        Player player = playerRepository.findByUserName(authentication.getName()).orElse(null);
        GamePlayer gamePlayer = gamePlayerRepository.getOne(gpid);

        if(player == null){
            return new ResponseEntity<>(makeMap("error", "No esta autorizado"), HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer == null){
            return new ResponseEntity<>(makeMap("error", "No esta autorizado"), HttpStatus.UNAUTHORIZED);
        }

        if(gamePlayer.getPlayer().getId() != player.getId()){
            return new ResponseEntity<>(makeMap("error", "Los players no coinciden"), HttpStatus.FORBIDDEN);
        }

        if(!gamePlayer.getShips().isEmpty()){
            return new ResponseEntity<>(makeMap("error", "No esta autorizado ya tengo Ships"), HttpStatus.UNAUTHORIZED);
        }

        ships.forEach(ship -> {
            ship.setGamePlayer(gamePlayer);
            shipRepository.save(ship);
        });


        return new ResponseEntity<>(makeMap("OK", "Ship created"), HttpStatus.CREATED);

    }

    private boolean isGuest(Authentication authentication){
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }


    public static Map<String, Object> makeMap(String key, Object value){
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }



}
