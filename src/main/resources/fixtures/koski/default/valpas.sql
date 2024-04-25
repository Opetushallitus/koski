--
-- PostgreSQL database dump
--

-- Dumped from database version 15.2 (Debian 15.2-1.pgdg110+1)
-- Dumped by pg_dump version 15.5 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ilmoitus; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public.ilmoitus (
    uuid uuid NOT NULL,
    luotu timestamp without time zone NOT NULL,
    oppija_oid text NOT NULL,
    kunta_oid text NOT NULL,
    "tekijä_organisaatio_oid" text NOT NULL,
    "tekijä_oid" text NOT NULL
);


ALTER TABLE public.ilmoitus OWNER TO oph;

--
-- Name: ilmoitus_lisätiedot; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public."ilmoitus_lisätiedot" (
    ilmoitus_uuid uuid NOT NULL,
    data jsonb NOT NULL
);


ALTER TABLE public."ilmoitus_lisätiedot" OWNER TO oph;

--
-- Name: ilmoitus_opiskeluoikeus_konteksti; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public.ilmoitus_opiskeluoikeus_konteksti (
    ilmoitus_uuid uuid NOT NULL,
    opiskeluoikeus_oid text NOT NULL
);


ALTER TABLE public.ilmoitus_opiskeluoikeus_konteksti OWNER TO oph;

--
-- Name: opiskeluoikeus_lisätiedot; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public."opiskeluoikeus_lisätiedot" (
    oppija_oid text NOT NULL,
    opiskeluoikeus_oid text NOT NULL,
    oppilaitos_oid text NOT NULL,
    muu_haku boolean NOT NULL
);


ALTER TABLE public."opiskeluoikeus_lisätiedot" OWNER TO oph;

--
-- Name: oppivelvollisuuden_keskeytys; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public.oppivelvollisuuden_keskeytys (
    uuid uuid NOT NULL,
    oppija_oid character varying NOT NULL,
    alku date NOT NULL,
    loppu date,
    luotu timestamp without time zone NOT NULL,
    "tekijä_oid" text NOT NULL,
    "tekijä_organisaatio_oid" text NOT NULL,
    peruttu boolean NOT NULL
);


ALTER TABLE public.oppivelvollisuuden_keskeytys OWNER TO oph;

--
-- Name: oppivelvollisuuden_keskeytyshistoria; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public.oppivelvollisuuden_keskeytyshistoria (
    id integer NOT NULL,
    ov_keskeytys_uuid uuid NOT NULL,
    muutos_tehty timestamp without time zone NOT NULL,
    muutoksen_tekija text NOT NULL,
    oppija_oid character varying NOT NULL,
    alku date NOT NULL,
    loppu date,
    luotu timestamp without time zone NOT NULL,
    "tekijä_oid" text NOT NULL,
    "tekijä_organisaatio_oid" text NOT NULL,
    peruttu boolean NOT NULL
);


ALTER TABLE public.oppivelvollisuuden_keskeytyshistoria OWNER TO oph;

--
-- Name: oppivelvollisuuden_keskeytyshistoria_id_seq; Type: SEQUENCE; Schema: public; Owner: oph
--

CREATE SEQUENCE public.oppivelvollisuuden_keskeytyshistoria_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.oppivelvollisuuden_keskeytyshistoria_id_seq OWNER TO oph;

--
-- Name: oppivelvollisuuden_keskeytyshistoria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: oph
--

ALTER SEQUENCE public.oppivelvollisuuden_keskeytyshistoria_id_seq OWNED BY public.oppivelvollisuuden_keskeytyshistoria.id;


--
-- Name: oppivelvollisuudesta_vapautetut; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public.oppivelvollisuudesta_vapautetut (
    oppija_oid text NOT NULL,
    virkailija_oid text NOT NULL,
    kunta_koodiarvo text NOT NULL,
    vapautettu date NOT NULL,
    aikaleima timestamp without time zone DEFAULT now() NOT NULL,
    mitatoity timestamp without time zone DEFAULT '9999-01-01 00:00:00'::timestamp without time zone NOT NULL
);


ALTER TABLE public.oppivelvollisuudesta_vapautetut OWNER TO oph;

--
-- Name: schema_version; Type: TABLE; Schema: public; Owner: oph
--

CREATE TABLE public.schema_version (
    version_rank integer NOT NULL,
    installed_rank integer NOT NULL,
    version character varying(50) NOT NULL,
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.schema_version OWNER TO oph;

--
-- Name: oppivelvollisuuden_keskeytyshistoria id; Type: DEFAULT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.oppivelvollisuuden_keskeytyshistoria ALTER COLUMN id SET DEFAULT nextval('public.oppivelvollisuuden_keskeytyshistoria_id_seq'::regclass);


--
-- Data for Name: ilmoitus; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public.ilmoitus (uuid, luotu, oppija_oid, kunta_oid, "tekijä_organisaatio_oid", "tekijä_oid") FROM stdin;
a61efa6b-e920-4358-9e2b-0a3e86d826d3	2024-01-01 14:55:44.842172	1.2.246.562.24.00000000034	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
66218bf5-97cd-4198-aaf3-75f6f3e75c4e	2024-01-01 14:55:44.849743	1.2.246.562.24.00000000035	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
1405ee95-31ce-4dd0-ba2d-8c65debb4870	2024-01-01 14:55:44.856004	1.2.246.562.24.00000000036	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
7e839607-8bb5-45c9-8fa4-746a947bda00	2024-01-01 14:55:44.864153	1.2.246.562.24.00000000037	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
46d21287-b970-4064-8d1e-90ca471e8fd8	2024-01-01 14:55:44.8715	1.2.246.562.24.00000000039	1.2.246.562.10.69417312936	1.2.246.562.10.26197302388	1.2.246.562.24.12312312302
3d48dbb6-2ba8-4e2b-abe4-6bfbf2369cfd	2024-01-01 14:55:44.877147	1.2.246.562.24.00000000050	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
f95ed503-307b-4c2e-9595-a555266be2af	2021-06-15 14:55:44.883777	1.2.246.562.24.00000000040	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
b8187810-7b23-475c-a41f-f5e327dc0e83	2021-06-15 14:55:44.889519	1.2.246.562.24.00000000041	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
68247027-6e60-47bb-9a49-8721f5060e85	2021-09-15 14:55:44.895099	1.2.246.562.24.00000000041	1.2.246.562.10.346830761110	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
6445a13c-c58b-470e-97aa-ac8947438345	2021-09-20 14:55:44.900605	1.2.246.562.24.00000000041	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
6a57e527-3fa6-45d6-8410-0bf4a1ee59b0	2021-11-30 14:55:44.906442	1.2.246.562.24.00000000041	1.2.246.562.10.346830761110	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
283c8092-1bce-44ac-9955-25fd99270090	2024-01-01 14:55:44.911776	1.2.246.562.24.00000000051	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
ea1f27fb-a320-49b7-be18-a75095b8ca12	2021-05-20 14:55:44.917967	1.2.246.562.24.00000000134	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
bbe3f2f6-b1db-41fc-9c2b-12af992ecbdc	2021-05-19 14:55:44.924269	1.2.246.562.24.00000000134	1.2.246.562.10.346830761110	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
4954cab3-4084-4afd-8707-f0510b1fedf4	2024-01-01 14:55:44.929969	1.2.246.562.24.00000000024	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
2ddb5c66-fc6c-4d36-aba2-4252d72adf4b	2021-08-15 14:55:44.936763	1.2.246.562.24.00000000148	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
162d0c22-78b0-49d4-9861-5d656bd30161	2021-04-08 14:55:44.942837	1.2.246.562.24.00000000149	1.2.246.562.10.346830761110	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
6c5feba5-a81a-41df-a4f8-e25835526ffd	2021-06-27 14:55:44.949279	1.2.246.562.24.00000000166	1.2.246.562.10.69417312936	1.2.246.562.10.14613773812	1.2.246.562.24.12312312302
\.


--
-- Data for Name: ilmoitus_lisätiedot; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public."ilmoitus_lisätiedot" (ilmoitus_uuid, data) FROM stdin;
66218bf5-97cd-4198-aaf3-75f6f3e75c4e	{"kunta": {"oid": "1.2.246.562.10.69417312936"}, "hakenutMuualle": false, "oppijaYhteystiedot": {}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812"}, "tekijäYhteystiedot": {"etunimet": "Valpas", "sukunimi": "Käyttäjä"}}
a61efa6b-e920-4358-9e2b-0a3e86d826d3	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
1405ee95-31ce-4dd0-ba2d-8c65debb4870	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
7e839607-8bb5-45c9-8fa4-746a947bda00	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
46d21287-b970-4064-8d1e-90ca471e8fd8	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.26197302388", "nimi": {"fi": "Aapajoen koulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
3d48dbb6-2ba8-4e2b-abe4-6bfbf2369cfd	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
f95ed503-307b-4c2e-9595-a555266be2af	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
b8187810-7b23-475c-a41f-f5e327dc0e83	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
68247027-6e60-47bb-9a49-8721f5060e85	{"kunta": {"oid": "1.2.246.562.10.346830761110", "nimi": {"fi": "Helsingin kaupunki"}, "kotipaikka": {"nimi": {"fi": "Helsinki"}, "koodiarvo": "091", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
6445a13c-c58b-470e-97aa-ac8947438345	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
6a57e527-3fa6-45d6-8410-0bf4a1ee59b0	{"kunta": {"oid": "1.2.246.562.10.346830761110", "nimi": {"fi": "Helsingin kaupunki"}, "kotipaikka": {"nimi": {"fi": "Helsinki"}, "koodiarvo": "091", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
ea1f27fb-a320-49b7-be18-a75095b8ca12	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
bbe3f2f6-b1db-41fc-9c2b-12af992ecbdc	{"kunta": {"oid": "1.2.246.562.10.346830761110", "nimi": {"fi": "Helsingin kaupunki"}, "kotipaikka": {"nimi": {"fi": "Helsinki"}, "koodiarvo": "091", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
4954cab3-4084-4afd-8707-f0510b1fedf4	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
2ddb5c66-fc6c-4d36-aba2-4252d72adf4b	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
162d0c22-78b0-49d4-9861-5d656bd30161	{"kunta": {"oid": "1.2.246.562.10.346830761110", "nimi": {"fi": "Helsingin kaupunki"}, "kotipaikka": {"nimi": {"fi": "Helsinki"}, "koodiarvo": "091", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
6c5feba5-a81a-41df-a4f8-e25835526ffd	{"kunta": {"oid": "1.2.246.562.10.69417312936", "nimi": {"fi": "Pyhtään kunta"}, "kotipaikka": {"nimi": {"fi": "Pyhtää"}, "koodiarvo": "624", "koodistoUri": "kunta"}}, "hakenutMuualle": false, "yhteydenottokieli": "FI", "oppijaYhteystiedot": {"maa": {"koodiarvo": "246", "koodistoUri": "maatjavaltiot2"}, "puhelin": "0401234567", "lähiosoite": "Esimerkkikatu 123", "postinumero": "99999", "sähköposti": "Veijo.Valpas@gmail.com", "postitoimipaikka": "Pyhtää"}, "tekijäOrganisaatio": {"oid": "1.2.246.562.10.14613773812", "nimi": {"fi": "Jyväskylän normaalikoulu"}}, "tekijäYhteystiedot": {"puhelin": "040 123 4567", "etunimet": "valpas-jkl-normaali Mestari", "sukunimi": "käyttäjä", "kutsumanimi": "valpas-jkl-normaali", "sähköposti": "valpas-jkl-normaali@gmail.com"}}
\.


--
-- Data for Name: ilmoitus_opiskeluoikeus_konteksti; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public.ilmoitus_opiskeluoikeus_konteksti (ilmoitus_uuid, opiskeluoikeus_oid) FROM stdin;
\.


--
-- Data for Name: opiskeluoikeus_lisätiedot; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public."opiskeluoikeus_lisätiedot" (oppija_oid, opiskeluoikeus_oid, oppilaitos_oid, muu_haku) FROM stdin;
\.


--
-- Data for Name: oppivelvollisuuden_keskeytys; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public.oppivelvollisuuden_keskeytys (uuid, oppija_oid, alku, loppu, luotu, "tekijä_oid", "tekijä_organisaatio_oid", peruttu) FROM stdin;
005d6802-6007-4ab2-9356-898d81476253	1.2.246.562.24.00000000056	2021-09-01	2021-09-30	2021-02-28 08:00:00	1.2.246.562.24.12312312302	1.2.246.562.10.346830761110	f
494a7507-6d38-494f-87dd-0806484a2070	1.2.246.562.24.00000000056	2020-01-01	2020-01-30	2021-01-01 10:15:00	1.2.246.562.24.12312312302	1.2.246.562.10.346830761110	f
4e944a5f-c199-4b2a-9975-60f030dcc5cf	1.2.246.562.24.00000000057	2021-01-01	\N	2021-01-01 12:30:00	1.2.246.562.24.12312312302	1.2.246.562.10.346830761110	f
7f63a642-45e3-40cd-ab87-3a9f90d4dd7c	1.2.246.562.24.00000000065	2021-08-15	2021-10-30	2021-01-01 12:30:00	1.2.246.562.24.12312312301	1.2.246.562.10.346830761110	f
d86d4e8a-ce23-47f7-a06c-ef9121fb1237	1.2.246.562.24.00000000071	2021-09-30	\N	2021-01-01 12:30:00	1.2.246.562.24.12312312301	1.2.246.562.10.346830761110	f
58f1b7c3-2c3c-4fd4-b3d3-12070f8876fc	1.2.246.562.24.00000000134	2021-08-16	\N	2021-01-01 12:30:00	1.2.246.562.24.12312312301	1.2.246.562.10.346830761110	f
f2b56303-73aa-462c-90fd-a64fa8a916d1	1.2.246.562.24.00000000148	2021-09-01	\N	2021-09-05 12:30:00	1.2.246.562.24.12312312301	1.2.246.562.10.346830761110	f
18b0b94c-7e94-4cb5-bab9-de2565391561	1.2.246.562.24.00000000149	2019-01-01	2019-12-01	2019-01-02 12:30:00	1.2.246.562.24.12312312301	1.2.246.562.10.346830761110	f
\.


--
-- Data for Name: oppivelvollisuuden_keskeytyshistoria; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public.oppivelvollisuuden_keskeytyshistoria (id, ov_keskeytys_uuid, muutos_tehty, muutoksen_tekija, oppija_oid, alku, loppu, luotu, "tekijä_oid", "tekijä_organisaatio_oid", peruttu) FROM stdin;
\.


--
-- Data for Name: oppivelvollisuudesta_vapautetut; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public.oppivelvollisuudesta_vapautetut (oppija_oid, virkailija_oid, kunta_koodiarvo, vapautettu, aikaleima, mitatoity) FROM stdin;
1.2.246.562.24.00000000161	1.2.246.562.24.12312312301	091	2000-08-01	2024-04-25 14:55:43.130096	9999-01-01 00:00:00
\.


--
-- Data for Name: schema_version; Type: TABLE DATA; Schema: public; Owner: oph
--

COPY public.schema_version (version_rank, installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	1	init	SQL	V1__init.sql	922255268	oph	2024-04-25 12:50:52.055893	128	t
2	2	2	opiskeluoikeus lisätiedot	SQL	V2__opiskeluoikeus_lisätiedot.sql	845100289	oph	2024-04-25 12:50:52.32957	18	t
3	3	3	muuta timestamp tyypitys	SQL	V3__muuta_timestamp_tyypitys.sql	1324605831	oph	2024-04-25 12:50:52.389118	23	t
4	4	4	oppivelvollisuuden keskeytys	SQL	V4__oppivelvollisuuden_keskeytys.sql	-1939737018	oph	2024-04-25 12:50:52.451925	13	t
5	5	5	ilmoitus opiskeluoikeus konteksti	SQL	V5__ilmoitus_opiskeluoikeus_konteksti.sql	-829395977	oph	2024-04-25 12:50:52.505899	22	t
6	6	6	oppivelvollisuuden keskeytyshistoria	SQL	V6__oppivelvollisuuden_keskeytyshistoria.sql	180730583	oph	2024-04-25 12:50:52.565698	23	t
7	7	7	oppivelvollisuudesta vapautetut taulu	SQL	V7__oppivelvollisuudesta_vapautetut_taulu.sql	-1360050194	oph	2024-04-25 12:50:52.626194	17	t
\.


--
-- Name: oppivelvollisuuden_keskeytyshistoria_id_seq; Type: SEQUENCE SET; Schema: public; Owner: oph
--

SELECT pg_catalog.setval('public.oppivelvollisuuden_keskeytyshistoria_id_seq', 1, false);


--
-- Name: ilmoitus_lisätiedot ilmoitus_lisätiedot_pkey; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public."ilmoitus_lisätiedot"
    ADD CONSTRAINT "ilmoitus_lisätiedot_pkey" PRIMARY KEY (ilmoitus_uuid);


--
-- Name: ilmoitus_opiskeluoikeus_konteksti ilmoitus_opiskeluoikeus_konteksti_pkey; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.ilmoitus_opiskeluoikeus_konteksti
    ADD CONSTRAINT ilmoitus_opiskeluoikeus_konteksti_pkey PRIMARY KEY (ilmoitus_uuid, opiskeluoikeus_oid);


--
-- Name: ilmoitus ilmoitus_pkey; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.ilmoitus
    ADD CONSTRAINT ilmoitus_pkey PRIMARY KEY (uuid);


--
-- Name: opiskeluoikeus_lisätiedot opiskeluoikeus_lisätiedot_pkey; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public."opiskeluoikeus_lisätiedot"
    ADD CONSTRAINT "opiskeluoikeus_lisätiedot_pkey" PRIMARY KEY (oppija_oid, opiskeluoikeus_oid, oppilaitos_oid);


--
-- Name: oppivelvollisuuden_keskeytys oppivelvollisuuden_keskeytys_pkey; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.oppivelvollisuuden_keskeytys
    ADD CONSTRAINT oppivelvollisuuden_keskeytys_pkey PRIMARY KEY (uuid);


--
-- Name: oppivelvollisuuden_keskeytyshistoria oppivelvollisuuden_keskeytyshistoria_pkey; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.oppivelvollisuuden_keskeytyshistoria
    ADD CONSTRAINT oppivelvollisuuden_keskeytyshistoria_pkey PRIMARY KEY (id);


--
-- Name: schema_version schema_version_pk; Type: CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.schema_version
    ADD CONSTRAINT schema_version_pk PRIMARY KEY (version);


--
-- Name: oppivelvollisuudesta_vapautetut_oppija_oid_mitatoity_idx; Type: INDEX; Schema: public; Owner: oph
--

CREATE UNIQUE INDEX oppivelvollisuudesta_vapautetut_oppija_oid_mitatoity_idx ON public.oppivelvollisuudesta_vapautetut USING btree (oppija_oid, mitatoity);


--
-- Name: schema_version_ir_idx; Type: INDEX; Schema: public; Owner: oph
--

CREATE INDEX schema_version_ir_idx ON public.schema_version USING btree (installed_rank);


--
-- Name: schema_version_s_idx; Type: INDEX; Schema: public; Owner: oph
--

CREATE INDEX schema_version_s_idx ON public.schema_version USING btree (success);


--
-- Name: schema_version_vr_idx; Type: INDEX; Schema: public; Owner: oph
--

CREATE INDEX schema_version_vr_idx ON public.schema_version USING btree (version_rank);


--
-- Name: ilmoitus_lisätiedot ilmoitus_fk; Type: FK CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public."ilmoitus_lisätiedot"
    ADD CONSTRAINT ilmoitus_fk FOREIGN KEY (ilmoitus_uuid) REFERENCES public.ilmoitus(uuid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ilmoitus_opiskeluoikeus_konteksti ilmoitus_opiskeluoikeus_konteksti_fk; Type: FK CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.ilmoitus_opiskeluoikeus_konteksti
    ADD CONSTRAINT ilmoitus_opiskeluoikeus_konteksti_fk FOREIGN KEY (ilmoitus_uuid) REFERENCES public.ilmoitus(uuid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: oppivelvollisuuden_keskeytyshistoria oppivelvollisuuden_keskeytyshistoria_ov_keskeytys_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: oph
--

ALTER TABLE ONLY public.oppivelvollisuuden_keskeytyshistoria
    ADD CONSTRAINT oppivelvollisuuden_keskeytyshistoria_ov_keskeytys_uuid_fkey FOREIGN KEY (ov_keskeytys_uuid) REFERENCES public.oppivelvollisuuden_keskeytys(uuid);


--
-- PostgreSQL database dump complete
--

