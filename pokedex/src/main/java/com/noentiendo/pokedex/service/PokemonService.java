package com.noentiendo.pokedex.service;

import java.util.List;
import java.util.UUID;

import com.noentiendo.pokedex.dto.PokemonDto;

public interface PokemonService {

    List<PokemonDto> searchPokemons(String name, String type, Integer pokedexNumber);

    PokemonDto getPokemonById(UUID id);

    PokemonDto createPokemon(PokemonDto pokemonDto);

    PokemonDto updatePokemon(UUID id, PokemonDto pokemonDto);

    void deletePokemon(UUID id);

    List<PokemonDto> getAllPokemons();
}
