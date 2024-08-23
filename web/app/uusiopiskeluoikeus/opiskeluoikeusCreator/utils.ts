import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Maksuttomuus } from '../../types/fi/oph/koski/schema/Maksuttomuus'
import { MaksuttomuusTieto } from '../../types/fi/oph/koski/schema/MaksuttomuusTieto'
import { Oppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { Toimipiste } from '../../types/fi/oph/koski/schema/Toimipiste'

export const isKoodiarvo = <A extends string, U extends string>(
  koodi: Koodistokoodiviite<U>,
  koodiarvo: A
): koodi is Koodistokoodiviite<U, A> => koodi.koodiarvo === koodiarvo

export const toToimipiste = (org: OrganisaatioHierarkia): Toimipiste =>
  Toimipiste({
    oid: org.oid,
    nimi: org.nimi,
    kotipaikka: org.kotipaikka as Koodistokoodiviite<'kunta'>
  })

export const toOppilaitos = (org: OrganisaatioHierarkia): Oppilaitos =>
  Oppilaitos({
    oid: org.oid
  })

export const maksuttomuuslisätiedot = <T extends MaksuttomuusTieto>(
  alku: string,
  maksuton: boolean | null,
  lisätietoCtor: (p: { maksuttomuus: Maksuttomuus[] }) => T
): T | undefined =>
  maksuton === null
    ? undefined
    : lisätietoCtor({
        maksuttomuus: [Maksuttomuus({ alku, maksuton })]
      })
