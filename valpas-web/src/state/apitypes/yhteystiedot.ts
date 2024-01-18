import { ISODateTime, LocalizedString, Oid } from "../common"
import { KoodistoKoodiviite } from "./koodistot"

export type Yhteystiedot<T extends YhteystietojenAlkuperä> = {
  alkuperä: T
  yhteystietoryhmänNimi: LocalizedString
  henkilönimi?: string
  sähköposti?: string
  puhelinnumero?: string
  matkapuhelinnumero?: string
  lähiosoite?: string
  postitoimipaikka?: string
  postinumero?: string
  maa?: LocalizedString
}

export type YhteystietojenAlkuperä = AlkuperäHakemukselta | AlkuperäRekisteristä

export type AlkuperäHakemukselta = {
  hakuNimi: LocalizedString
  haunAlkamispaivämäärä: ISODateTime
  hakemuksenMuokkauksenAikaleima?: ISODateTime
  hakuOid: Oid
  hakemusOid: Oid
}

export type AlkuperäRekisteristä = {
  alkuperä: KoodistoKoodiviite<"yhteystietojenalkupera">
  tyyppi: KoodistoKoodiviite<"yhteystietotyypit">
}

export const isHakemukselta = (
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>,
): yhteystiedot is Yhteystiedot<AlkuperäHakemukselta> =>
  isAlkuperäHakemukselta(yhteystiedot.alkuperä)

export const isAlkuperäHakemukselta = (
  alkuperä: YhteystietojenAlkuperä,
): alkuperä is AlkuperäHakemukselta => {
  const a = alkuperä as AlkuperäHakemukselta
  return (
    a.hakuNimi !== undefined &&
    a.haunAlkamispaivämäärä !== undefined &&
    a.hakuOid !== undefined &&
    a.hakemusOid !== undefined
  )
}

export const isRekisteristä = (
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>,
): yhteystiedot is Yhteystiedot<AlkuperäRekisteristä> =>
  isAlkuperäRekisteristä(yhteystiedot.alkuperä)

export const isAlkuperäRekisteristä = (
  alkuperä: YhteystietojenAlkuperä,
): alkuperä is AlkuperäRekisteristä => {
  const a = alkuperä as AlkuperäRekisteristä
  return a.alkuperä !== undefined && a.tyyppi !== undefined
}
