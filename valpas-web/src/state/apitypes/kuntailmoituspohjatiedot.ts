import { Oid, OrganisaatioWithOid } from "../common"
import { Kieli, Maa } from "./koodistot"
import { YhteystietojenAlkuperä } from "./yhteystiedot"

export type KuntailmoitusPohjatiedot = {
  tekijäHenkilö?: KuntailmoituksenTekijäHenkilö
  mahdollisetTekijäOrganisaatiot: OrganisaatioWithOid[]
  oppijat: OppijanPohjatiedot[]
  kunnat: OrganisaatioWithOid[]
  maat: Maa[]
  yhteydenottokielet: Kieli[]
}

export type KuntailmoituksenTekijäHenkilö = {
  oid?: Oid
  etunimet?: string
  sukunimi?: string
  kutsumanimi?: string
  email?: string
  puhelinnumero?: string
}

export type OppijanPohjatiedot = {
  oppijaOid: string
  mahdollisetTekijäOrganisaatiot: OrganisaatioWithOid[]
  yhteydenottokieli?: Kieli
  turvakielto: Boolean
  yhteystiedot: PohjatietoYhteystieto[]
  hetu?: string
}

export type PohjatietoYhteystieto = {
  yhteystietojenAlkuperä: YhteystietojenAlkuperä
  yhteystiedot: KuntailmoituksenOppijanYhteystiedot
  kunta?: OrganisaatioWithOid
}

export type KuntailmoituksenOppijanYhteystiedot = {
  puhelinnumero?: string
  email?: string
  lähiosoite?: string
  postinumero?: string
  postitoimipaikka?: string
  maa?: Maa
}
