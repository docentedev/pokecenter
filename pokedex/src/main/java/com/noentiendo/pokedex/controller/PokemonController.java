package com.noentiendo.pokedex.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noentiendo.pokedex.dto.PokemonDto;
import com.noentiendo.pokedex.service.PokemonService;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {

    final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping
    public ResponseEntity<List<PokemonDto>> getAllPokemons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer pokedexNumber) {
        boolean hasFilters = (name != null && !name.isBlank()) || (type != null && !type.isBlank())
                || pokedexNumber != null;
        List<PokemonDto> pokemons = hasFilters ? pokemonService.searchPokemons(name, type, pokedexNumber)
                : pokemonService.getAllPokemons();
        return ResponseEntity.ok(pokemons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PokemonDto> getPokemonById(@PathVariable UUID id) {
        PokemonDto pokemonDto = pokemonService.getPokemonById(id);
        return ResponseEntity.ok(pokemonDto);
    }

    @PostMapping
    public ResponseEntity<PokemonDto> createPokemon(@RequestBody PokemonDto pokemonDto) {
        PokemonDto createdPokemon = pokemonService.createPokemon(pokemonDto);
        return ResponseEntity.ok(createdPokemon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PokemonDto> updatePokemon(@PathVariable UUID id, @RequestBody PokemonDto pokemonDto) {
        PokemonDto updatedPokemon = pokemonService.updatePokemon(id, pokemonDto);
        return ResponseEntity.ok(updatedPokemon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePokemon(@PathVariable UUID id) {
        pokemonService.deletePokemon(id);
        return ResponseEntity.noContent().build();
    }
}
