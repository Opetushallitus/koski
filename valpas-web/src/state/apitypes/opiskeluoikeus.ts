import * as A from "fp-ts/Array"
import { KoskiOpiskeluoikeudenTila } from "../../state/apitypes/koskiopiskeluoikeudentila"
import { ISODate, Oid } from "../common"
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
  muuOpetusTiedot?: MuuOpetusLaajatTiedot
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus?: PäätasonSuoritus
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

export type LaajatOpintotasonTiedot = OpintotasonTiedot & {
  tarkastelupäivänKoskiTilanAlkamispäivä: ISODate
}

export type PerusopetusLaajatTiedot = LaajatOpintotasonTiedot & {
  vuosiluokkiinSitomatonOpetus: boolean
}

export type PerusopetuksenJälkeinenLaajatTiedot = LaajatOpintotasonTiedot

export type MuuOpetusLaajatTiedot = LaajatOpintotasonTiedot

export type OpiskeluoikeusSuppeatTiedot = {
  oid: Oid
  onHakeutumisValvottava: boolean
  onSuorittamisValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  perusopetusTiedot?: PerusopetusSuppeatTiedot
  perusopetuksenJälkeinenTiedot?: PerusopetuksenJälkeinenSuppeatTiedot
  muuOpetusTiedot?: MuuOpetusSuppeatTiedot
  muuHaku?: boolean
  päätasonSuoritukset: PäätasonSuoritus[]
  tarkasteltavaPäätasonSuoritus?: PäätasonSuoritus
  onTehtyIlmoitus?: boolean
}

export type PerusopetusSuppeatTiedot = OpintotasonTiedot & {
  vuosiluokkiinSitomatonOpetus: boolean
}

export type PerusopetuksenJälkeinenSuppeatTiedot = OpintotasonTiedot

export type MuuOpetusSuppeatTiedot = OpintotasonTiedot

export type Maksuttomuus = {
  alku: ISODate
  loppu?: ISODate
  maksuton: boolean
}

export type OikeuttaMaksuttomuuteenPidennetty = {
  alku: ISODate
  loppu: ISODate
}

export type PäätasonSuoritus = {
  toimipiste: Toimipiste
  ryhmä?: string
  suorituksenTyyppi: Suorituksentyyppi
}

export const isHakeutumisvalvottavaOpiskeluoikeus =
  (organisaatioOid: string | undefined) => (oo: OpiskeluoikeusSuppeatTiedot) =>
    oo.onHakeutumisValvottava && oo.oppilaitos.oid == organisaatioOid

export const isSuorittamisvalvottavaOpiskeluoikeus =
  (organisaatioOid: string | undefined) => (oo: OpiskeluoikeusSuppeatTiedot) =>
    oo.onSuorittamisValvottava &&
    oo.oppilaitos.oid == organisaatioOid &&
    // Redundantti tarkistus bugien varalta. Suorittamisvalvottavien pitäisi
    // kaikkien olla perusopetuksen jälkeisiä opintoja sisältäviä opiskeluoikeuksia:
    oo.perusopetuksenJälkeinenTiedot !== undefined

export const isNuortenPerusopetus = (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.tyyppi.koodiarvo === "perusopetus"

export const isInternationalSchool = (oo: OpiskeluoikeusSuppeatTiedot) =>
  oo.tyyppi.koodiarvo === "internationalschool"

// TODO: TOR-1685 Eurooppalainen koulu

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

export const voimassaolevaTaiTulevaPeruskoulunJälkeinenMuunaOpintonaNäytettäväOpiskeluoikeus =
  (opiskeluoikeus: OpiskeluoikeusSuppeatTiedot): boolean => {
    const tiedot = opiskeluoikeus.perusopetuksenJälkeinenTiedot
    const tila = tiedot?.tarkastelupäivänTila.koodiarvo
    const näytäMuuna = tiedot?.näytäMuunaPerusopetuksenJälkeisenäOpintona

    return (
      (tila === "voimassa" || tila === "voimassatulevaisuudessa") &&
      !!näytäMuuna
    )
  }
