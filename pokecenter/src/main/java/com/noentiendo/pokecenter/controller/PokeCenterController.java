package com.noentiendo.pokecenter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noentiendo.pokecenter.client.PokedexClient;
import com.noentiendo.pokecenter.dto.ApiResponse;
import com.noentiendo.pokecenter.dto.PokemonDto;

@RestController
@RequestMapping("/api/pokecenter")
public class PokeCenterController {

    final PokedexClient pokedexClient;

    public PokeCenterController(PokedexClient pokedexClient) {
        this.pokedexClient = pokedexClient;
    }

    @GetMapping("/pokemons")
    public ResponseEntity<ApiResponse<List<PokemonDto>>> getAllPokemons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer pokedexNumber) {
        
        List<PokemonDto> pokemons;
        boolean hasFilters = (name != null && !name.isBlank()) || (type != null && !type.isBlank())
                || pokedexNumber != null;
        
        if (hasFilters) {
            pokemons = pokedexClient.searchPokemons(name, type, pokedexNumber);
        } else {
            pokemons = pokedexClient.getAllPokemons();
        }
        
        ApiResponse<List<PokemonDto>> response = new ApiResponse<>(true, pokemons);
        return ResponseEntity.ok(response);
    }
}
