import { LocalizedString } from './LocalizedString'

/**
 * OpiskeluvalmiuksiaTukevienOpintojenJakso
 *
 * @see `fi.oph.koski.schema.OpiskeluvalmiuksiaTukevienOpintojenJakso`
 */
export type OpiskeluvalmiuksiaTukevienOpintojenJakso = {
  $class: 'fi.oph.koski.schema.OpiskeluvalmiuksiaTukevienOpintojenJakso'
  alku: string
  loppu: string
  kuvaus: LocalizedString
}

export const OpiskeluvalmiuksiaTukevienOpintojenJakso = (o: {
  alku: string
  loppu: string
  kuvaus: LocalizedString
}): OpiskeluvalmiuksiaTukevienOpintojenJakso => ({
  $class: 'fi.oph.koski.schema.OpiskeluvalmiuksiaTukevienOpintojenJakso',
  ...o
})

OpiskeluvalmiuksiaTukevienOpintojenJakso.className =
  'fi.oph.koski.schema.OpiskeluvalmiuksiaTukevienOpintojenJakso' as const

export const isOpiskeluvalmiuksiaTukevienOpintojenJakso = (
  a: any
): a is OpiskeluvalmiuksiaTukevienOpintojenJakso =>
  a?.$class === 'fi.oph.koski.schema.OpiskeluvalmiuksiaTukevienOpintojenJakso'
