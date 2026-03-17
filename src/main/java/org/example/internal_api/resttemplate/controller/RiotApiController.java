package org.example.internal_api.resttemplate.controller;

import lombok.RequiredArgsConstructor;
import org.example.internal_api.resttemplate.dto.RiotAccountResponse;
import org.example.internal_api.resttemplate.service.RiotApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RiotApiController {

    private final RiotApiService riotApiService;

    @GetMapping("api/riot/account/{gameName}/{tagLine}")
    public RiotAccountResponse getRiotAccount(@PathVariable String gameName, @PathVariable String tagLine) {
        return riotApiService.getAccountInfo(gameName, tagLine);
    }

    @GetMapping("api/matches/by-puuid/{puuid}")
    public List<String> getMatchIds(@PathVariable String puuid) {
        return riotApiService.getMatchIds(puuid);
    }

    @GetMapping("api/matches/{matchId}")
    public Object getMatchDetail(@PathVariable String matchId) {
        return riotApiService.getMatchDetail(matchId);
    }
}
