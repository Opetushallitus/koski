import { PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso } from './PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'

/**
 * Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/display/OPHPALV/Perusopetukseen+valmistava+opetus#Perusopetukseenvalmistavaopetus-Opiskeluoikeudentilat:L%C3%A4sn%C3%A4olojaopintojenlopettaminen)
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila`
 */
export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso>
}

export const PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso>
  } = {}
): PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila => ({
  $class:
    'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila' as const

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila =>
  a?.$class ===
  'fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
