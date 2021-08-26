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
import { KoodistoKoodiviite } from "../../state/apitypes/koodistot"
import {
  KuntailmoitusLaajatTiedotLisätiedoilla,
  sortKuntailmoitusLaajatTiedotLisätiedoilla,
} from "../../state/apitypes/kuntailmoitus"
import {
  OpiskeluoikeusLaajatTiedot,
  sortOpiskeluoikeusLaajatTiedot,
} from "../../state/apitypes/opiskeluoikeus"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import { OppivelvollisuudenKeskeytys } from "../../state/apitypes/oppivelvollisuudenkeskeytys"
import { organisaatioNimi } from "../../state/apitypes/organisaatiot"
import { ISODate } from "../../state/common"
import { formatDate, parseYear } from "../../utils/date"
import { pick } from "../../utils/objects"
import { OppijaKuntailmoitus } from "./OppijaKuntailmoitus"
import "./OppijanOpiskeluhistoria.less"

const b = bem("oppijanopiskeluhistoria")

export type OppijanOpiskeluhistoriaProps = {
  oppija: OppijaHakutilanteillaLaajatTiedot
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
    const opiskeluoikeudet = sortOpiskeluoikeusLaajatTiedot(language)(
      props.oppija.oppija.opiskeluoikeudet
    )

    const ilmoitukset = sortKuntailmoitusLaajatTiedotLisätiedoilla(
      props.oppija.kuntailmoitukset
    )

    const keskeytykset = props.oppija.oppivelvollisuudenKeskeytykset

    // Yhdistä erilaatuiset asiat yhtenäiseksi listaksi
    return pipe(
      [
        ...ilmoitukset.map((ilmoitus, index) => ({
          order: orderString("A", ilmoitus.kuntailmoitus.aikaleima, index),
          child: (
            <OpiskeluhistoriaIlmoitus
              key={`i-${index}`}
              kuntailmoitus={ilmoitus}
            />
          ),
        })),
        ...opiskeluoikeudet.map((oo, index) => ({
          order: orderString("B", oo.alkamispäivä, index),
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
      ],
      A.sort(opiskeluhistoriaItemOrd),
      pick("child")
    )
  }, [
    language,
    props.oppija.kuntailmoitukset,
    props.oppija.oppija.opiskeluoikeudet,
    props.oppija.oppivelvollisuudenKeskeytykset,
  ])

  return items.length > 0 ? (
    <>{items}</>
  ) : (
    <NoDataMessage>
      <T id="oppija__ei_opiskeluhistoriaa" />
    </NoDataMessage>
  )
}

type OpiskeluhistoriaOpintoProps = {
  opiskeluoikeus: OpiskeluoikeusLaajatTiedot
}

const OpiskeluhistoriaOpinto = ({
  opiskeluoikeus,
}: OpiskeluhistoriaOpintoProps) => {
  const nimi = koodistonimi(
    opiskeluoikeus.tarkasteltavaPäätasonSuoritus.suorituksenTyyppi
  )
  const range = yearRangeString(
    opiskeluoikeus.alkamispäivä,
    opiskeluoikeus.päättymispäivä
  )

  return (
    <IconSection icon={<OpiskeluIcon color="gray" />}>
      <IconSectionHeading>
        {nimi} {range}
      </IconSectionHeading>
      <InfoTable size="tighter">
        {opiskeluoikeus.tarkastelupäivänTila && (
          <InfoTableRow
            label={t("oppija__tila")}
            value={tilaString(opiskeluoikeus)}
          />
        )}
        <InfoTableRow
          label={t("oppija__toimipiste")}
          value={organisaatioNimi(
            opiskeluoikeus.tarkasteltavaPäätasonSuoritus.toimipiste
          )}
        />
        {opiskeluoikeus.tarkasteltavaPäätasonSuoritus.ryhmä && (
          <InfoTableRow
            label={t("oppija__ryhma")}
            value={opiskeluoikeus.tarkasteltavaPäätasonSuoritus.ryhmä}
          />
        )}
        {opiskeluoikeus.vuosiluokkiinSitomatonOpetus && (
          <InfoTableRow
            label={t("oppija__muuta")}
            value={t("oppija__vuosiluokkiin_sitomaton_opetus")}
          />
        )}
        <InfoTableRow
          label="Opiskeluoikeuden alkamispäivä"
          value={formatDate(opiskeluoikeus.alkamispäivä)}
        />
        {opiskeluoikeus.päättymispäivä && (
          <InfoTableRow
            label="Opiskeluoikeuden päättymispäivä"
            value={formatDate(opiskeluoikeus.päättymispäivä)}
          />
        )}
      </InfoTable>
    </IconSection>
  )
}

type OpiskeluhistoriaIlmoitusProps = {
  kuntailmoitus: KuntailmoitusLaajatTiedotLisätiedoilla
}

const OpiskeluhistoriaIlmoitus = ({
  kuntailmoitus,
}: OpiskeluhistoriaIlmoitusProps) => (
  <IconSection icon={<OpiskeluhistoriaTapahtumaIcon color="gray" />}>
    <IconSectionHeading>
      <T id="oppija__ilmoitushistoria_otsikko" />
    </IconSectionHeading>
    <InfoTable size="tighter">
      {kuntailmoitus.kuntailmoitus.aikaleima && (
        <InfoTableRow
          label={t("oppija__ilmoitushistoria_päivämäärä")}
          value={formatDate(kuntailmoitus.kuntailmoitus.aikaleima)}
        />
      )}
      <InfoTableRow
        label={t("oppija__ilmoitushistoria_ilmoittaja")}
        value={organisaatioNimi(
          kuntailmoitus.kuntailmoitus.tekijä.organisaatio
        )}
      />
      <InfoTableRow
        label={t("oppija__ilmoitushistoria_kohde")}
        value={organisaatioNimi(kuntailmoitus.kuntailmoitus.kunta)}
      />
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
    <IconSectionHeading>Oppivelvollisuus</IconSectionHeading>
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
      <div className={b("lisatiedot")} onClick={() => setModalVisibility(true)}>
        <T id="oppija__ilmoitushistoria_lisätiedot" />
      </div>
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

const tilaString = (opiskeluoikeus: OpiskeluoikeusLaajatTiedot): string => {
  const valpasTila = opiskeluoikeus.tarkastelupäivänTila
  const koskiTila = opiskeluoikeus.tarkastelupäivänKoskiTila

  if (valpasTila.koodiarvo === "voimassatulevaisuudessa") {
    const alkamispäivä = formatDate(opiskeluoikeus.alkamispäivä)
    return t("oppija__tila_voimassatulevaisuudessa", {
      päivämäärä: alkamispäivä,
    })
  }

  switch (koskiTila.koodiarvo) {
    case "valiaikaisestikeskeytynyt":
      const tarkastelujaksonAlku = formatDate(
        opiskeluoikeus.tarkastelupäivänAikajaksonAlku
      )
      return t("oppija__tila_valiaikaisesti_keskeytetty", {
        päivämäärä: tarkastelujaksonAlku,
      })
    default:
      return koodistonimi(koskiTila)
  }
}
