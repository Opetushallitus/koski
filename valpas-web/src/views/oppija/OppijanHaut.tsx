import bem from "bem-ts"
import * as A from "fp-ts/Array"
import React from "react"
import { IconSection } from "../../components/containers/IconSection"
import { HakuIcon } from "../../components/icons/Icon"
import { LongArrow } from "../../components/icons/LongArrow"
import { ExternalLink } from "../../components/navigation/ExternalLink"
import { Datum } from "../../components/tables/DataTable"
import { LeanTable } from "../../components/tables/LeanTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import {
  formatFixedNumber,
  getLocalizedMaybe,
  koodiviiteToShortString,
  t,
  T,
} from "../../i18n/i18n"
import {
  HakuLaajatTiedot,
  sortHakuLaajatTiedot,
} from "../../state/apitypes/haku"
import {
  Hakutoive,
  isVastaanotettu,
  isVastaanotettuEhdollisesti,
} from "../../state/apitypes/hakutoive"
import { plainComponent } from "../../utils/plaincomponent"
import "./OppijanHaut.less"

const b = bem("oppijanhaut")

export type OppijanHautProps = {
  hakutilanteet: HakuLaajatTiedot[]
  hakutilanneError?: string
}

export const OppijanHaut = (props: OppijanHautProps) => {
  const haut = sortHakuLaajatTiedot(props.hakutilanteet)
  const error = props.hakutilanneError
  return error ? (
    <NoDataMessage>
      <T id="oppija__hakuhistoria_virhe" />
    </NoDataMessage>
  ) : A.isNonEmpty(haut) ? (
    <div className={b()}>
      {haut.map((haku) => (
        <HakuTable key={haku.hakuOid} haku={haku} />
      ))}
    </div>
  ) : (
    <NoDataMessage>
      <T id="oppija__ei_hakuhistoriaa" />
    </NoDataMessage>
  )
}

type HakuTableProps = {
  haku: HakuLaajatTiedot
}

const HakuTable = (props: HakuTableProps) => {
  const hasHarkinnanvaraisuus = props.haku.hakutoiveet.some(
    (toive) => toive.harkinnanvarainen,
  )

  return (
    <IconSection icon={<HakuIcon color="gray" />}>
      <TertiaryHeading className={b("hakunimi")}>
        <HaunNimi haku={props.haku} />{" "}
        <ExternalLink to={props.haku.hakemusUrl}>
          <T
            id={
              props.haku
                ? "hakemuksentila__hakenut"
                : "hakemuksentila__ei_hakenut"
            }
          />
        </ExternalLink>
      </TertiaryHeading>
      <LeanTable
        className={b("table")}
        columns={[
          {
            label: t("oppija__hakukohde"),
            size: "col6",
          },
          {
            label: t("oppija__valintatilanne"),
            size: "col4",
          },
          {
            label: t("oppija__pisteet"),
            size: "col3",
          },
          {
            label: t("oppija__alin_pistemäärä"),
            size: "col3",
          },
        ]}
        data={props.haku.hakutoiveet.map(hakutoiveToTableValue)}
      />
      {hasHarkinnanvaraisuus ? (
        <div className={b("footnotes")}>
          1) <T id="oppija__hakenut_harkinnanvaraisesti" />
        </div>
      ) : null}
    </IconSection>
  )
}

type HaunNimiProps = {
  haku: HakuLaajatTiedot
}

const HaunNimi = (props: HaunNimiProps) => {
  const haunNimi = getLocalizedMaybe(props.haku.hakuNimi)
  return (
    <span
      className={b("hakunimititle", { paattynyt: !props.haku.aktiivinenHaku })}
    >
      {props.haku.aktiivinenHaku
        ? haunNimi
        : t("oppija__paattynyt_haku", { haku: haunNimi || "?" })}
    </span>
  )
}

const hakutoiveToTableValue = (hakutoive: Hakutoive, index: number): Datum => ({
  key: [index.toString()],
  values: [
    {
      value:
        formatOrderNumber(hakutoive.hakutoivenumero) +
        (getLocalizedMaybe(hakutoive.organisaatioNimi) || t("tieto_puuttuu")) +
        (hakutoive.hakukohdeNimi
          ? ", " + getLocalizedMaybe(hakutoive.hakukohdeNimi)
          : ""),
      display: (
        <>
          {formatOrderNumber(hakutoive.hakutoivenumero)}
          {hakutoive.organisaatioNimi ? (
            getLocalizedMaybe(hakutoive.organisaatioNimi)
          ) : (
            <NoDataMessage>
              <T id="tieto_puuttuu" />
            </NoDataMessage>
          )}
          {hakutoive.hakukohdeNimi &&
            ", " + getLocalizedMaybe(hakutoive.hakukohdeNimi)}
          {hakutoive.harkinnanvarainen ? (
            <FootnoteReference>1</FootnoteReference>
          ) : null}
          {isVastaanotettu(hakutoive) ? (
            <div>
              <LongArrow />
              <span className={b("otettuvastaan")}>
                {isVastaanotettuEhdollisesti(hakutoive) ? (
                  <T id="oppija__otettu_vastaan_ehdollisesti" />
                ) : (
                  <T id="oppija__otettu_vastaan" />
                )}
              </span>
            </div>
          ) : null}
        </>
      ),
    },
    {
      value:
        hakutoive?.valintatila &&
        (hakutoive.valintatila.koodiarvo === "varasijalla" &&
        hakutoive.varasijanumero !== undefined
          ? t("valintatieto__ns_varasija", { n: hakutoive.varasijanumero })
          : koodiviiteToShortString(hakutoive.valintatila)),
    },
    { value: formatFixedNumber(hakutoive.pisteet, 2) },
    { value: formatFixedNumber(hakutoive.alinHyvaksyttyPistemaara, 2) },
  ],
})

const formatOrderNumber = (n?: number): string =>
  n !== undefined ? `${n}. ` : ""

const FootnoteReference = plainComponent("span", b("footnotereference"))
