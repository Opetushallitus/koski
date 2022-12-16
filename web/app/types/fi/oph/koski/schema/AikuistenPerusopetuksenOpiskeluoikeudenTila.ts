import { AikuistenPerusopetuksenOpiskeluoikeusjakso } from './AikuistenPerusopetuksenOpiskeluoikeusjakso'

/**
 * Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/pages/viewpage.action?pageId=190613191#Aikuistenperusopetuksenopiskeluoikeudenl%C3%A4sn%C3%A4olotiedotjaopiskeluoikeudenrahoitusmuodontiedot-Opiskeluoikeudentilat/L%C3%A4sn%C3%A4olojaopintojenlopettaminen)
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenTila`
 */
export type AikuistenPerusopetuksenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AikuistenPerusopetuksenOpiskeluoikeusjakso>
}

export const AikuistenPerusopetuksenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<AikuistenPerusopetuksenOpiskeluoikeusjakso>
  } = {}
): AikuistenPerusopetuksenOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isAikuistenPerusopetuksenOpiskeluoikeudenTila = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeudenTila =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeudenTila'
