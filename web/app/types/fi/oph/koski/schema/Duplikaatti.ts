/**
 * Duplikaatti
 *
 * @see `fi.oph.koski.schema.Duplikaatti`
 */
export type Duplikaatti = {
  $class: 'fi.oph.koski.schema.Duplikaatti'
  tyyppi: 'Duplikaatti'
  arvo: string
}

export const Duplikaatti = (o: {
  tyyppi?: 'Duplikaatti'
  arvo: string
}): Duplikaatti => ({
  $class: 'fi.oph.koski.schema.Duplikaatti',
  tyyppi: 'Duplikaatti',
  ...o
})

Duplikaatti.className = 'fi.oph.koski.schema.Duplikaatti' as const

export const isDuplikaatti = (a: any): a is Duplikaatti =>
  a?.$class === 'fi.oph.koski.schema.Duplikaatti'
