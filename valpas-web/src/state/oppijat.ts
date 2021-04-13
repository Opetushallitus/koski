import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import { Language } from "../i18n/i18n"
import {
  KoodistoKoodiviite,
  Opiskeluoikeudentyyppi,
  ValpasOpiskeluoikeudenTila,
} from "./koodistot"
import { ISODate, ISODateTime, LocalizedString, Oid } from "./types"

export type OppijaHakutilanteillaLaajatTiedot = {
  oppija: OppijaLaajatTiedot
  hakutilanteet: HakuLaajatTiedot[]
  hakutilanneError?: string
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
}

export type OppijaHakutilanteillaSuppeatTiedot = {
  oppija: OppijaSuppeatTiedot
  hakutilanteet: HakuSuppeatTiedot[]
  hakutilanneError?: string
}

export type OppijaLaajatTiedot = {
  henkilö: HenkilöLaajatTiedot
  opiskeluoikeudet: OpiskeluoikeusLaajatTiedot[]
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti?: ISODate
  oikeutetutOppilaitokset: Oid[]
}

export type OppijaSuppeatTiedot = {
  henkilö: HenkilöSuppeatTiedot
  opiskeluoikeudet: OpiskeluoikeusSuppeatTiedot[]
  opiskelee: boolean
  oppivelvollisuusVoimassaAsti?: ISODate
}

export type HenkilöLaajatTiedot = {
  oid: Oid
  hetu?: string
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
  turvakielto: boolean
}

export type HenkilöSuppeatTiedot = {
  oid: Oid
  syntymäaika?: ISODate
  etunimet: string
  sukunimi: string
}

export type Yhteystiedot<T extends YhteystietojenAlkuperä> = {
  alkuperä: T
  yhteystietoryhmänNimi: LocalizedString
  henkilönimi?: string
  sähköposti?: string
  puhelinnumero?: string
  matkapuhelinnumero?: string
  lähiosoite?: string
  kunta?: string
  postinumero?: string
  maa?: string
}

export type YhteystietojenAlkuperä = AlkuperäHakemukselta | AlkuperäRekisteristä

export type AlkuperäHakemukselta = {
  hakuNimi: LocalizedString
  haunAlkamispaivämäärä: ISODateTime
  hakuOid: Oid
  hakemusOid: Oid
}

export type AlkuperäRekisteristä = {
  alkuperä: KoodistoKoodiviite<"yhteystietojenalkupera">
  tyyppi: KoodistoKoodiviite<"yhteystietotyypit">
}

export type Oppilaitos = {
  oid: Oid
  nimi: LocalizedString
}

export type Toimipiste = {
  oid: Oid
  nimi: LocalizedString
}

export type HakuLaajatTiedot = {
  hakuOid: Oid
  hakuNimi?: LocalizedString
  hakemusOid: Oid
  hakemusUrl: string
  aktiivinenHaku: boolean
  muokattu: ISODateTime
  hakutoiveet: Hakutoive[]
  osoite: string
  puhelinnumero: string
  sähköposti: string
  huoltajanNimi?: string
  huoltajanPuhelinnumero?: string
  huoltajanSähköposti?: string
}

export type HakuSuppeatTiedot = Pick<
  HakuLaajatTiedot,
  "hakuOid" | "hakuNimi" | "hakemusOid" | "hakemusUrl" | "aktiivinenHaku"
> & {
  hakutoiveet: SuppeaHakutoive[]
}

export type Hakutoive = {
  hakutoivenumero?: number
  hakukohdeNimi?: LocalizedString
  organisaatioNimi?: LocalizedString
  koulutusNimi?: LocalizedString
  pisteet?: number
  alinValintaPistemaara?: number
  valintatila?: KoodistoKoodiviite<
    "valpashaunvalintatila",
    HakutoiveValintatilakoodiarvo
  >
}

export type SuppeaHakutoive = Pick<
  Hakutoive,
  "organisaatioNimi" | "hakutoivenumero" | "valintatila"
>

export type HakutoiveValintatilakoodiarvo =
  | "hyvaksytty"
  | "hylatty"
  | "varasijalla"
  | "peruuntunut"
  | "peruttu"
  | "peruutettu"

export type OpiskeluoikeusLaajatTiedot = {
  oid: Oid
  onValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  toimipiste?: Toimipiste
  alkamispäivä?: ISODate
  päättymispäivä?: ISODate
  ryhmä?: string
  tarkastelupäivänTila: ValpasOpiskeluoikeudenTila
}

export type OpiskeluoikeusSuppeatTiedot = {
  oid: Oid
  onValvottava: boolean
  tyyppi: Opiskeluoikeudentyyppi
  oppilaitos: Oppilaitos
  toimipiste?: Toimipiste
  ryhmä?: string
  tarkastelupäivänTila: ValpasOpiskeluoikeudenTila
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

export const OpiskeluoikeusLaajatTiedot = {
  sort: (lang: Language) =>
    A.sortBy<OpiskeluoikeusLaajatTiedot>([
      Ord.reverse(alkamispäiväOrd),
      Ord.reverse(päättymispäiväOrd),
      tyyppiNimiOrd(lang),
    ]),
}

const muokkausOrd = Ord.contramap((haku: HakuLaajatTiedot) => haku.muokattu)(
  string.Ord
)

export const Haku = {
  latest: (haut: HakuLaajatTiedot[]) =>
    pipe(haut, A.sortBy([muokkausOrd]), A.head, O.toNullable),
}

export const Hakutoive = {
  isHyväksytty: (toive: Hakutoive) =>
    toive.valintatila?.koodiarvo === "hyvaksytty",
  isVarasijalla: (toive: Hakutoive) =>
    toive.valintatila?.koodiarvo === "varasijalla",
  isEiPaikkaa: (toive: Hakutoive) =>
    toive.valintatila?.koodiarvo === undefined
      ? false
      : !["hyvaksytty", "varasijalla"].includes(toive.valintatila.koodiarvo),
}

export const Yhteystiedot = {
  isIlmoitettu: (
    yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
  ): yhteystiedot is Yhteystiedot<AlkuperäHakemukselta> => {
    const a = yhteystiedot.alkuperä as AlkuperäHakemukselta
    return (
      a.hakuNimi !== undefined &&
      a.haunAlkamispaivämäärä !== undefined &&
      a.hakuOid !== undefined &&
      a.hakemusOid !== undefined
    )
  },

  isVirallinen: (
    yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
  ): yhteystiedot is Yhteystiedot<AlkuperäRekisteristä> => {
    const a = yhteystiedot.alkuperä as AlkuperäRekisteristä
    return a.alkuperä !== undefined && a.tyyppi !== undefined
  },
}

const onValvottavaOpiskeluoikeus = (organisaatioOid: string | undefined) => (
  oo: OpiskeluoikeusSuppeatTiedot
) => oo.onValvottava && oo.oppilaitos.oid == organisaatioOid

export const valvottavatOpiskeluoikeudet = (
  organisaatioOid: string | undefined,
  opiskeluoikeudet: Array<OpiskeluoikeusSuppeatTiedot>
) => opiskeluoikeudet.filter(onValvottavaOpiskeluoikeus(organisaatioOid))
