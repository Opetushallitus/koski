/**
 * Palauttaa Ahvenanmaan perusopetuksen vuosiluokkaraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka`
 */
export type MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka'
  vuosiluokka: string
  paiva: string
  type: 'ahvenanmaanPerusopetuksenVuosiluokka'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka = (o: {
  vuosiluokka: string
  paiva: string
  type?: 'ahvenanmaanPerusopetuksenVuosiluokka'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka => ({
  type: 'ahvenanmaanPerusopetuksenVuosiluokka',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka' as const

export const isMassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka = (
  a: any
): a is MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryAhvenanmaanPerusopetuksenVuosiluokka'
