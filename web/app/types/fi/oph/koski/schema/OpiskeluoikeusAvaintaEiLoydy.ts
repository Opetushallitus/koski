/**
 * OpiskeluoikeusAvaintaEiLöydy
 *
 * @see `fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy`
 */
export type OpiskeluoikeusAvaintaEiLöydy = {
  $class: 'fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy'
  tyyppi: 'OpiskeluoikeusAvaintaEiLöydy'
  arvo: string
}

export const OpiskeluoikeusAvaintaEiLöydy = (o: {
  tyyppi?: 'OpiskeluoikeusAvaintaEiLöydy'
  arvo: string
}): OpiskeluoikeusAvaintaEiLöydy => ({
  $class: 'fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy',
  tyyppi: 'OpiskeluoikeusAvaintaEiLöydy',
  ...o
})

OpiskeluoikeusAvaintaEiLöydy.className =
  'fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy' as const

export const isOpiskeluoikeusAvaintaEiLöydy = (
  a: any
): a is OpiskeluoikeusAvaintaEiLöydy =>
  a?.$class === 'fi.oph.koski.schema.OpiskeluoikeusAvaintaEiLöydy'
