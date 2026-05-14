package com.noentiendo.pokecenter.client;

import java.util.List;
import java.util.UUID;

import com.noentiendo.pokecenter.dto.PokemonDto;

public interface PokedexClient {

    List<PokemonDto> getAllPokemons();

    List<PokemonDto> searchPokemons(String name, String type, Integer pokedexNumber);

    PokemonDto getPokemonById(UUID id);

    PokemonDto createPokemon(PokemonDto pokemonDto);

    PokemonDto updatePokemon(UUID id, PokemonDto pokemonDto);

    void deletePokemon(UUID id);
}
