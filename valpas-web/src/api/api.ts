import { User } from "../state/auth"
import { Oppija } from "../state/oppijat"
import { apiGet, apiPost, mockApi } from "./apiFetch"

/**
 * Login
 */
export const fetchLogin = async (username: string, password: string) =>
  apiPost<User>("login", {
    body: {
      username,
      password,
    },
  })

/**
 * Get current user
 */
export const fetchCurrentUser = async () => apiGet<User>("api/user")

/**
 * Get oppijat
 */
export const fetchOppijat = mockApi<Oppija[], []>(() => [
  {
    oid: "1.123.123.123.123.123.1",
    nimi: "Aaltonen Ada Adalmiina",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "aktiivinen",
    },
    valintatiedot: [
      {
        hakukohdenumero: 1,
        hakukohde: {
          oid: "1.3.3.3.3.3.3",
          nimi: { fi: "Ressun lukio" },
        },
        tila: "hyväksytty",
      },
    ],
    vastaanotetut: [
      {
        oid: "1.3.3.3.3.3.3",
        nimi: { fi: "Ressun lukio" },
      },
    ],
    lasna: [
      {
        oid: "1.3.3.3.3.3.3",
        nimi: { fi: "Ressun lukio" },
      },
    ],
  },
  {
    oid: "1.123.123.123.123.123.2",
    nimi: "Kinnunen Jami Jalmari",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "aktiivinen",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.3",
    nimi: "Laitela Niklas Henri",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "aktiivinen",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.4",
    nimi: "Mäkinen Tapio Kalervo",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "ei",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.5",
    nimi: "Ojanen Jani Kalle",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "ei",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.6",
    nimi: "Pohjanen Anna Maria",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "puutteellinen",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.7",
    nimi: "Raatikainen Hanna Sisko",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "aktiivinen",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.8",
    nimi: "Vuorenmaa Maija Kaarina",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "aktiivinen",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
  {
    oid: "1.123.123.123.123.123.9",
    nimi: "Ylänen Toni Vilhelm",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    luokka: "9A",
    hakemuksentila: {
      tila: "luonnos",
    },
    valintatiedot: [],
    vastaanotetut: [],
    lasna: [],
  },
])
