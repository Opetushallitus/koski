/**
 * Palauttaa perusopetuksen vuosiluokkaraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka`
 */
export type MassaluovutusQueryPerusopetuksenVuosiluokka = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka'
  vuosiluokka: string
  paiva: string
  type: 'perusopetuksenVuosiluokka'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}

export const MassaluovutusQueryPerusopetuksenVuosiluokka = (o: {
  vuosiluokka: string
  paiva: string
  type?: 'perusopetuksenVuosiluokka'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  language?: 'fi' | 'sv' | 'en'
  kotikuntaPvm?: string
}): MassaluovutusQueryPerusopetuksenVuosiluokka => ({
  type: 'perusopetuksenVuosiluokka',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryPerusopetuksenVuosiluokka.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka' as const

export const isMassaluovutusQueryPerusopetuksenVuosiluokka = (
  a: any
): a is MassaluovutusQueryPerusopetuksenVuosiluokka =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryPerusopetuksenVuosiluokka'
