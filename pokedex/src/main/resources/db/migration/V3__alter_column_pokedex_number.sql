-- alter column pokedex_number to allow null values
ALTER TABLE pokemon
ALTER COLUMN pokedex_number DROP NOT NULL;