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
import { IlmoitusListIcon, OpiskeluIcon } from "../../components/icons/Icon"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { getLocalized, T, t, useLanguage } from "../../i18n/i18n"
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
import { ISODate } from "../../state/common"
import { formatDate, formatNullableDate, parseYear } from "../../utils/date"
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
      ],
      A.sort(opiskeluhistoriaItemOrd),
      pick("child")
    )
  }, [
    language,
    props.oppija.kuntailmoitukset,
    props.oppija.oppija.opiskeluoikeudet,
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
  const nimi = koodistonimi(opiskeluoikeus.tyyppi)
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
        <InfoTableRow value={getLocalized(opiskeluoikeus.oppilaitos.nimi)} />
        {opiskeluoikeus.ryhmä && (
          <InfoTableRow
            label={t("oppija__ryhma")}
            value={opiskeluoikeus.ryhmä}
          />
        )}
        {opiskeluoikeus.tarkastelupäivänTila && (
          <InfoTableRow
            label={t("oppija__tila")}
            value={tilaString(opiskeluoikeus)}
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
  <IconSection icon={<IlmoitusListIcon color="gray" />}>
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
        value={getLocalized(
          kuntailmoitus.kuntailmoitus.tekijä.organisaatio.nimi
        )}
      />
      <InfoTableRow
        label={t("oppija__ilmoitushistoria_kohde")}
        value={getLocalized(kuntailmoitus.kuntailmoitus.kunta.nimi)}
      />
      <InfoTableRow value={<IlmoitusLink kuntailmoitus={kuntailmoitus} />} />
    </InfoTable>
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
  getLocalized(k.nimi) || k.koodiarvo

const yearRangeString = (a?: ISODate, b?: ISODate): string =>
  a || b ? [yearString(a), "–", yearString(b)].filter((s) => !!s).join(" ") : ""

const yearString = (date?: ISODate): string | undefined =>
  date && parseYear(date).toString()

const tilaString = (opiskeluoikeus: OpiskeluoikeusLaajatTiedot): string => {
  const tila = opiskeluoikeus.tarkastelupäivänTila
  const alkamispäivä = formatDate(opiskeluoikeus.alkamispäivä)
  const päättymispäivä = formatNullableDate(opiskeluoikeus.päättymispäivä)

  switch (tila.koodiarvo) {
    case "voimassatulevaisuudessa":
      return t("oppija__tila_voimassatulevaisuudessa", {
        päivämäärä: alkamispäivä,
      })
    case "valmistunut":
      return t("oppija__tila_valmistunut", { päivämäärä: päättymispäivä })
    case "eronnut":
      return t("oppija__tila_eronnut", { päivämäärä: päättymispäivä })
    case "katsotaaneronneeksi":
      return t("oppija__tila_katsotaaneronneeksi", {
        päivämäärä: päättymispäivä,
      })
    case "mitatoity":
      return t("oppija__tila_mitatoity", { päivämäärä: päättymispäivä })
    case "voimassa":
    case "peruutettu":
    case "tuntematon":
    default:
      return koodistonimi(tila)
  }
}
