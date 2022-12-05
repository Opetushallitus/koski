import bem from "bem-ts"
import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import React, { useMemo, useState } from "react"
import {
  IconSection,
  IconSectionHeading,
} from "../../components/containers/IconSection"
import { Modal } from "../../components/containers/Modal"
import {
  OpiskeluhistoriaTapahtumaIcon,
  OpiskeluIcon,
} from "../../components/icons/Icon"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { getLocalizedMaybe, T, t, useLanguage } from "../../i18n/i18n"
import { HenkilöLaajatTiedot } from "../../state/apitypes/henkilo"
import { KoodistoKoodiviite } from "../../state/apitypes/koodistot"
import { KoskiOpiskeluoikeudenTila } from "../../state/apitypes/koskiopiskeluoikeudentila"
import { kuntaKotipaikka } from "../../state/apitypes/kuntailmoitus"
import {
  LaajatOpintotasonTiedot,
  OpintotasonTiedot,
} from "../../state/apitypes/opiskeluoikeus"
import { OppivelvollisuudenKeskeytys } from "../../state/apitypes/oppivelvollisuudenkeskeytys"
import { organisaatioNimi } from "../../state/apitypes/organisaatiot"
import { suorituksenTyyppiToKoulutustyyppi } from "../../state/apitypes/suorituksentyyppi"
import { ValpasOpiskeluoikeudenTila } from "../../state/apitypes/valpasopiskeluoikeudentila"
import { ISODate, Language } from "../../state/common"
import { formatDate, formatDateRange, parseYear } from "../../utils/date"
import { withoutDefaultAction } from "../../utils/events"
import { pick } from "../../utils/objects"
import { filterFalsy } from "../../utils/types"
import { OppijaKuntailmoitus } from "./OppijaKuntailmoitus"
import "./OppijanOpiskeluhistoria.less"
import {
  MinimiOpiskeluoikeus,
  MinimiOppijaKuntailmoitus,
} from "./typeIntersections"

const b = bem("oppijanopiskeluhistoria")

export type OppijanOpiskeluhistoriaProps = {
  henkilö: HenkilöLaajatTiedot
  opiskeluoikeudet: MinimiOpiskeluoikeus[]
  kuntailmoitukset: MinimiOppijaKuntailmoitus[]
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
}

type OpiskeluhistoriaItem = {
  order: string
  child: React.ReactNode
}

const opiskeluhistoriaItemOrd = Ord.reverse(
  Ord.contramap((item: OpiskeluhistoriaItem) => item.order)(string.Ord)
)

const orderString = (
  priority: string,
  time: string | undefined,
  index: number
) => `${time || "0000-00-00"}-${priority}-${(9999999 - index).toString()}`

export const OppijanOpiskeluhistoria = (
  props: OppijanOpiskeluhistoriaProps
) => {
  const language = useLanguage()

  const items = useMemo(() => {
    // Järjestele listat ensin niiden omien kriteerien mukaan
    const opiskeluoikeudet = sortOpiskeluoikeudet(language)(
      props.opiskeluoikeudet
    )

    const ilmoitukset = sortKuntailmoitukset(props.kuntailmoitukset)

    const keskeytykset = props.oppivelvollisuudenKeskeytykset

    // Yhdistä erilaatuiset asiat yhtenäiseksi listaksi
    return pipe(
      [
        opiskeluoikeudet.length === 0 && {
          order: orderString("A", new Date().toISOString(), 0),
          child: <EiOpiskeluhistoriaOpintoja key={`i-${0}-no-history`} />,
        },
        ...ilmoitukset.map((ilmoitus, index) => ({
          order: orderString("A", ilmoitus.aikaleima, index),
          child: (
            <OpiskeluhistoriaIlmoitus
              key={`i-${index}`}
              kuntailmoitus={ilmoitus}
            />
          ),
        })),
        ...opiskeluoikeudet.map((oo, index) => ({
          order: orderString("B", aiempienOpintojenAlkamispäivä(oo), index),
          child: (
            <OpiskeluhistoriaOpinto key={`oo-${index}`} opiskeluoikeus={oo} />
          ),
        })),
        ...keskeytykset.map((ovk, index) => ({
          order: orderString("C", ovk.alku, index),
          child: (
            <OpiskeluhistoriaOppivelvollisuudenKeskeytys
              key={`ovk-${index}`}
              keskeytys={ovk}
            />
          ),
        })),
      ].filter(filterFalsy),
      A.sort(opiskeluhistoriaItemOrd),
      pick("child")
    )
  }, [
    language,
    props.kuntailmoitukset,
    props.opiskeluoikeudet,
    props.oppivelvollisuudenKeskeytykset,
  ])

  return items.length > 0 ? (
    <>{items}</>
  ) : (
    <NoDataMessage>
      <T id="oppija__ei_opiskeluhistoriaa" />
    </NoDataMessage>
  )
}

const EiOpiskeluhistoriaOpintoja = () => (
  <IconSection icon={<OpiskeluIcon color="gray" />}>
    <NoDataMessage data-testid="ei-opiskeluoikeushistoria-opintoja-text">
      <T id="oppija__ei_opiskeluhistoriaa" />
    </NoDataMessage>
  </IconSection>
)

type OpiskeluhistoriaOpintoProps = {
  opiskeluoikeus: MinimiOpiskeluoikeus
}

const OpiskeluhistoriaOpinto = ({
  opiskeluoikeus,
}: OpiskeluhistoriaOpintoProps) => {
  const nimi = opiskeluoikeus.tarkasteltavaPäätasonSuoritus
    ? suorituksenTyyppiToKoulutustyyppi(
        opiskeluoikeus.tarkasteltavaPäätasonSuoritus.suorituksenTyyppi
      )
    : t("Opiskeluoikeus")

  const alkamispäivä = aiempienOpintojenAlkamispäivä(opiskeluoikeus)
  const päättymispäivä = myöhempienOpintojenPäättymispäivä(opiskeluoikeus)
  const tarkastelupäivänTila =
    myöhempienOpintojenTarkastelupäivänTila(opiskeluoikeus)

  const range = yearRangeString(alkamispäivä, päättymispäivä)

  return (
    <IconSection icon={<OpiskeluIcon color="gray" />}>
      <IconSectionHeading>
        {nimi} {range}
      </IconSectionHeading>
      <InfoTable size="tighter">
        {tarkastelupäivänTila && (
          <InfoTableRow
            label={t("oppija__tila")}
            value={tilaString(opiskeluoikeus)}
          />
        )}
        {isPerusopetuksenJälkeinenOpiskeluoikeus(opiskeluoikeus) && (
          <InfoTableRow
            label={t("oppija__maksuttomuus")}
            value={maksuttomuusValue(opiskeluoikeus)}
          />
        )}
        {opiskeluoikeus.tarkasteltavaPäätasonSuoritus && (
          <InfoTableRow
            label={t("oppija__toimipiste")}
            value={organisaatioNimi(
              opiskeluoikeus.tarkasteltavaPäätasonSuoritus.toimipiste
            )}
          />
        )}
        {opiskeluoikeus.tarkasteltavaPäätasonSuoritus?.ryhmä && (
          <InfoTableRow
            label={t("oppija__ryhma")}
            value={opiskeluoikeus.tarkasteltavaPäätasonSuoritus.ryhmä}
          />
        )}
        {opiskeluoikeus.perusopetusTiedot?.vuosiluokkiinSitomatonOpetus && (
          <InfoTableRow
            label={t("oppija__muuta")}
            value={t("oppija__vuosiluokkiin_sitomaton_opetus")}
          />
        )}
        <InfoTableRow
          label={t("oppija__opiskeluoikeuden_alkamispäivä")}
          value={formatDate(aiempienOpintojenAlkamispäivä(opiskeluoikeus))}
        />
        {/* TODO: TOR-1685 Eurooppalainen koulu */}
        {isValmistunutInternationalSchoolinPerusopetuksestaAiemminTaiLähitulevaisuudessa(
          opiskeluoikeus
        ) && (
          <InfoTableRow
            label={t(
              "oppija__international_school_perusopetuksen_vahvistuspäivä"
            )}
            value={formatDate(
              opiskeluoikeus.perusopetusTiedot!.päättymispäivä!
            )}
          />
        )}
        {päättymispäivä && (
          <InfoTableRow
            label={t("oppija__opiskeluoikeuden_päättymispäivä")}
            value={formatDate(päättymispäivä)}
          />
        )}
      </InfoTable>
    </IconSection>
  )
}

type OpiskeluhistoriaIlmoitusProps = {
  kuntailmoitus: MinimiOppijaKuntailmoitus
}

const OpiskeluhistoriaIlmoitus = ({
  kuntailmoitus,
}: OpiskeluhistoriaIlmoitusProps) => (
  <IconSection icon={<OpiskeluhistoriaTapahtumaIcon color="gray" />}>
    <IconSectionHeading>
      <T id="oppija__ilmoitushistoria_otsikko" />
    </IconSectionHeading>
    <InfoTable size="tighter">
      {kuntailmoitus.aikaleima && (
        <InfoTableRow
          label={t("oppija__ilmoitushistoria_päivämäärä")}
          value={formatDate(kuntailmoitus.aikaleima)}
        />
      )}
      {kuntailmoitus.tekijä && (
        <InfoTableRow
          label={t("oppija__ilmoitushistoria_ilmoittaja")}
          value={organisaatioNimi(kuntailmoitus.tekijä.organisaatio)}
        />
      )}
      {kuntailmoitus.kunta && (
        <InfoTableRow
          label={t("oppija__ilmoitushistoria_kohde")}
          value={kuntaKotipaikka(kuntailmoitus.kunta)}
        />
      )}
      <InfoTableRow value={<IlmoitusLink kuntailmoitus={kuntailmoitus} />} />
    </InfoTable>
  </IconSection>
)

type OpiskeluhistoriaOppivelvollisuudenKeskeytysProps = {
  keskeytys: OppivelvollisuudenKeskeytys
}

const OpiskeluhistoriaOppivelvollisuudenKeskeytys = (
  props: OpiskeluhistoriaOppivelvollisuudenKeskeytysProps
) => (
  <IconSection icon={<OpiskeluhistoriaTapahtumaIcon color="gray" />}>
    <IconSectionHeading>
      {t("oppija__historia_keskeytys_otsikko")}
    </IconSectionHeading>
    <div>
      {props.keskeytys.loppu
        ? t("oppija__oppivelvollisuus_keskeytetty_value", {
            alkuPvm: formatDate(props.keskeytys.alku),
            loppuPvm: formatDate(props.keskeytys.loppu),
          })
        : t("oppija__oppivelvollisuus_keskeytetty_toistaiseksi_value", {
            alkuPvm: formatDate(props.keskeytys.alku),
          })}
    </div>
  </IconSection>
)

const IlmoitusLink = (props: OpiskeluhistoriaIlmoitusProps) => {
  const [modalVisible, setModalVisibility] = useState(false)

  return (
    <>
      <a
        href="#"
        className={b("lisatiedot")}
        onClick={withoutDefaultAction(() => setModalVisibility(true))}
      >
        <T id="oppija__ilmoitushistoria_lisätiedot" />
      </a>
      {modalVisible && (
        <Modal onClose={() => setModalVisibility(false)} closeOnBackgroundClick>
          <OppijaKuntailmoitus kuntailmoitus={props.kuntailmoitus} />
        </Modal>
      )}
    </>
  )
}

const koodistonimi = (k: KoodistoKoodiviite<string, string>): string =>
  getLocalizedMaybe(k.nimi) || k.koodiarvo

const yearRangeString = (a?: ISODate, b?: ISODate): string =>
  a || b ? [yearString(a), "–", yearString(b)].filter((s) => !!s).join(" ") : ""

const yearString = (date?: ISODate): string | undefined =>
  date && parseYear(date).toString()

const tilaString = (opiskeluoikeus: MinimiOpiskeluoikeus): string => {
  const valpasTila = myöhempienOpintojenTarkastelupäivänTila(opiskeluoikeus)
  const koskiTila = myöhempienOpintojenTarkastelupäivänKoskiTila(opiskeluoikeus)

  if (valpasTila.koodiarvo === "voimassatulevaisuudessa") {
    const alkamispäivä = formatDate(
      aiempienOpintojenAlkamispäivä(opiskeluoikeus)
    )
    return t("oppija__tila_voimassatulevaisuudessa", {
      päivämäärä: alkamispäivä,
    })
  }

  switch (koskiTila.koodiarvo) {
    case "valiaikaisestikeskeytynyt":
      const tarkastelujaksonAlku = formatDate(
        myöhempienOpintojenKoskiTilanAlkamispäivä(opiskeluoikeus)
      )
      return t("oppija__tila_valiaikaisesti_keskeytynyt", {
        päivämäärä: tarkastelujaksonAlku,
      })
    default:
      return koodistonimi(koskiTila)
  }
}

const maksuttomuusValue = (opiskeluoikeus: MinimiOpiskeluoikeus) => {
  const maksuttomuusRivit = (opiskeluoikeus.maksuttomuus || []).map(
    ({ maksuton, alku, loppu }) =>
      t(
        maksuton
          ? "oppija__maksuttomuus_maksuttomuusjakso_maksuton"
          : "oppija__maksuttomuus_maksuttomuusjakso_ei_maksuton",
        {
          aikaväli: formatDateRange(alku, loppu),
        }
      )
  )

  const maksuttomuudenPidennysRivit = (
    opiskeluoikeus.oikeuttaMaksuttomuuteenPidennetty || []
  ).map(({ alku, loppu }) =>
    t("oppija__oikeutta_maksuttomuuteen_pidennetty", {
      aikaväli: formatDateRange(alku, loppu),
    })
  )

  const rivit = [...maksuttomuudenPidennysRivit, ...maksuttomuusRivit]

  return A.isNonEmpty(rivit) ? (
    <ul>
      {rivit.map((rivi, index) => (
        <li key={index}>{rivi}</li>
      ))}
    </ul>
  ) : (
    t("oppija__maksuttomuus_ei_siirretty")
  )
}

const opiskeluoikeusAiempienOpintojenDateOrd = (key: keyof OpintotasonTiedot) =>
  Ord.contramap(
    (o: MinimiOpiskeluoikeus) =>
      (o.muuOpetusTiedot?.[key] ||
        o.perusopetusTiedot?.[key] ||
        o.perusopetuksenJälkeinenTiedot?.[key] ||
        "0000-0-00") as ISODate
  )(string.Ord)

const opiskeluoikeusMyöhempienOpintojenDateOrd = (
  key: keyof OpintotasonTiedot
) =>
  Ord.contramap(
    (o: MinimiOpiskeluoikeus) =>
      (o.perusopetuksenJälkeinenTiedot?.[key] ||
        o.perusopetusTiedot?.[key] ||
        o.muuOpetusTiedot?.[key] ||
        "0000-00-00") as ISODate
  )(string.Ord)

const tyyppiNimiOrd = (lang: Language) =>
  Ord.contramap((o: MinimiOpiskeluoikeus) => o.tyyppi?.nimi?.[lang] || "")(
    string.Ord
  )

const alkamispäiväOrd = opiskeluoikeusAiempienOpintojenDateOrd("alkamispäivä")
const päättymispäiväOrd =
  opiskeluoikeusMyöhempienOpintojenDateOrd("päättymispäivä")

const sortOpiskeluoikeudet = (lang: Language) =>
  A.sortBy<MinimiOpiskeluoikeus>([
    Ord.reverse(alkamispäiväOrd),
    Ord.reverse(päättymispäiväOrd),
    tyyppiNimiOrd(lang),
  ])

const aiempiOpinto = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): LaajatOpintotasonTiedot =>
  opiskeluoikeus.muuOpetusTiedot ||
  opiskeluoikeus.perusopetusTiedot ||
  opiskeluoikeus.perusopetuksenJälkeinenTiedot!

const myöhempiOpinto = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): LaajatOpintotasonTiedot =>
  opiskeluoikeus.perusopetuksenJälkeinenTiedot ||
  opiskeluoikeus.perusopetusTiedot ||
  opiskeluoikeus.muuOpetusTiedot!

const aiempienOpintojenAlkamispäivä = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): ISODate => aiempiOpinto(opiskeluoikeus).alkamispäivä!

const myöhempienOpintojenPäättymispäivä = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): ISODate | undefined => myöhempiOpinto(opiskeluoikeus).päättymispäivä

const myöhempienOpintojenTarkastelupäivänTila = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): ValpasOpiskeluoikeudenTila =>
  myöhempiOpinto(opiskeluoikeus).tarkastelupäivänTila

const myöhempienOpintojenTarkastelupäivänKoskiTila = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): KoskiOpiskeluoikeudenTila =>
  myöhempiOpinto(opiskeluoikeus).tarkastelupäivänKoskiTila

const myöhempienOpintojenKoskiTilanAlkamispäivä = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): ISODate =>
  myöhempiOpinto(opiskeluoikeus).tarkastelupäivänKoskiTilanAlkamispäivä

const isValmistunutInternationalSchoolinPerusopetuksestaAiemminTaiLähitulevaisuudessa =
  (oo: MinimiOpiskeluoikeus) =>
    oo.tyyppi?.koodiarvo === "internationalschool" &&
    oo.perusopetusTiedot !== undefined &&
    oo.perusopetusTiedot.valmistunutAiemminTaiLähitulevaisuudessa &&
    oo.perusopetusTiedot.päättymispäivä !== undefined

{
  /*TODO: TOR-1685 Eurooppalainen koulu*/
}

const isPerusopetuksenJälkeinenOpiskeluoikeus = (
  opiskeluoikeus: MinimiOpiskeluoikeus
): boolean => opiskeluoikeus.perusopetuksenJälkeinenTiedot !== undefined

const kuntailmoitusAikaleimaOrd = Ord.contramap(
  (kuntailmoitus: MinimiOppijaKuntailmoitus) =>
    kuntailmoitus.aikaleima || "0000-00-00"
)(string.Ord)

const sortKuntailmoitukset = A.sort(Ord.reverse(kuntailmoitusAikaleimaOrd))
