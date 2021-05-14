import { Kieli, Maa } from "./koodistot"
import {
  KuntailmoituksenTekijäHenkilö,
  KuntailmoitusKunta,
} from "./kuntailmoitus"
import { Organisaatio } from "./organisaatiot"
import { YhteystietojenAlkuperä } from "./yhteystiedot"

export type KuntailmoitusPohjatiedot = {
  tekijäHenkilö?: KuntailmoituksenTekijäHenkilö
  mahdollisetTekijäOrganisaatiot: Organisaatio[]
  oppijat: OppijanPohjatiedot[]
  kunnat: KuntailmoitusKunta[]
  maat: Maa[]
  yhteydenottokielet: Kieli[]
}

export type OppijanPohjatiedot = {
  oppijaOid: string
  mahdollisetTekijäOrganisaatiot: Organisaatio[]
  yhteydenottokieli?: Kieli
  turvakielto: Boolean
  yhteystiedot: PohjatietoYhteystieto[]
  hetu?: string
}

export type PohjatietoYhteystieto = {
  yhteystietojenAlkuperä: YhteystietojenAlkuperä
  yhteystiedot: KuntailmoituksenOppijanYhteystiedot
  kunta?: Organisaatio
}

export type KuntailmoituksenOppijanYhteystiedot = {
  puhelinnumero?: string
  email?: string
  lähiosoite?: string
  postinumero?: string
  postitoimipaikka?: string
  maa?: Maa
}
