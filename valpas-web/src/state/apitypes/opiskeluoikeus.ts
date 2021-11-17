import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { KoskiOpiskeluoikeudenTila } from "../../state/apitypes/koskiopiskeluoikeudentila"
import { ISODate, Language, Oid } from "../common"
import { Opiskeluoikeudentyyppi } from "./koodistot"
import { OppijaHakutilanteillaSuppeatTiedot } from "./oppija"
import { Oppilaitos, Toimipiste } from "./organisaatiot"
import { Suorituksentyyppi } from "./suorituksentyyppi"
import { ValpasOpiskeluoikeudenTila } from "./valpasopiskeluoikeudentila"

export type OpiskeluoikeusLaajatTiedot = {
  oid: Oid
  onHakeutumisValvottava: boolean
  onSuorittamisValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  perusopetusTiedot?: PerusopetusLaajatTiedot
  perusopetuksenJälkeinenTiedot?: PerusopetuksenJälkeinenLaajatTiedot
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus: PäätasonSuoritus
  onTehtyIlmoitus?: boolean
  maksuttomuus?: Maksuttomuus[]
  oikeuttaMaksuttomuuteenPidennetty?: OikeuttaMaksuttomuuteenPidennetty[]
}

export type OpintotasonTiedot = {
  alkamispäivä?: ISODate
  päättymispäivä?: ISODate
  päättymispäiväMerkittyTulevaisuuteen?: boolean
  tarkastelupäivänTila: ValpasOpiskeluoikeudenTila
  tarkastelupäivänKoskiTila: KoskiOpiskeluoikeudenTila
  valmistunutAiemminTaiLähitulevaisuudessa: boolean
  näytäMuunaPerusopetuksenJälkeisenäOpintona?: boolean
}

export type PerusopetusLaajatTiedot = OpintotasonTiedot & {
  tarkastelupäivänKoskiTilanAlkamispäivä: ISODate
  vuosiluokkiinSitomatonOpetus: boolean
}

export type PerusopetuksenJälkeinenLaajatTiedot = OpintotasonTiedot & {
  tarkastelupäivänKoskiTilanAlkamispäivä: ISODate
}

export type OpiskeluoikeusSuppeatTiedot = {
  oid: Oid
  onHakeutumisValvottava: boolean
  onSuorittamisValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  perusopetusTiedot?: PerusopetusSuppeatTiedot
  perusopetuksenJälkeinenTiedot?: PerusopetuksenJälkeinenSuppeatTiedot
  muuHaku?: boolean
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus?: PäätasonSuoritus
  onTehtyIlmoitus?: boolean
}

export type PerusopetusSuppeatTiedot = OpintotasonTiedot & {
  vuosiluokkiinSitomatonOpetus: boolean
}

export type PerusopetuksenJälkeinenSuppeatTiedot = OpintotasonTiedot

export type Maksuttomuus = {
  alku: ISODate
  loppu?: ISODate
  maksuton: boolean
}

export type OikeuttaMaksuttomuuteenPidennetty = {
  alku: ISODate
  loppu: ISODate
}

type PäätasonSuoritus = {
  toimipiste: Toimipiste
  ryhmä?: string
  suorituksenTyyppi: Suorituksentyyppi
}

const opiskeluoikeusAiempienOpintojenDateOrd = (key: keyof OpintotasonTiedot) =>
  Ord.contramap(
    (o: OpiskeluoikeusLaajatTiedot) =>
      (o.perusopetusTiedot?.[key] ||
        o.perusopetuksenJälkeinenTiedot?.[key] ||
        "0000-0-00") as ISODate
  )(string.Ord)

const opiskeluoikeusMyöhempienOpintojenDateOrd = (
  key: keyof OpintotasonTiedot
) =>
  Ord.contramap(
    (o: OpiskeluoikeusLaajatTiedot) =>
      (o.perusopetuksenJälkeinenTiedot?.[key] ||
        o.perusopetusTiedot?.[key] ||
        "0000-00-00") as ISODate
  )(string.Ord)

const alkamispäiväOrd = opiskeluoikeusAiempienOpintojenDateOrd("alkamispäivä")
const päättymispäiväOrd = opiskeluoikeusMyöhempienOpintojenDateOrd(
  "päättymispäivä"
)
const tyyppiNimiOrd = (lang: Language) =>
  Ord.contramap((o: OpiskeluoikeusLaajatTiedot) => o.tyyppi.nimi?.[lang] || "")(
    string.Ord
  )

export const sortOpiskeluoikeusLaajatTiedot = (lang: Language) =>
  A.sortBy<OpiskeluoikeusLaajatTiedot>([
    Ord.reverse(alkamispäiväOrd),
    Ord.reverse(päättymispäiväOrd),
    tyyppiNimiOrd(lang),
  ])

export const isHakeutumisvalvottavaOpiskeluoikeus = (
  organisaatioOid: string | undefined
) => (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.onHakeutumisValvottava && oo.oppilaitos.oid == organisaatioOid

export const isSuorittamisvalvottavaOpiskeluoikeus = (
  organisaatioOid: string | undefined
) => (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.onSuorittamisValvottava &&
  oo.oppilaitos.oid == organisaatioOid &&
  // Redundantti tarkistus bugien varalta. Suorittamisvalvottavien pitäisi
  // kaikkien olla perusopetuksen jälkeisiä opintoja sisältäviä opiskeluoikeuksia:
  oo.perusopetuksenJälkeinenTiedot !== undefined

export const isNuortenPerusopetus = (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.tyyppi.koodiarvo === "perusopetus"

export const isInternationalSchool = (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.tyyppi.koodiarvo === "internationalschool"

export const isValmistunutInternationalSchoolinPerusopetuksestaAiemminTaiLähitulevaisuudessa = (
  oo: OpiskeluoikeusLaajatTiedot
) =>
  oo.tyyppi.koodiarvo == "internationalschool" &&
  oo.perusopetusTiedot !== undefined &&
  oo.perusopetusTiedot.valmistunutAiemminTaiLähitulevaisuudessa &&
  oo.perusopetusTiedot.päättymispäivä !== undefined

export const hakeutumisvalvottavatOpiskeluoikeudet = (
  organisaatioOid: Oid | undefined,
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[]
) =>
  opiskeluoikeudet.filter(isHakeutumisvalvottavaOpiskeluoikeus(organisaatioOid))

export const suorittamisvalvottaviaOpiskeluoikeuksiaCount = (
  organisaatioOid: Oid | undefined,
  oppijat: OppijaHakutilanteillaSuppeatTiedot[]
): number =>
  A.flatten(
    oppijat.map((oppija: OppijaHakutilanteillaSuppeatTiedot) =>
      suorittamisvalvottavatOpiskeluoikeudet(
        organisaatioOid,
        oppija.oppija.opiskeluoikeudet
      )
    )
  ).length

export const suorittamisvalvottavatOpiskeluoikeudet = (
  organisaatioOid: Oid | undefined,
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[]
) =>
  opiskeluoikeudet.filter(
    isSuorittamisvalvottavaOpiskeluoikeus(organisaatioOid)
  )

export const voimassaolevaTaiTulevaPeruskoulunJälkeinenMuunaOpintonaNäytettäväOpiskeluoikeus = (
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): boolean => {
  const tiedot = opiskeluoikeus.perusopetuksenJälkeinenTiedot
  const tila = tiedot?.tarkastelupäivänTila.koodiarvo
  const näytäMuuna = tiedot?.näytäMuunaPerusopetuksenJälkeisenäOpintona

  return (
    (tila === "voimassa" || tila === "voimassatulevaisuudessa") && !!näytäMuuna
  )
}

export const aiempienOpintojenAlkamispäivä = (
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
): ISODate => {
  const tiedot =
    opiskeluoikeus.perusopetusTiedot ||
    opiskeluoikeus.perusopetuksenJälkeinenTiedot

  return tiedot!.alkamispäivä!
}

export const myöhempienOpintojenPäättymispäivä = (
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
): ISODate | undefined => {
  const tiedot =
    opiskeluoikeus.perusopetuksenJälkeinenTiedot ||
    opiskeluoikeus.perusopetusTiedot

  return tiedot!.päättymispäivä
}

export const myöhempienOpintojenTarkastelupäivänTila = (
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
): ValpasOpiskeluoikeudenTila => {
  const tiedot =
    opiskeluoikeus.perusopetuksenJälkeinenTiedot ||
    opiskeluoikeus.perusopetusTiedot

  return tiedot!.tarkastelupäivänTila
}

export const myöhempienOpintojenTarkastelupäivänKoskiTila = (
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
): KoskiOpiskeluoikeudenTila => {
  const tiedot =
    opiskeluoikeus.perusopetuksenJälkeinenTiedot ||
    opiskeluoikeus.perusopetusTiedot

  return tiedot!.tarkastelupäivänKoskiTila
}

export const myöhempienOpintojenKoskiTilanAlkamispäivä = (
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
): ISODate => {
  const tiedot =
    opiskeluoikeus.perusopetuksenJälkeinenTiedot ||
    opiskeluoikeus.perusopetusTiedot

  return tiedot!.tarkastelupäivänKoskiTilanAlkamispäivä
}

export const isPerusopetuksenJälkeinenOpiskeluoikeus = (
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
): boolean =>
  opiskeluoikeus.perusopetuksenJälkeinenTiedot !== undefined &&
  opiskeluoikeus.tyyppi.koodiarvo !== "esiopetus"
