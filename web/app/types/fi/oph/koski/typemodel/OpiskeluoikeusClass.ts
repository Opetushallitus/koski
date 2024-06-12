import { SuoritusClass } from './SuoritusClass'

/**
 * OpiskeluoikeusClass
 *
 * @see `fi.oph.koski.typemodel.OpiskeluoikeusClass`
 */
export type OpiskeluoikeusClass = {
  $class: 'fi.oph.koski.typemodel.OpiskeluoikeusClass'
  className: string
  tyyppi: string
  suoritukset: Array<SuoritusClass>
}

export const OpiskeluoikeusClass = (o: {
  className: string
  tyyppi: string
  suoritukset?: Array<SuoritusClass>
}): OpiskeluoikeusClass => ({
  $class: 'fi.oph.koski.typemodel.OpiskeluoikeusClass',
  suoritukset: [],
  ...o
})

OpiskeluoikeusClass.className =
  'fi.oph.koski.typemodel.OpiskeluoikeusClass' as const

export const isOpiskeluoikeusClass = (a: any): a is OpiskeluoikeusClass =>
  a?.$class === 'fi.oph.koski.typemodel.OpiskeluoikeusClass'
