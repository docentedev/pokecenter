-- autogenerate UUIsD for id column in pokemon table
ALTER TABLE pokemon
ALTER COLUMN id SET DEFAULT gen_random_uuid();