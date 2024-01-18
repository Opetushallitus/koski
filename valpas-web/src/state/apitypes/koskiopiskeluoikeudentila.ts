import { KoodistoKoodiviite } from "./koodistot"

export type KoskiOpiskeluoikeudenTila = KoodistoKoodiviite<
  "koskiopiskeluoikeudentila",
  | "eronnut"
  | "hyvaksytystisuoritettu"
  | "katsotaaneronneeksi"
  | "keskeytynyt"
  | "lasna"
  | "loma"
  | "mitatoity"
  | "peruutettu"
  | "valiaikaisestikeskeytynyt"
  | "valmistunut"
>

export const isSuorittamisenValvonnassaIlmoitettavaTila = (
  tila: KoskiOpiskeluoikeudenTila,
) =>
  ["eronnut", "katsotaaneronneeksi", "keskeytynyt", "peruutettu"].includes(
    tila.koodiarvo,
  )
