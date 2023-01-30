/**
 * Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
 *
 * @see `fi.oph.koski.schema.Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu`
 */
export type Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu = {
  $class: 'fi.oph.koski.schema.Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu'
  maksettu?: boolean
  summa?: number
  apuraha?: number
}

export const Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu = (
  o: {
    maksettu?: boolean
    summa?: number
    apuraha?: number
  } = {}
): Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu => ({
  $class: 'fi.oph.koski.schema.Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu',
  ...o
})

Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu.className =
  'fi.oph.koski.schema.Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu' as const

export const isLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu = (
  a: any
): a is Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu =>
  a?.$class ===
  'fi.oph.koski.schema.Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu'
