/**
 * Palauttaa TUVA-koulutuksen perusopetuksen oppijamääräraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti`
 */
export type MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'tuvaPerusopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti = (o: {
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'tuvaPerusopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti => ({
  type: 'tuvaPerusopetuksenOppijamaaratRaportti',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti' as const

export const isMassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti = (
  a: any
): a is MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti'
