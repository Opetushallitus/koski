import { AmmatillinenOpiskeluoikeusjakso } from './AmmatillinenOpiskeluoikeusjakso'

/**
 * Ks. tarkemmin ammatillisen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/pages/viewpage.action?pageId=190612822#id-1.1.Ammatillistenopiskeluoikeuksienl%C3%A4sn%C3%A4olotiedotjaopiskeluoikeudenrahoitusmuodontiedot-Opiskeluoikeudentilat)
 *
 * @see `fi.oph.koski.schema.AmmatillinenOpiskeluoikeudenTila`
 */
export type AmmatillinenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AmmatillinenOpiskeluoikeusjakso>
}

export const AmmatillinenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<AmmatillinenOpiskeluoikeusjakso>
  } = {}
): AmmatillinenOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

export const isAmmatillinenOpiskeluoikeudenTila = (
  a: any
): a is AmmatillinenOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.AmmatillinenOpiskeluoikeudenTila'
