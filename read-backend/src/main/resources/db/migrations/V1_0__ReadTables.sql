CREATE TABLE IF NOT EXISTS public.money (
  fortune_id VARCHAR(255) NOT NULL,
  currency VARCHAR(255) NOT NULL,
  amount NUMERIC NOT NULL,
  PRIMARY KEY(fortune_id, currency)
);
