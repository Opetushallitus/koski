import { parseISODate } from '../date/date'
import { isArvioinniton } from '../types/fi/oph/koski/schema/Arvioinniton'
import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { isMahdollisestiArvioinniton } from '../types/fi/oph/koski/schema/MahdollisestiArvioinniton'
import { isPäätasonSuoritus } from '../types/fi/oph/koski/schema/PaatasonSuoritus'
import { isPaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { isPreIBKurssinSuoritus2015 } from '../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { Suoritus } from '../types/fi/oph/koski/schema/Suoritus'
import { parasArviointi } from './arvioinnit'

export const suoritusValmis = (suoritus: Suoritus) => {
  if (isPäätasonSuoritus(suoritus)) {
    const vahvistuspäivä = suoritus.vahvistus?.päivä
    return vahvistuspäivä && isInPast(vahvistuspäivä)
  } else if (
    isArvioinniton(suoritus) ||
    isMahdollisestiArvioinniton(suoritus)
  ) {
    return true
  } else {
    const arviointi =
      suoritus.arviointi && parasArviointi(suoritus.arviointi as Arviointi[])
    const arviointiPäivä = (arviointi as any)?.päivä
    return arviointi
      ? arviointiPäivä
        ? isInPast(arviointiPäivä)
        : !!arviointi
      : false
  }
}

const isInPast = (dateStr?: string) =>
  dateStr !== undefined && parseISODate(dateStr) <= new Date()

export const containsPaikallinenSuoritus = (s: Suoritus): boolean => {
  if (isPaikallinenKoodi(s.koulutusmoduuli.tunniste)) {
    return true
  }
  const osasuoritukset: Suoritus[] | undefined = (s as any).osasuoritukset
  return osasuoritukset?.some(containsPaikallinenSuoritus) ?? false
}
