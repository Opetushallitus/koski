/**
 * Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan
 * Lokalisoitu teksti, jossa mukana ruotsi
 *
 * @see `fi.oph.koski.schema.Swedish`
 */
export type Swedish = {
  $class: 'fi.oph.koski.schema.Swedish'
  sv: string
  en?: string
}

export const Swedish = (o: { sv: string; en?: string }): Swedish => ({
  $class: 'fi.oph.koski.schema.Swedish',
  ...o
})

export const isSwedish = (a: any): a is Swedish =>
  a?.$class === 'fi.oph.koski.schema.Swedish'
