import { Oid, OrganisaatioWithOid } from "../common"
import { Kieli, Kunta, Maa } from "./koodistot"
import { YhteystietojenAlkuperä } from "./yhteystiedot"

export type KuntailmoitusPohjatiedot = {
  tekijäHenkilö?: KuntailmoituksenTekijäHenkilö
  mahdollisetTekijäorganisaatiot: OrganisaatioWithOid[]
  oppijat: OppijanPohjatiedot[]
  kunnat: KuntailmoitusKunta[]
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

export type KuntailmoitusKunta = OrganisaatioWithOid & {
  kotipaikka?: Kunta
}

export type OppijanPohjatiedot = {
  oppijaOid: string
  mahdollisetTekijäorganisaatiot: TekijäorganisaationPohjatiedot[]
  yhteydenottokieli?: Kieli
  turvakielto: Boolean
  yhteystiedot: PohjatietoYhteystieto[]
  hetu?: string
}

export type TekijäorganisaationPohjatiedot = {
  organisaatio: OrganisaatioWithOid
  hakenutMuualle?: Boolean
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
