import { SuoritusClass } from './SuoritusClass'

/**
 * OpiskeluoikeusClass
 *
 * @see `fi.oph.koski.typemodel.OpiskeluoikeusClass`
 */
export type OpiskeluoikeusClass = {
  $class: 'fi.oph.koski.typemodel.OpiskeluoikeusClass'
  tyyppi: string
  lisätiedot: Array<string>
  suoritukset: Array<SuoritusClass>
  className: string
  opiskeluoikeusjaksot: Array<string>
}

export const OpiskeluoikeusClass = (o: {
  tyyppi: string
  lisätiedot?: Array<string>
  suoritukset?: Array<SuoritusClass>
  className: string
  opiskeluoikeusjaksot?: Array<string>
}): OpiskeluoikeusClass => ({
  $class: 'fi.oph.koski.typemodel.OpiskeluoikeusClass',
  lisätiedot: [],
  suoritukset: [],
  opiskeluoikeusjaksot: [],
  ...o
})

OpiskeluoikeusClass.className =
  'fi.oph.koski.typemodel.OpiskeluoikeusClass' as const

export const isOpiskeluoikeusClass = (a: any): a is OpiskeluoikeusClass =>
  a?.$class === 'fi.oph.koski.typemodel.OpiskeluoikeusClass'
