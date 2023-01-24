import { NuortenPerusopetuksenOpiskeluoikeusjakso } from './NuortenPerusopetuksenOpiskeluoikeusjakso'

/**
 * Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/pages/viewpage.action?pageId=190612597#id-1.Opiskeluoikeudenperustiedotjalis%C3%A4tiedot-Opiskeluoikeudentilat/L%C3%A4sn%C3%A4olojaopintojenlopettaminen)
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeudenTila`
 */
export type NuortenPerusopetuksenOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<NuortenPerusopetuksenOpiskeluoikeusjakso>
}

export const NuortenPerusopetuksenOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<NuortenPerusopetuksenOpiskeluoikeusjakso>
  } = {}
): NuortenPerusopetuksenOpiskeluoikeudenTila => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

NuortenPerusopetuksenOpiskeluoikeudenTila.className =
  'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeudenTila' as const

export const isNuortenPerusopetuksenOpiskeluoikeudenTila = (
  a: any
): a is NuortenPerusopetuksenOpiskeluoikeudenTila =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenOpiskeluoikeudenTila'
