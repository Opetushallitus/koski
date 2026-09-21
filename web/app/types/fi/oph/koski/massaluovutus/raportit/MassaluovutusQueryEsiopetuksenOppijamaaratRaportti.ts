/**
 * Palauttaa esiopetuksen oppijamääräraportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti`
 */
export type MassaluovutusQueryEsiopetuksenOppijamaaratRaportti = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'esiopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryEsiopetuksenOppijamaaratRaportti = (o: {
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'esiopetuksenOppijamaaratRaportti'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryEsiopetuksenOppijamaaratRaportti => ({
  type: 'esiopetuksenOppijamaaratRaportti',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryEsiopetuksenOppijamaaratRaportti.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti' as const

export const isMassaluovutusQueryEsiopetuksenOppijamaaratRaportti = (
  a: any
): a is MassaluovutusQueryEsiopetuksenOppijamaaratRaportti =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryEsiopetuksenOppijamaaratRaportti'
