/**
 * Käytetään ilmaisemaan sitä että aiemmin lähdejärjestelmästä siirretty opiskeluoikeus on muutettu KOSKI-käyttöliittymällä muokattavaksi opiskeluoikeudeksi.
 *
 * @see `fi.oph.koski.schema.LähdejärjestelmäkytkennänPurkaminen`
 */
export type LähdejärjestelmäkytkennänPurkaminen = {
  $class: 'fi.oph.koski.schema.LähdejärjestelmäkytkennänPurkaminen'
  purettu: string
}

export const LähdejärjestelmäkytkennänPurkaminen = (o: {
  purettu: string
}): LähdejärjestelmäkytkennänPurkaminen => ({
  $class: 'fi.oph.koski.schema.LähdejärjestelmäkytkennänPurkaminen',
  ...o
})

LähdejärjestelmäkytkennänPurkaminen.className =
  'fi.oph.koski.schema.LähdejärjestelmäkytkennänPurkaminen' as const

export const isLähdejärjestelmäkytkennänPurkaminen = (
  a: any
): a is LähdejärjestelmäkytkennänPurkaminen =>
  a?.$class === 'fi.oph.koski.schema.LähdejärjestelmäkytkennänPurkaminen'
