/**
 * Lokalisoitu teksti. Vähintään yksi kielistä (fi/sv/en) vaaditaan
 * Lokalisoitu teksti, jossa mukana englanti
 *
 * @see `fi.oph.koski.schema.English`
 */
export type English = {
  $class: 'fi.oph.koski.schema.English'
  en: string
}

export const English = (o: { en: string }): English => ({
  $class: 'fi.oph.koski.schema.English',
  ...o
})

export const isEnglish = (a: any): a is English =>
  a?.$class === 'fi.oph.koski.schema.English'
