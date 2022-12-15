export type Koodistokoodiviite<
  T extends string = string,
  S extends string = string
> = {
  koodiarvo: T
  koodistoUri: S
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoVersio?: number
}

export type PaikallinenKoodi = {
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}

export type LocalizedString = {
  fi: string
  sv?: string
  en?: string
}

export type LaajuusOpintopisteissä = {
  arvo: number
  yksikkö: Koodistokoodiviite
}
