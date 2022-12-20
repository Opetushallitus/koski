/**
 * Duplikaatti
 *
 * @see `fi.oph.koski.schema.Duplikaatti`
 */
export type Duplikaatti = {
  $class: 'fi.oph.koski.schema.Duplikaatti'
  tyyppi: string
  arvo: string
}

export const Duplikaatti = (o: {
  tyyppi: string
  arvo: string
}): Duplikaatti => ({ $class: 'fi.oph.koski.schema.Duplikaatti', ...o })

export const isDuplikaatti = (a: any): a is Duplikaatti =>
  a?.$class === 'fi.oph.koski.schema.Duplikaatti'
