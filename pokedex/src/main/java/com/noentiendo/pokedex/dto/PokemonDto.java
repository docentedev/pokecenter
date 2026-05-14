package com.noentiendo.pokedex.dto;

import java.util.UUID;

public record PokemonDto(
        UUID id,
        String name,
        String type,
        int pokedexNumber
        ) {

}
