/**
 * Palauttaa lukion 2019 opintopistekertymät-raportin (LOPS 2021).
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat`
 */
export type MassaluovutusQueryLukio2019Opintopistekertymat = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat'
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type: 'lukio2019Opintopistekertymat'
  alku: string
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryLukio2019Opintopistekertymat = (o: {
  loppu: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'lukio2019Opintopistekertymat'
  alku: string
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryLukio2019Opintopistekertymat => ({
  type: 'lukio2019Opintopistekertymat',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryLukio2019Opintopistekertymat.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat' as const

export const isMassaluovutusQueryLukio2019Opintopistekertymat = (
  a: any
): a is MassaluovutusQueryLukio2019Opintopistekertymat =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukio2019Opintopistekertymat'
