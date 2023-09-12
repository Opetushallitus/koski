import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import {
  Koodistokoodiviite,
  isKoodistokoodiviite
} from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukutaitokoulutuksenArviointi } from '../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import {
  isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import {
  OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus,
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsasuoritus'
import {
  VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import {
  VapaanSivistystyönJotpaKoulutuksenSuoritus,
  isVapaanSivistystyönJotpaKoulutuksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import {
  VapaanSivistystyönJotpaKoulutus,
  isVapaanSivistystyönJotpaKoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import {
  VapaanSivistystyönLukutaitokoulutus,
  isVapaanSivistystyönLukutaitokoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenTyoelamaJakso'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import {
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus,
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import {
  VapaanSivistystyönVapaatavoitteinenKoulutus,
  isVapaanSivistystyönVapaatavoitteinenKoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteinenKoulutus'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import {
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022,
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import {
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022,
  VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import {
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022,
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import {
  VSTKotoutumiskoulutus2022,
  isVSTKotoutumiskoulutus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutus2022'

export const isLaajuuksellinenVSTKoulutusmoduuli = (
  x: any
): x is
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
  | VSTKotoutumiskoulutus2022
  | VapaanSivistystyönJotpaKoulutus
  | VapaanSivistystyönLukutaitokoulutus
  | VapaanSivistystyönVapaatavoitteinenKoulutus => {
  return (
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(x) ||
    isVSTKotoutumiskoulutus2022(x) ||
    isVapaanSivistystyönJotpaKoulutus(x) ||
    isVapaanSivistystyönLukutaitokoulutus(x) ||
    isVapaanSivistystyönVapaatavoitteinenKoulutus(x)
  )
}

export const isPerusteellinenVSTKoulutusmoduuli = (
  x: any
): x is
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
  | VSTKotoutumiskoulutus2022
  | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
  | VapaanSivistystyönLukutaitokoulutus => {
  return (
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(x) ||
    isVSTKotoutumiskoulutus2022(x) ||
    isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(x) ||
    isVapaanSivistystyönLukutaitokoulutus(x)
  )
}

type OsasuoritusOf<T> = T extends { osasuoritukset?: any }
  ? NonNullable<T['osasuoritukset']>[number]
  : never

export type VSTOsasuoritus =
  | OsasuoritusOf<VapaanSivistystyönPäätasonSuoritus>
  | OsasuoritusOf<OsasuoritusOf<VapaanSivistystyönPäätasonSuoritus>>
  | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus

export type VSTOsasuoritusOsasuorituksilla = Extract<
  VSTOsasuoritus,
  {
    osasuoritukset?: any
  }
>

export type VSTKoulutusmoduuli = VSTOsasuoritus['koulutusmoduuli']

export function isVSTOsasuoritusOsasuorituksilla(
  x: VSTOsasuoritus
): x is VSTOsasuoritusOsasuorituksilla {
  return true
}

export type VSTOsasuoritusArvioinnilla = Extract<
  VSTOsasuoritus,
  {
    arviointi?: Arviointi[]
  }
>

export type VSTKoulutusmoduuliKuvauksella = Extract<
  VSTKoulutusmoduuli,
  { kuvaus?: any }
>

export type VSTOsasuoritusTunnustuksella = Extract<
  VSTOsasuoritus,
  { tunnustettu?: any }
>

export type VSTOsasuoritusLukutaitokoulutuksenArvioinnilla = Extract<
  VSTOsasuoritus,
  {
    arviointi?: LukutaitokoulutuksenArviointi[]
  }
>

export type VSTOsasuoritusIlmanArviointia = Exclude<
  VSTOsasuoritus,
  {
    arviointi?: Arviointi[]
  }
>

export function isVSTOsasuoritusArvioinnilla(
  s: VSTOsasuoritus
): s is VSTOsasuoritusArvioinnilla {
  switch (s.$class) {
    case MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus.className:
    case OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus.className:
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus.className:
    case VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022.className:
    case VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022.className:
    case VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022.className:
    case VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus.className:
    case VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus.className:
    case VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso.className:
    case VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus.className:
      return true
    default:
      return false
  }
}

export function isVSTKoulutusmoduuliKuvauksella(
  s: VSTKoulutusmoduuli
): s is VSTKoulutusmoduuliKuvauksella {
  switch (s.$class) {
    case 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus':
    case 'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus':
    case 'fi.oph.koski.schema.MuuallaSuoritetutVapaanSivistystyönOpinnot':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus':
    case 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022':
      return true
    default:
      return false
  }
}

export function isTunnustettuVSTOsasuoritus(
  s: VSTOsasuoritus
): s is VSTOsasuoritusTunnustuksella {
  switch (s.$class) {
    case 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus':
    case 'fi.oph.koski.schema.MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus':
      return true
    default:
      return false
  }
}

export type VSTArviointi = NonNullable<
  VSTOsasuoritusArvioinnilla['arviointi']
>[number]

export type VSTTaitotaso = NonNullable<
  NonNullable<
    VSTOsasuoritusLukutaitokoulutuksenArvioinnilla['arviointi']
  >[number]['taitotaso']
>

export type CreateVSTArviointi = (
  o: VSTOsasuoritusArvioinnilla
) => (arvosana: any) => VSTArviointi | null

export type CreateVSTTaitotaso = (
  o: VSTOsasuoritusLukutaitokoulutuksenArvioinnilla
) => (arvosana: any) => VSTTaitotaso | null

export const isVSTOsasuoritusJollaOsasuorituksia = (
  s: any
): s is
  | VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  | VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  | VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
  | VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  | OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =>
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(s) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(s) ||
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(s) ||
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(s) ||
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(s) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus(s) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
    s
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
    s
  )

export function hasOpintokokonaisuus(
  x: any
): x is
  | VapaanSivistystyönJotpaKoulutuksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus {
  return (
    isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(x) ||
    isVapaanSivistystyönJotpaKoulutuksenSuoritus(x)
  )
}

/**
 * Tarkistaa, että koodistokoodiviitteen koodistoUri ja koodiarvo täsmäävät haluttuun arvoon. Tämä lisäksi tarkentaa Koodistokoodiviitteen tyypin.
 * @param val Tarkistettava koodistokoodiviite
 * @param koodistoUri Koodiston URI
 * @param koodiarvo  Koodiarvo
 * @returns Onko Koodistokoodiviite tyyppiä Koodistokoodiviite<T, K>
 */
export function narrowKoodistokoodiviite<T extends string, K extends string>(
  val: unknown,
  koodistoUri: T,
  koodiarvo?: K
): val is K extends string
  ? Koodistokoodiviite<T, K>
  : Koodistokoodiviite<T, string> {
  if (!isKoodistokoodiviite(val)) {
    return false
  }
  if (val.koodistoUri !== koodistoUri) {
    return false
  }
  if (koodiarvo !== undefined && val.koodiarvo !== koodiarvo) {
    return false
  }
  return true
}
