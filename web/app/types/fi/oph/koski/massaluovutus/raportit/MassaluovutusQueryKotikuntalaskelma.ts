/**
 * Palauttaa perusopetusikäisten oppilaiden lukumäärät opetuksen järjestäjittäin ja oppilaan kotikunnittain valittuna päivänä. Kysely vaatii valitussa organisaatiossa oikeudet sekä perusopetuksen että esiopetuksen opiskeluoikeuksiin.
 *
 * @see `fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKotikuntalaskelma`
 */
export type MassaluovutusQueryKotikuntalaskelma = {
  $class: 'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKotikuntalaskelma'
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type: 'kotikuntalaskelma'
  password?: string
  organisaatioOid?: string
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}

export const MassaluovutusQueryKotikuntalaskelma = (o: {
  paiva: string
  language?: 'fi' | 'sv' | 'en'
  type?: 'kotikuntalaskelma'
  password?: string
  organisaatioOid?: string
  format?: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
}): MassaluovutusQueryKotikuntalaskelma => ({
  type: 'kotikuntalaskelma',
  $class:
    'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKotikuntalaskelma',
  format: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  ...o
})

MassaluovutusQueryKotikuntalaskelma.className =
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKotikuntalaskelma' as const

export const isMassaluovutusQueryKotikuntalaskelma = (
  a: any
): a is MassaluovutusQueryKotikuntalaskelma =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryKotikuntalaskelma'
