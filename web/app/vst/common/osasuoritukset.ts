import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
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
