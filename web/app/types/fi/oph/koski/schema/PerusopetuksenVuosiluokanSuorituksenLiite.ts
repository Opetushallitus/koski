import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Vuosiluokan todistuksen liitetieto
 *
 * @see `fi.oph.koski.schema.PerusopetuksenVuosiluokanSuorituksenLiite`
 */
export type PerusopetuksenVuosiluokanSuorituksenLiite = {
  $class: 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuorituksenLiite'
  tunniste: Koodistokoodiviite<
    'perusopetuksentodistuksenliitetieto',
    'kayttaytyminen' | 'tyoskentely'
  >
  kuvaus: LocalizedString
}

export const PerusopetuksenVuosiluokanSuorituksenLiite = (o: {
  tunniste: Koodistokoodiviite<
    'perusopetuksentodistuksenliitetieto',
    'kayttaytyminen' | 'tyoskentely'
  >
  kuvaus: LocalizedString
}): PerusopetuksenVuosiluokanSuorituksenLiite => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuorituksenLiite',
  ...o
})

PerusopetuksenVuosiluokanSuorituksenLiite.className =
  'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuorituksenLiite' as const

export const isPerusopetuksenVuosiluokanSuorituksenLiite = (
  a: any
): a is PerusopetuksenVuosiluokanSuorituksenLiite =>
  a?.$class === 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuorituksenLiite'
