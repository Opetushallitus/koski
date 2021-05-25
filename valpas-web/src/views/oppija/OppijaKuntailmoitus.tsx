import bem from "bem-ts"
import React from "react"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { IlmoitusIcon } from "../../components/icons/Icon"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { getLocalized, T, t } from "../../i18n/i18n"
import {
  KuntailmoituksenTekijäLaajatTiedot,
  KuntailmoitusLaajatTiedotLisätiedoilla,
} from "../../state/apitypes/kuntailmoitus"
import { KuntailmoituksenOppijanYhteystiedot } from "../../state/apitypes/kuntailmoituspohjatiedot"
import { ISODateTime } from "../../state/common"
import { joinToString, nonNull } from "../../utils/arrays"
import { formatDate } from "../../utils/date"
import { plainComponent } from "../../utils/plaincomponent"
import "./OppijaKuntailmoitus.less"

const b = bem("kuntailmoitus")

export type OppijaKuntailmoitusProps = {
  kuntailmoitus: KuntailmoitusLaajatTiedotLisätiedoilla
}

export const OppijaKuntailmoitus = (props: OppijaKuntailmoitusProps) => {
  const { kuntailmoitus, aktiivinen } = props.kuntailmoitus

  return (
    <Frame>
      <KuntailmoitusHeader
        aikaleima={kuntailmoitus.aikaleima}
        aktiivinen={aktiivinen}
      />
      <Body>
        <ColumnsContainer>
          <Column size={4}>
            <ColumnHeading>
              <T id="oppija__perustiedot" />
            </ColumnHeading>
            <KuntailmoitusSection
              label={t("oppija__ilmoituksen_kohde")}
              testId="kohde"
            >
              {getLocalized(kuntailmoitus.kunta.nimi)}
            </KuntailmoitusSection>
            <IlmoituksenTekijä tekijä={kuntailmoitus.tekijä} />
          </Column>
          <Column size={8}>
            <ColumnHeading>
              <T id="oppija__tiedot_oppilaasta" />
            </ColumnHeading>
            {kuntailmoitus.oppijanYhteystiedot && (
              <TiedotOppijasta
                yhteystiedot={kuntailmoitus.oppijanYhteystiedot}
                hakenutMuualle={kuntailmoitus.hakenutMuualle}
              />
            )}
          </Column>
        </ColumnsContainer>
      </Body>
    </Frame>
  )
}

type KuntailmoitusHeaderProps = {
  aikaleima?: ISODateTime
  aktiivinen: boolean
}

const KuntailmoitusHeader = (props: KuntailmoitusHeaderProps) => (
  <Header>
    <KuntailmoitusIcon aktiivinen={props.aktiivinen} />
    <Title>
      <T
        id="oppija__vastuuilmoitus_otsikko"
        params={{
          päivämäärä: props.aikaleima ? formatDate(props.aikaleima) : "???",
        }}
      />
    </Title>
  </Header>
)

type KuntailmoitusIconProps = {
  aktiivinen: boolean
}

const KuntailmoitusIcon = (props: KuntailmoitusIconProps) => (
  <IconContainer className={b("icon", { aktiivinen: props.aktiivinen })}>
    <IlmoitusIcon />
  </IconContainer>
)

type KuntailmoitusSectionProps = {
  label?: string
  children: React.ReactNode
  testId?: string
}

const KuntailmoitusSection = (props: KuntailmoitusSectionProps) => (
  <section className={b("section")}>
    {props.label && <SectionName>{props.label}:</SectionName>}
    <div data-testid={props.testId}>{props.children}</div>
  </section>
)

type IlmoituksenTekijäProps = {
  tekijä: KuntailmoituksenTekijäLaajatTiedot
}

const IlmoituksenTekijä = (props: IlmoituksenTekijäProps) => {
  const rows = [
    joinToString([
      props.tekijä.henkilö?.sukunimi,
      props.tekijä.henkilö?.etunimet,
    ]),
    props.tekijä.henkilö?.email,
    props.tekijä.henkilö?.puhelinnumero,
    getLocalized(props.tekijä.organisaatio.nimi),
  ].filter(nonNull)

  return (
    <KuntailmoitusSection
      label={t("oppija__ilmoittajan_yhteystiedot")}
      testId="tekijä"
    >
      {rows.map((row, index) => (
        <div key={index}>{row}</div>
      ))}
    </KuntailmoitusSection>
  )
}

type TiedotOppijastaProps = {
  yhteystiedot: KuntailmoituksenOppijanYhteystiedot
  hakenutMuualle?: boolean
}

const TiedotOppijasta = (props: TiedotOppijastaProps) => {
  const rows: InfoTableRow[] = [
    {
      label: t("oppija__lähiosoite"),
      value: props.yhteystiedot.lähiosoite,
      testId: "lähiosoite",
    },
    {
      label: t("oppija__postitoimipaikka"),
      value: joinToString([
        props.yhteystiedot.postinumero,
        props.yhteystiedot.postitoimipaikka,
      ]),
      testId: "postitoimipaikka",
    },
    {
      label: t("oppija__maa"),
      value: getLocalized(props.yhteystiedot.maa?.nimi),
      testId: "maa",
    },
    {
      label: t("oppija__puhelin"),
      value: props.yhteystiedot.puhelinnumero,
      testId: "puhelin",
    },
    {
      label: t("oppija__email"),
      value: props.yhteystiedot.email,
      testId: "email",
    },
    {
      label: t("oppija__hakenut_muualle"),
      value: props.hakenutMuualle ? "Kyllä" : "Ei",
      testId: "muuHaku",
    },
  ].filter((row) => nonNull(row.value))

  return (
    <KuntailmoitusSection testId="oppija">
      <InfoTable size="tighter">
        {rows.map((row, index) => (
          <InfoTableRow key={index} {...row} />
        ))}
      </InfoTable>
    </KuntailmoitusSection>
  )
}

const Frame = plainComponent("article", b("frame"))
const Header = plainComponent("header", b("header"))
const IconContainer = plainComponent("div", b("icon"))
const Title = plainComponent("div", b("title"))
const Body = plainComponent("section", b("body"))
const ColumnHeading = plainComponent("h3", b("columnheading"))
const SectionName = plainComponent("h4", b("sectionname"))
