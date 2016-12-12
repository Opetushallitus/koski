create extension unaccent;
-- text search configuration that's basec on Finnish, but ignores accents like á
ALTER TEXT SEARCH DICTIONARY unaccent (RULES='unaccent');
CREATE TEXT SEARCH CONFIGURATION koski ( COPY = finnish );
ALTER TEXT SEARCH CONFIGURATION koski
ALTER MAPPING FOR hword, hword_part, word
WITH unaccent, finnish_stem;