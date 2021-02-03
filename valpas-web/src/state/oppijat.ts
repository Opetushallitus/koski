import {
  HakemuksentilaKoodistoviite,
  koodistoviite,
  ValintatietotilaKoodistoviite,
} from "./koodistot"
import { ISODate, LocalizedString, Oid } from "./types"

export type Oppija = {
  oid: Oid
  nimi: string
  hetu: string
  oppilaitos: Oppilaitos
  syntymaaika: ISODate
  ryhmä: string
  hakemukset: Hakemus[]
}

export type Oppilaitos = {
  oid: Oid
  nimi: LocalizedString
}

export type Hakemus = {
  nimi: LocalizedString
  luotu: ISODate
  tila: HakemuksentilaKoodistoviite
  valintatiedot: Valintatieto[]
}

export type Valintatieto = {
  hakukohdenumero?: number
  hakukohde: Oppilaitos
  tila?: ValintatietotilaKoodistoviite
}

// Mock-dataa, joka siirtyy myöhemmin backendin puolelle
const hakemuksentila = koodistoviite("hakemuksentila")
const valintatieto = koodistoviite("valintatietotila")

export const mockOppijat: Oppija[] = [
  {
    oid: "1.123.123.123.123.123.1",
    nimi: "Aaltonen Ada Adalmiina",
    hetu: "291105A636C",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [
      {
        nimi: { fi: "Yhteishaku 2021" },
        luotu: "2021-02-03",
        tila: hakemuksentila("aktiivinen"),
        valintatiedot: [
          {
            hakukohdenumero: 1,
            hakukohde: {
              oid: "1.3.3.3.3.3.3",
              nimi: { fi: "Ressun lukio" },
            },
            tila: valintatieto("läsnä"),
          },
        ],
      },
    ],
  },
  {
    oid: "1.123.123.123.123.123.2",
    nimi: "Kinnunen Jami Jalmari",
    hetu: "120605A823D",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [
      {
        nimi: { fi: "Yhteishaku 2021" },
        luotu: "2021-02-03",
        tila: hakemuksentila("aktiivinen"),
        valintatiedot: [
          {
            hakukohdenumero: 1,
            hakukohde: {
              oid: "1.3.3.3.3.3.3",
              nimi: { fi: "Ressun lukio" },
            },
            tila: valintatieto("vastaanotettu"),
          },
        ],
      },
    ],
  },
  {
    oid: "1.123.123.123.123.123.3",
    nimi: "Laitela Niklas Henri",
    hetu: "240505A5385",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
  {
    oid: "1.123.123.123.123.123.4",
    nimi: "Mäkinen Tapio Kalervo",
    hetu: "140805A143C",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
  {
    oid: "1.123.123.123.123.123.5",
    nimi: "Ojanen Jani Kalle",
    hetu: "190605A037K",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
  {
    oid: "1.123.123.123.123.123.6",
    nimi: "Pohjanen Anna Maria",
    hetu: "060505A314A",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
  {
    oid: "1.123.123.123.123.123.7",
    nimi: "Raatikainen Hanna Sisko",
    hetu: "270805A578T",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
  {
    oid: "1.123.123.123.123.123.8",
    nimi: "Vuorenmaa Maija Kaarina",
    hetu: "240105A381V",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
  {
    oid: "1.123.123.123.123.123.9",
    nimi: "Ylänen Toni Vilhelm",
    hetu: "200705A606C",
    oppilaitos: {
      oid: "1.123.123.123.123.123.123",
      nimi: { fi: "Järvenpään yhteiskoulu" },
    },
    syntymaaika: "2005-07-31",
    ryhmä: "9A",
    hakemukset: [],
  },
]
