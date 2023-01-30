/**
 * Korkeakoulun opiskeluoikeuden lukuvuosimaksut
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLukuvuosimaksu`
 */
export type KorkeakoulunOpiskeluoikeudenLukuvuosimaksu = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'
  alku: string
  loppu?: string
  summa?: number
}

export const KorkeakoulunOpiskeluoikeudenLukuvuosimaksu = (o: {
  alku: string
  loppu?: string
  summa?: number
}): KorkeakoulunOpiskeluoikeudenLukuvuosimaksu => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLukuvuosimaksu',
  ...o
})

KorkeakoulunOpiskeluoikeudenLukuvuosimaksu.className =
  'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLukuvuosimaksu' as const

export const isKorkeakoulunOpiskeluoikeudenLukuvuosimaksu = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenLukuvuosimaksu =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'
