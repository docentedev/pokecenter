-- Create pokedex_number into pokemon table
ALTER TABLE pokemon
ADD COLUMN pokedex_number INT UNIQUE;