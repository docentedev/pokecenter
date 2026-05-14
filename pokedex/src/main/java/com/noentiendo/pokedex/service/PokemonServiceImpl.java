package com.noentiendo.pokedex.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.noentiendo.pokedex.dto.PokemonDto;
import com.noentiendo.pokedex.model.Pokemon;
import com.noentiendo.pokedex.repository.PokemonRepository;

@Service
public class PokemonServiceImpl implements PokemonService {

    private final PokemonRepository pokemonRepository;

    public PokemonServiceImpl(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    @Override
    public PokemonDto getPokemonById(UUID id) {
        Pokemon pokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pokemon not found with id: " + id));
        return convertToDto(pokemon);
    }

        @Override
        public List<PokemonDto> searchPokemons(String name, String type, Integer pokedexNumber) {
        return pokemonRepository.findAll().stream()
            .filter(pokemon -> name == null || name.isBlank()
                || pokemon.getName().toLowerCase().contains(name.toLowerCase()))
            .filter(pokemon -> type == null || type.isBlank()
                || pokemon.getType().equalsIgnoreCase(type))
            .filter(pokemon -> pokedexNumber == null || pokemon.getPokedexNumber() == pokedexNumber)
            .map(this::convertToDto)
            .toList();
        }

    @Override
    public PokemonDto createPokemon(PokemonDto pokemonDto) {
        Pokemon pokemon = convertToEntity(pokemonDto);
        Pokemon savedPokemon = pokemonRepository.save(pokemon);
        return convertToDto(savedPokemon);
    }

    @Override
    public PokemonDto updatePokemon(UUID id, PokemonDto pokemonDto) {
        Pokemon existingPokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pokemon not found with id: " + id));
        existingPokemon.setName(pokemonDto.name());
        existingPokemon.setType(pokemonDto.type());
        existingPokemon.setPokedexNumber(pokemonDto.pokedexNumber());
        Pokemon updatedPokemon = pokemonRepository.save(existingPokemon);
        return convertToDto(updatedPokemon);
    }

    @Override
    public void deletePokemon(UUID id) {
        Pokemon existingPokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pokemon not found with id: " + id));
        pokemonRepository.delete(existingPokemon);
    }

    @Override
    public List<PokemonDto> getAllPokemons() {
        System.out.println("Fetching all pokemons from the database...");
        List<Pokemon> pokemons = pokemonRepository.findAll();
        return pokemons.stream().map(this::convertToDto)
                .toList();
    }

    // dto to entity and entity to dto conversion methods can be added here
    private PokemonDto convertToDto(Pokemon pokemon) {
        return new PokemonDto(pokemon.getId(), pokemon.getName(), pokemon.getType(), pokemon.getPokedexNumber());
    }

    private Pokemon convertToEntity(PokemonDto pokemonDto) {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(pokemonDto.id());
        pokemon.setName(pokemonDto.name());
        pokemon.setType(pokemonDto.type());
        pokemon.setPokedexNumber(pokemonDto.pokedexNumber());
        return pokemon;
    }
}
