import { todayISODate } from '../../date/date'
import { Arviointi } from '../../types/fi/oph/koski/schema/Arviointi'
import { isLukutaitokoulutuksenArviointi } from '../../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { isVSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { isVapaanSivistystyöJotpaKoulutuksenArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyoJotpaKoulutuksenArviointi'
import { isVapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyoVapaatavoitteisenKoulutuksenArviointi'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenTyoelamaJakso'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import {
  isVSTKoulutuksenSuoritus,
  VSTArviointi,
  VSTArviointiPäivällä,
  VSTSuoritus,
  VSTSuoritusArvioinnilla
} from './types'
import { isVapaanSivistystyönOsaamismerkinArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinArviointi'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'

export const createArviointi =
  <T extends Arviointi>(
    ctor: (p: { arvosana: T['arvosana']; päivä: string }) => T
  ) =>
  (arvosana: T['arvosana']) =>
    ctor({
      arvosana,
      päivä: todayISODate()
    })

export const arviointienPuolestaVahvistettavissa = (
  oo: VapaanSivistystyönOpiskeluoikeus
): boolean =>
  oo.suoritukset
    .filter(isVSTKoulutuksenSuoritus)
    .flatMap((s: any) => s.osasuoritukset || [])
    .filter(isVSTOsasuoritusArvioinnilla)
    .filter((os) => os.arviointi === undefined).length === 0

export function isVSTOsasuoritusArvioinnilla(
  s: VSTSuoritus
): s is VSTSuoritusArvioinnilla {
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

export function isVSTArviointiPäivällä(
  x: VSTArviointi
): x is Extract<VSTArviointiPäivällä, { päivä?: any }> {
  return (
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
      x
    ) ||
    isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(
      x
    ) ||
    isVSTKotoutumiskoulutuksenOsasuorituksenArviointi2022(x) ||
    isVapaanSivistystyöJotpaKoulutuksenArviointi(x) ||
    isLukutaitokoulutuksenArviointi(x) ||
    isVapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi(x) ||
    isVapaanSivistystyönOsaamismerkinArviointi(x)
  )
}
