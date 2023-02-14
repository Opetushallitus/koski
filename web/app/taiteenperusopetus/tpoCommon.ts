import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import { todayISODate } from '../date/date'
import { localize, t } from '../i18n/i18n'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { TaiteenPerusopetuksenArviointi } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenArviointi'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { KoodiarvotOf } from '../util/koodisto'
import { EPSILON, sum } from '../util/numbers'

export const createTpoArviointi = (
  arvosana: Koodistokoodiviite<
    'arviointiasteikkotaiteenperusopetus',
    'hyvaksytty'
  >
): TaiteenPerusopetuksenArviointi =>
  TaiteenPerusopetuksenArviointi({
    arvosana,
    päivä: todayISODate()
  })

export const taiteenPerusopetuksenSuorituksenNimi = (
  suoritus: TaiteenPerusopetuksenPäätasonSuoritus
): LocalizedString => {
  const titles: Record<
    KoodiarvotOf<TaiteenPerusopetuksenPäätasonSuoritus['tyyppi']>,
    string
  > = {
    taiteenperusopetuksenlaajanoppimaaranperusopinnot:
      'Laajan oppimäärän perusopinnot',
    taiteenperusopetuksenlaajanoppimaaransyventavatopinnot:
      'Laajan oppimäärän syventävät opinnot',
    taiteenperusopetuksenyleisenoppimaaranteemaopinnot:
      'Yleisen oppimäärän teemaopinnot',
    taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot:
      'Yleisen oppimäärän yhteiset opinnot'
  }

  return localize(
    `${t(titles[suoritus.tyyppi.koodiarvo])}, ${t(
      suoritus.koulutusmoduuli.taiteenala.nimi
    ).toLowerCase()}`
  )
}

export const minimimääräArvioitujaOsasuorituksia = (
  suoritus: TaiteenPerusopetuksenPäätasonSuoritus
): boolean =>
  pipe(
    O.fromNullable(suoritus.osasuoritukset),
    O.map(A.filter((os) => Boolean(os.arviointi))),
    O.map(A.map((os) => os.koulutusmoduuli.laajuus.arvo)),
    O.map(sum),
    O.fold(
      () => false,
      (laajuus) =>
        laajuus >= minimilaajuudet[suoritus.tyyppi.koodiarvo] - EPSILON
    )
  )

// Jos päivität näitä arvoja, päivitä myös TaiteenPerusopetusValidation.scala -> validateSuoritustenLaajuus
const minimilaajuudet: Record<
  KoodiarvotOf<TaiteenPerusopetuksenPäätasonSuoritus['tyyppi']>,
  number
> = {
  taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot: 11.1,
  taiteenperusopetuksenyleisenoppimaaranteemaopinnot: 7.4,
  taiteenperusopetuksenlaajanoppimaaranperusopinnot: 29.6,
  taiteenperusopetuksenlaajanoppimaaransyventavatopinnot: 18.5
}
