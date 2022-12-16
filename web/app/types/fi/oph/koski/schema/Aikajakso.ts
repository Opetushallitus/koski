/**
 * Aikajakson pituus (alku- ja loppupäivämäärä)
 *
 * @see `fi.oph.koski.schema.Aikajakso`
 */
export type Aikajakso = {
  $class: 'fi.oph.koski.schema.Aikajakso'
  alku: string
  loppu?: string
}

export const Aikajakso = (o: { alku: string; loppu?: string }): Aikajakso => ({
  $class: 'fi.oph.koski.schema.Aikajakso',
  ...o
})

export const isAikajakso = (a: any): a is Aikajakso => a?.$class === 'Aikajakso'
