package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private long turn;

    @ElementCollection
    @Column(name = "salvoLocations")
    private Set<String> salvoLocations;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    public Salvo() {}

    public Salvo(long turn, Set<String> salvoLocations, GamePlayer gamePlayer) {
        this.turn = turn;
        this.salvoLocations = salvoLocations;
        this.gamePlayer = gamePlayer;
    }

    public Map<String, Object> makeSalvoDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.getTurn());
        dto.put("player", this.getGamePlayer().getPlayer().getId());
        dto.put("locations", this.getSalvoLocations());
        return dto;
    }

    // Getter
    public long getId() {
        return id;
    }

    public long getTurn() {
        return turn;
    }

    public Set<String> getSalvoLocations() {
        return salvoLocations;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }


    //Setter

    public void setTurn(long turn) {
        this.turn = turn;
    }

    public void setSalvoLocations(Set<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
}
