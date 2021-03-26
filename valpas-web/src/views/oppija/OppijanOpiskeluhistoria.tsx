import React from "react"
import {
  IconSection,
  IconSectionHeading,
} from "../../components/containers/IconSection"
import { OpiskeluIcon } from "../../components/icons/Icon"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { getLocalized, T, t, useLanguage } from "../../i18n/i18n"
import { KoodistoKoodiviite } from "../../state/koodistot"
import { Opiskeluoikeus, OppijaHakutilanteilla } from "../../state/oppijat"
import { ISODate } from "../../state/types"
import { formatNullableDate, parseYear } from "../../utils/date"

export type OppijanOpiskeluhistoriaProps = {
  oppija: OppijaHakutilanteilla
}

export const OppijanOpiskeluhistoria = (
  props: OppijanOpiskeluhistoriaProps
) => {
  const language = useLanguage()
  const sort = Opiskeluoikeus.sort(language)

  return props.oppija.oppija.opiskeluoikeudet.length > 0 ? (
    <>
      {sort(props.oppija.oppija.opiskeluoikeudet).map((opiskeluoikeus) => {
        const nimi = koodistonimi(opiskeluoikeus.tyyppi)
        const range = yearRangeString(
          opiskeluoikeus.alkamispäivä,
          opiskeluoikeus.päättymispäivä
        )

        return (
          <IconSection
            key={opiskeluoikeus.oid}
            icon={<OpiskeluIcon color="gray" />}
          >
            <IconSectionHeading>
              {nimi} {range}
            </IconSectionHeading>
            <InfoTable size="tighter">
              <InfoTableRow
                value={getLocalized(opiskeluoikeus.oppilaitos.nimi)}
              />
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
      })}
    </>
  ) : (
    <NoDataMessage>
      <T id="oppija__ei_opiskeluhistoriaa" />
    </NoDataMessage>
  )
}

const koodistonimi = (k: KoodistoKoodiviite<string, string>): string =>
  getLocalized(k.nimi) || k.koodiarvo

const yearRangeString = (a?: ISODate, b?: ISODate): string =>
  a || b ? [yearString(a), "–", yearString(b)].filter((s) => !!s).join(" ") : ""

const yearString = (date?: ISODate): string | undefined =>
  date && parseYear(date).toString()

const tilaString = (opiskeluoikeus: Opiskeluoikeus): string => {
  const tila = opiskeluoikeus.tarkastelupäivänTila
  const alkamispäivä = formatNullableDate(opiskeluoikeus.alkamispäivä)
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
