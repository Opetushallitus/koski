-- Table Definition ----------------------------------------------

CREATE TABLE opiskeluoikeushistoria_virheet (
    id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aikaleima time with time zone NOT NULL DEFAULT now(),
    opiskeluoikeus jsonb NOT NULL,
    historia jsonb NOT NULL,
    diff jsonb NOT NULL
);
