import { LocalizedString, Oid } from "../common"

export type Organisaatio = {
  oid: Oid
  nimi: LocalizedString
}

export type Oppilaitos = Organisaatio
export type Toimipiste = Organisaatio

export const organisaatioWithOid = (oid: Oid): Organisaatio => ({
  oid,
  nimi: {},
})

export const trimOrganisaatio = <T extends Organisaatio>(
  org: T
): Organisaatio => ({
  oid: org.oid,
  nimi: org.nimi,
})
