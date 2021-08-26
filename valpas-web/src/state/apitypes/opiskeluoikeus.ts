import * as A from "fp-ts/Array"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { KoskiOpiskeluoikeudenTila } from "../../state/apitypes/koskiopiskeluoikeudentila"
import { ISODate, Language, Oid } from "../common"
import { Opiskeluoikeudentyyppi, Suorituksentyyppi } from "./koodistot"
import { OppijaHakutilanteillaSuppeatTiedot } from "./oppija"
import { Oppilaitos, Toimipiste } from "./organisaatiot"
import { ValpasOpiskeluoikeudenTila } from "./valpasopiskeluoikeudentila"

export type OpiskeluoikeusLaajatTiedot = {
  oid: Oid
  onHakeutumisValvottava: boolean
  onSuorittamisValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  alkamispäivä: ISODate
  päättymispäivä?: ISODate
  päättymispäiväMerkittyTulevaisuuteen?: boolean
  tarkastelupäivänTila: ValpasOpiskeluoikeudenTila
  tarkastelupäivänKoskiTila: KoskiOpiskeluoikeudenTila
  tarkastelupäivänAikajaksonAlku: ISODate
  näytettäväPerusopetuksenSuoritus: boolean
  vuosiluokkiinSitomatonOpetus: boolean
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus: PäätasonSuoritus
}

export type OpiskeluoikeusSuppeatTiedot = {
  oid: Oid
  onHakeutumisValvottava: boolean
  onSuorittamisValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  tarkastelupäivänTila: ValpasOpiskeluoikeudenTila
  tarkastelupäivänKoskiTila: KoskiOpiskeluoikeudenTila
  alkamispäivä: ISODate
  päättymispäivä?: ISODate
  päättymispäiväMerkittyTulevaisuuteen?: boolean
  näytettäväPerusopetuksenSuoritus: boolean
  vuosiluokkiinSitomatonOpetus: boolean
  muuHaku?: boolean
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus?: PäätasonSuoritus
}

type PäätasonSuoritus = {
  toimipiste: Toimipiste
  ryhmä?: string
  suorituksenTyyppi: Suorituksentyyppi
}

const opiskeluoikeusDateOrd = (key: keyof OpiskeluoikeusLaajatTiedot) =>
  Ord.contramap(
    (o: OpiskeluoikeusLaajatTiedot) => (o[key] as ISODate) || "0000-00-00"
  )(string.Ord)

const alkamispäiväOrd = opiskeluoikeusDateOrd("alkamispäivä")
const päättymispäiväOrd = opiskeluoikeusDateOrd("päättymispäivä")
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
  oo.onSuorittamisValvottava && oo.oppilaitos.oid == organisaatioOid

export const isNuortenPerusopetus = (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.tyyppi.koodiarvo === "perusopetus"

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

export const voimassaolevaTaiTulevaPeruskoulunJälkeinenOpiskeluoikeus = (
  opiskeluoikeus: OpiskeluoikeusSuppeatTiedot
): boolean => {
  const tila = opiskeluoikeus.tarkastelupäivänTila.koodiarvo
  return (
    !isNuortenPerusopetus(opiskeluoikeus) &&
    (tila === "voimassa" || tila === "voimassatulevaisuudessa")
  )
}
