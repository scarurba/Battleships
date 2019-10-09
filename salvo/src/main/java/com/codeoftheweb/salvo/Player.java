package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Entity
public class Player{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String userName;
    private String password;

    public float wins;
    public float losses;
    public float draws;
    public float totalScore;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<Score> Scores = new LinkedHashSet<>();

    public Player() {
    }

    public Player(String userName, String password) {

        this.userName = userName;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<Score> getScores() {
        return Scores;
    }

    public void setScores(Set<Score> scores) {
        Scores = scores;
    }

    // Getter Score
    public float getWins(Set<Score> puntajes) {
        return puntajes.stream().filter(puntaje -> puntaje.getScore() == 1).count();
    }

    public float getLosses(Set<Score> puntajes) {
        return puntajes.stream().filter(puntaje -> puntaje.getScore() == 0).count();
    }

    public float getDraws(Set<Score> puntajes) {
        return puntajes.stream().filter(puntaje -> puntaje.getScore() == (float) 0.5).count();
    }

    public float getTotalScore() {
        float victorias = getWins(this.getScores())*1;
        float empates = getDraws(this.getScores())*(float) 0.5;
        float derrotas = getLosses(this.getScores())*0;

        return victorias + empates + derrotas;
    }

    // getter y setter Password
        public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // PlayerDTO
    public Map<String, Object> makePlayerDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id",this.getId());
        dto.put("email",this.getUserName());
        return dto;
    }

}


