import { OppijaHakutilanteillaLaajatTiedot } from "./oppija"

export type KansalaisnäkymänTiedot = {
  omatTiedot?: OppijaHakutilanteillaLaajatTiedot
  huollettavat: OppijaHakutilanteillaLaajatTiedot[]
  huollettavatIlmanTietoja: KansalainenIlmanTietoja[]
}

export type KansalainenIlmanTietoja = {
  nimi: string
}
