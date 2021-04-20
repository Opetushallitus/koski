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
  maa?: string
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

export const isIlmoitettu = (
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
): yhteystiedot is Yhteystiedot<AlkuperäHakemukselta> => {
  const a = yhteystiedot.alkuperä as AlkuperäHakemukselta
  return (
    a.hakuNimi !== undefined &&
    a.haunAlkamispaivämäärä !== undefined &&
    a.hakuOid !== undefined &&
    a.hakemusOid !== undefined
  )
}

export const isVirallinen = (
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
): yhteystiedot is Yhteystiedot<AlkuperäRekisteristä> => {
  const a = yhteystiedot.alkuperä as AlkuperäRekisteristä
  return a.alkuperä !== undefined && a.tyyppi !== undefined
}
