/**
 * Arvioitsijan/arvioitsijoiden tiedot
 *
 * @see `fi.oph.koski.schema.Arvioitsija`
 */
export type Arvioitsija = {
  $class: 'fi.oph.koski.schema.Arvioitsija'
  nimi: string
}

export const Arvioitsija = (o: { nimi: string }): Arvioitsija => ({
  $class: 'fi.oph.koski.schema.Arvioitsija',
  ...o
})

export const isArvioitsija = (a: any): a is Arvioitsija =>
  a?.$class === 'fi.oph.koski.schema.Arvioitsija'
