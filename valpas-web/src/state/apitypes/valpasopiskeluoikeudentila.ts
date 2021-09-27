import { KoodistoKoodiviite } from "./koodistot"

export type ValpasOpiskeluoikeudenTila = KoodistoKoodiviite<
  "valpasopiskeluoikeudentila",
  | "eronnut"
  | "voimassa"
  | "voimassatulevaisuudessa"
  | "valmistunut"
  | "mitatoity"
  | "katsotaaneronneeksi"
  | "peruutettu"
  | "tuntematon"
>

export const isVoimassa = (tila: ValpasOpiskeluoikeudenTila) =>
  tila.koodiarvo === "voimassa"

export const isVoimassaTulevaisuudessa = (tila: ValpasOpiskeluoikeudenTila) =>
  tila.koodiarvo === "voimassatulevaisuudessa"

export const isValmistunut = (tila: ValpasOpiskeluoikeudenTila) =>
  tila.koodiarvo === "valmistunut"
