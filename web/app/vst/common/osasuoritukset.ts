import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import { VSTSuoritusArvioinnilla } from './types'

const isArvioitu = (osasuoritus?: VSTSuoritusArvioinnilla): boolean =>
  osasuoritus?.arviointi !== undefined && osasuoritus.arviointi.length > 0

export const isCompletedJotpaOsasuoritus =
  (suoritus: VapaanSivistystyönJotpaKoulutuksenSuoritus) =>
  (rowIndex: number) =>
    isArvioitu((suoritus.osasuoritukset || [])[rowIndex])

export const isCompletedVapaatavoitteinenOsasuoritus =
  (suoritus: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus) =>
  (rowIndex: number) =>
    isArvioitu((suoritus.osasuoritukset || [])[rowIndex])

export const isCompletedLukutaitokoulutuksenOsasuoritus =
  (suoritus: VapaanSivistystyönLukutaitokoulutuksenSuoritus) =>
  (rowIndex: number) =>
    isArvioitu((suoritus.osasuoritukset || [])[rowIndex])

export const isCompletedKoto2022Osasuoritus =
  (
    suoritus: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
  ) =>
  (rowIndex: number) => {
    const osasuoritus = (suoritus.osasuoritukset || [])[rowIndex]
    return isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022(osasuoritus)
      ? undefined
      : isArvioitu(osasuoritus)
  }
