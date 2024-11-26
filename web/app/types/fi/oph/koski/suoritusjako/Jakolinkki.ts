/**
 * Jakolinkki
 *
 * @see `fi.oph.koski.suoritusjako.Jakolinkki`
 */
export type Jakolinkki = {
  $class: 'fi.oph.koski.suoritusjako.Jakolinkki'
  voimassaAsti: string
}

export const Jakolinkki = (o: { voimassaAsti: string }): Jakolinkki => ({
  $class: 'fi.oph.koski.suoritusjako.Jakolinkki',
  ...o
})

Jakolinkki.className = 'fi.oph.koski.suoritusjako.Jakolinkki' as const

export const isJakolinkki = (a: any): a is Jakolinkki =>
  a?.$class === 'fi.oph.koski.suoritusjako.Jakolinkki'
