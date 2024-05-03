import bem from "bem-ts"
import { isEmpty } from "fp-ts/lib/Array"
import React, { useCallback } from "react"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { IlmoitusIcon } from "../../components/icons/Icon"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { getLocalizedMaybe, T, t } from "../../i18n/i18n"
import {
  KuntailmoituksenTekijäLaajatTiedot,
  kuntaKotipaikka,
} from "../../state/apitypes/kuntailmoitus"
import { KuntailmoituksenOppijanYhteystiedot } from "../../state/apitypes/kuntailmoituspohjatiedot"
import { organisaatioNimi } from "../../state/apitypes/organisaatiot"
import { ISODateTime } from "../../state/common"
import { nonNull, nullableJoinToString } from "../../utils/arrays"
import { formatDate } from "../../utils/date"
import { plainComponent } from "../../utils/plaincomponent"
import "./OppijaKuntailmoitus.less"
import {
  isTurvakiellollinenKuntailmoitus,
  MinimiOppijaKuntailmoitus,
} from "./typeIntersections"
import { useApiMethod } from "../../api/apiHooks"
import { mitätöiKuntailmoitus } from "../../api/api"
import { usePrompt } from "../../components/containers/Prompt"
import { pipe } from "fp-ts/lib/function"
import * as E from "fp-ts/Either"
import { isFeatureFlagEnabled } from "../../state/featureFlags"
import { FlatButton } from "../../components/buttons/FlatButton"

const b = bem("kuntailmoitus")

export type OppijaKuntailmoitusProps = {
  kuntailmoitus: MinimiOppijaKuntailmoitus
}

export const OppijaKuntailmoitus = (props: OppijaKuntailmoitusProps) => {
  const kuntailmoitus = props.kuntailmoitus
  const turvakielto = isTurvakiellollinenKuntailmoitus(kuntailmoitus)
  const mitätöiImoitusApi = useApiMethod(mitätöiKuntailmoitus)
  const prompt = usePrompt()
  const onMitätöinti = useCallback(() => {
    prompt.show(t("kuntailmoitus__mitätöinti_varmistus"), async () => {
      pipe(
        await mitätöiImoitusApi.call(props.kuntailmoitus.id as string),
        E.map(() => location.reload()),
      )
    })
  }, [mitätöiImoitusApi, prompt, props.kuntailmoitus.id])

  return (
    <Frame>
      <KuntailmoitusHeader
        aikaleima={kuntailmoitus.aikaleima}
        aktiivinen={!!kuntailmoitus.aktiivinen}
      />
      <Body>
        {turvakielto ? (
          <NoDataMessage>
            <T id="oppija__tiedot_näkyvät_vain_vastaanottajalle_ja_lähettäjälle" />
          </NoDataMessage>
        ) : (
          <ColumnsContainer>
            <Column size={4}>
              <ColumnHeading>
                <T id="oppija__perustiedot" />
              </ColumnHeading>
              {kuntailmoitus.kunta && (
                <KuntailmoitusSection
                  label={t("oppija__ilmoituksen_kohde")}
                  testId="kohde"
                >
                  {kuntaKotipaikka(kuntailmoitus.kunta)}
                </KuntailmoitusSection>
              )}
              {kuntailmoitus.tekijä && (
                <IlmoituksenTekijä tekijä={kuntailmoitus.tekijä} />
              )}
            </Column>
            <Column size={8}>
              <ColumnHeading>
                <T id="oppija__tiedot_oppilaasta" />
              </ColumnHeading>
              <TiedotOppijasta
                yhteystiedot={kuntailmoitus.oppijanYhteystiedot}
                hakenutMuualle={kuntailmoitus.hakenutMuualle}
                tietojaKarsittu={kuntailmoitus.tietojaKarsittu}
              />
            </Column>
          </ColumnsContainer>
        )}
        {isFeatureFlagEnabled("kuntailmoitusMitätöinti") &&
          kuntailmoitus.oikeusTekijäOrganisaatioon && (
            <FlatButton
              testId="mitätöi-kuntailmoitus-btn"
              onClick={onMitätöinti}
            >
              {t("kuntailmoitus__mitätöinti_btn")}
            </FlatButton>
          )}
        {prompt.component}
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
    nullableJoinToString(" ")([
      props.tekijä.henkilö?.sukunimi,
      props.tekijä.henkilö?.etunimet,
    ]),
    props.tekijä.henkilö?.email,
    props.tekijä.henkilö?.puhelinnumero,
    organisaatioNimi(props.tekijä.organisaatio),
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
  yhteystiedot?: KuntailmoituksenOppijanYhteystiedot
  hakenutMuualle?: boolean
  tietojaKarsittu?: boolean
}

const TiedotOppijasta = (props: TiedotOppijastaProps) => {
  const rows: InfoTableRow[] = props.yhteystiedot
    ? [
        {
          label: t("oppija__lähiosoite"),
          value: props.yhteystiedot.lähiosoite,
          testId: "lähiosoite",
        },
        {
          label: t("oppija__postitoimipaikka"),
          value: nullableJoinToString(" ")([
            props.yhteystiedot.postinumero,
            props.yhteystiedot.postitoimipaikka,
          ]),
          testId: "postitoimipaikka",
        },
        {
          label: t("oppija__maa"),
          value: getLocalizedMaybe(props.yhteystiedot.maa?.nimi),
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
    : []

  return (
    <KuntailmoitusSection testId="oppija">
      <InfoTable size="tighter">
        {!props.yhteystiedot &&
          (props.tietojaKarsittu ? (
            <TiedotOppijastaError textId="oppija__tiedot_näkyvät_vain_vastaanottajalle_ja_lähettäjälle" />
          ) : (
            <TiedotOppijastaError textId="oppija__lisätiedot_poistettu" />
          ))}
        {props.yhteystiedot && isEmpty(rows) && (
          <TiedotOppijastaError textId="oppija__ei_tietoja_ilmotuksella" />
        )}
        {rows.map((row, index) => (
          <InfoTableRow key={index} {...row} />
        ))}
      </InfoTable>
    </KuntailmoitusSection>
  )
}

type TiedotOppijastaErrorProps = {
  textId: string
}

const TiedotOppijastaError = (props: TiedotOppijastaErrorProps) => (
  <InfoTableRow
    value={
      <NoDataMessage>
        <T id={props.textId} />
      </NoDataMessage>
    }
  />
)

const Frame = plainComponent("article", b("frame"))
const Header = plainComponent("header", b("header"))
const IconContainer = plainComponent("div", b("icon"))
const Title = plainComponent("div", b("title"))
const Body = plainComponent("section", b("body"))
const ColumnHeading = plainComponent("h3", b("columnheading"))
const SectionName = plainComponent("h4", b("sectionname"))
