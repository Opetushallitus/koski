import { LukionOpiskeluoikeusjakso } from './LukionOpiskeluoikeusjakso'

/**
 * Ks. tarkemmin lukion ja IB-tutkinnon opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/display/OPHPALV/1.2.+Lukion+opiskeluoikeuden+tilajaksot+ja+opintojen+rahoitusmuodon+ilmaiseminen+tilajaksossa#id-1.2.Lukionopiskeluoikeudentilajaksotjaopintojenrahoitusmuodonilmaiseminentilajaksossa-Opiskeluoikeudentilat)
 *
 * @see `fi.oph.koski.schema.LukionOpiskeluoikeudenTila`
 */
export type LukionOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<LukionOpiskeluoikeusjakso>
}

export const LukionOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<LukionOpiskeluoikeusjakso>
  } = {}
): LukionOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.LukionOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

LukionOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.LukionOpiskeluoikeudenTila' as const

export const isLukionOpiskeluoikeudenTila = (
  a: any
): a is LukionOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.LukionOpiskeluoikeudenTila'
