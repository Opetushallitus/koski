/**
 * Palauttaa lukio/DIA/IB/International School/ESH opiskelijamäärät-raportin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat`
 */
export type MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'lukioDiaIbInternationalESHOpiskelijamaarat'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat =
  (o: {
    paiva: string
    language?: 'fi' | 'sv' | 'en'
    type?: 'lukioDiaIbInternationalESHOpiskelijamaarat'
    password?: string
    organisaatioOid?: string
    format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }): MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat => ({
    type: 'lukioDiaIbInternationalESHOpiskelijamaarat',
    $class:
      'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat',
    format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    ...o
  })

MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat' as const

export const isMassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat = (
  a: any
): a is MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat'
