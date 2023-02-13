import { todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TaiteenPerusopetuksenArviointi } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenArviointi'
import { localize, t } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { TaiteenPerusopetuksenPäätasonSuoritus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenPaatasonSuoritus'
import { KoodiarvotOf } from '../util/koodisto'

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
