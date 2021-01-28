import { ISODate, LocalizedString, Oid } from "./types"

export type Oppija = {
  oid: Oid
  nimi: string
  oppilaitos: Oppilaitos
  syntymaaika: ISODate
  luokka: string
  hakemuksentila: Hakemuksentila
  valintatiedot: Valintatieto[]
  vastaanotetut: Oppilaitos[]
  lasna: Oppilaitos[]
}

export type Oppilaitos = {
  oid: Oid
  nimi: LocalizedString
}

export type Hakemuksentila =
  | { tila: "ei" }
  | { tila: "aktiivinen" }
  | { tila: "passiivinen" }
  | { tila: "puutteellinen" }
  | { tila: "luonnos" }

export type Valintatieto = {
  hakukohde: Oppilaitos
  tila: ValintatietoTila
  hakukohdenumero?: number
}

export type ValintatietoTila = "hyväksytty" | "varasija"
