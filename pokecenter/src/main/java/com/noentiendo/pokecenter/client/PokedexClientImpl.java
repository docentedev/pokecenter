package com.noentiendo.pokecenter.client;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.noentiendo.pokecenter.dto.PokemonDto;

// apiPokedex.url=http://localhost:3333/api
@Component
public class PokedexClientImpl implements PokedexClient {

    private final RestClient restClient;

    public PokedexClientImpl(@Value("${apiPokedex.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<PokemonDto> getAllPokemons() {
        ResponseEntity<List<PokemonDto>> response = restClient.get()
                .uri("/pokemons")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<PokemonDto>>() {
                });

        List<PokemonDto> body = response.getBody();
        HttpStatusCode status = response.getStatusCode();
        if (status.is2xxSuccessful() && body != null) {
            return body;
        } else {
            throw new RuntimeException("Failed to fetch pokemons: " + status);
        }
    }

    @Override
    public List<PokemonDto> searchPokemons(String name, String type, Integer pokedexNumber) {
        ResponseEntity<List<PokemonDto>> response = restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/pokemons");
                    if (name != null && !name.isBlank()) {
                        builder = builder.queryParam("name", name);
                    }
                    if (type != null && !type.isBlank()) {
                        builder = builder.queryParam("type", type);
                    }
                    if (pokedexNumber != null) {
                        builder = builder.queryParam("pokedexNumber", pokedexNumber);
                    }
                    return builder.build();
                })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<PokemonDto>>() {
                });

        List<PokemonDto> body = response.getBody();
        HttpStatusCode status = response.getStatusCode();
        if (status.is2xxSuccessful() && body != null) {
            return body;
        }
        throw new RuntimeException("Failed to search pokemons: " + status);
    }

    @Override
    public PokemonDto getPokemonById(UUID id) {
        PokemonDto pokemon = restClient.get()
                .uri("/pokemons/{id}", id)
                .retrieve()
                .body(PokemonDto.class);
        return pokemon;
    }

    @Override
    public PokemonDto createPokemon(PokemonDto pokemonDto) {
        ResponseEntity<PokemonDto> response = restClient.post()
                .uri("/pokemons")
                .body(pokemonDto)
                .retrieve()
                .toEntity(PokemonDto.class);

        PokemonDto body = response.getBody();
        HttpStatusCode status = response.getStatusCode();
        if ((status.is2xxSuccessful() || status.value() == HttpStatus.CREATED.value()) && body != null) {
            return body;
        }
        throw new RuntimeException("Failed to create pokemon: " + status);
    }

    @Override
    public PokemonDto updatePokemon(UUID id, PokemonDto pokemonDto) {
        ResponseEntity<PokemonDto> response = restClient.put()
                .uri("/pokemons/{id}", id)
                .body(pokemonDto)
                .retrieve()
                .toEntity(PokemonDto.class);

        PokemonDto body = response.getBody();
        HttpStatusCode status = response.getStatusCode();
        if (status.is2xxSuccessful() && body != null) {
            return body;
        }
        throw new RuntimeException("Failed to update pokemon: " + status);
    }

    @Override
    public void deletePokemon(UUID id) {
        ResponseEntity<Void> response = restClient.delete()
                .uri("/pokemons/{id}", id)
                .retrieve()
                .toBodilessEntity();

        HttpStatusCode status = response.getStatusCode();
        if (!status.is2xxSuccessful()) {
            throw new RuntimeException("Failed to delete pokemon: " + status);
        }
    }

}
