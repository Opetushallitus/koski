/**
 * TyhjäTukijakso
 *
 * @see `fi.oph.koski.schema.TyhjäTukijakso`
 */
export type TyhjäTukijakso = {
  $class: 'fi.oph.koski.schema.TyhjäTukijakso'
}

export const TyhjäTukijakso = (o: object = {}): TyhjäTukijakso => ({
  $class: 'fi.oph.koski.schema.TyhjäTukijakso',
  ...o
})

TyhjäTukijakso.className = 'fi.oph.koski.schema.TyhjäTukijakso' as const

export const isTyhjäTukijakso = (a: any): a is TyhjäTukijakso =>
  a?.$class === 'fi.oph.koski.schema.TyhjäTukijakso'
