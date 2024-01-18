import bem from "bem-ts"
import { isNonEmpty, uniq } from "fp-ts/lib/Array"
import * as string from "fp-ts/string"
import React from "react"
import { Accordion } from "../../components/containers/Accordion"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { IconSection } from "../../components/containers/IconSection"
import { WarningIcon } from "../../components/icons/Icon"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import {
  getLocalized,
  getLocalizedMaybe,
  koodiviiteToShortString,
  t,
  T,
} from "../../i18n/i18n"
import { HenkilöLaajatTiedot } from "../../state/apitypes/henkilo"
import {
  isHakemukselta,
  isRekisteristä,
  Yhteystiedot,
  YhteystietojenAlkuperä,
} from "../../state/apitypes/yhteystiedot"
import { useIsKansalainenView } from "../../state/kansalainenContext"
import { nonNull } from "../../utils/arrays"
import { formatDate } from "../../utils/date"
import "./OppijanYhteystiedot.less"

const b = bem("oppijanyhteystiedot")

export type OppijanYhteystiedotProps = {
  henkilö: HenkilöLaajatTiedot
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
}

export const OppijanYhteystiedot = (props: OppijanYhteystiedotProps) => {
  const ilmoitetut = props.yhteystiedot.filter(isHakemukselta)
  const viralliset = props.yhteystiedot.filter(isRekisteristä)
  const viewIlmoitetut = ilmoitetut.length > 0
  const isKansalainenView = useIsKansalainenView()

  return (
    <>
      {props.henkilö.turvakielto && (
        <IconSection icon={<WarningIcon />} id="turvakielto-varoitus">
          <T
            id={
              isKansalainenView
                ? "oppija__kansalainen_turvakielto_varoitus"
                : "oppija__turvakielto_varoitus"
            }
          />
        </IconSection>
      )}
      <InfoTable id={"kotikunta-yhteystiedot-table"}>
        {props.henkilö.turvakielto ? (
          <InfoTableRow
            label={t("oppija__virallinen_kotikunta")}
            value={
              <NoDataMessage>
                {t("oppija__henkilöllä_turvakielto")}
              </NoDataMessage>
            }
            testId={"kotikunta-yhteystiedot"}
          />
        ) : (
          <InfoTableRow
            label={t("oppija__virallinen_kotikunta")}
            value={
              props.henkilö.kotikunta ? (
                koodiviiteToShortString(props.henkilö.kotikunta)
              ) : (
                <NoDataMessage>{t("tieto_puuttuu")}</NoDataMessage>
              )
            }
            testId={"kotikunta-yhteystiedot"}
          />
        )}
      </InfoTable>
      <ColumnsContainer>
        {viewIlmoitetut && (
          <Column size={6} id="ilmoitetut-yhteystiedot">
            <TertiaryHeading>
              <T id="oppija__ilmoitetut_yhteystiedot" />
            </TertiaryHeading>
            <YhteystietoAccordion
              yhteystiedot={ilmoitetut}
              label={(yt) =>
                getLocalized(yt.yhteystietoryhmänNimi) +
                (yt.alkuperä.hakemuksenMuokkauksenAikaleima !== undefined
                  ? " – " +
                    formatDate(yt.alkuperä.hakemuksenMuokkauksenAikaleima)
                  : formatDate(yt.alkuperä.haunAlkamispaivämäärä))
              }
            />
          </Column>
        )}

        <Column size={viewIlmoitetut ? 6 : 12} id="viralliset-yhteystiedot">
          <TertiaryHeading>
            <T id="oppija__viralliset_yhteystiedot" />
          </TertiaryHeading>
          <YhteystietoAccordion
            yhteystiedot={viralliset}
            label={(yt) =>
              uniq(string.Eq)([
                getLocalizedMaybe(yt.alkuperä.alkuperä.nimi)!,
                getLocalizedMaybe(yt.alkuperä.tyyppi.nimi)!,
              ]).join(": ")
            }
            noDataMessage={t(
              props.henkilö.turvakielto
                ? "oppija__henkilöllä_turvakielto"
                : "oppija__yhteystietoja_ei_löytynyt",
            )}
          />
        </Column>
      </ColumnsContainer>
    </>
  )
}

type YhteystietoAccordionProps<T extends YhteystietojenAlkuperä> = {
  yhteystiedot: Array<Yhteystiedot<T>>
  label: (yt: Yhteystiedot<T>) => string
  noDataMessage?: string
}

const YhteystietoAccordion = <T extends YhteystietojenAlkuperä>(
  props: YhteystietoAccordionProps<T>,
) =>
  isNonEmpty(props.yhteystiedot) ? (
    <Accordion
      accordionId="yhteystiedot"
      items={props.yhteystiedot.map((yhteystiedot) => ({
        label: props.label(yhteystiedot),
        render: () => <Yhteystietolista yhteystiedot={yhteystiedot} />,
      }))}
    />
  ) : (
    <NoDataMessage>{props.noDataMessage}</NoDataMessage>
  )

type YhteystietolistaProps = {
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
}

const Yhteystietolista = (props: YhteystietolistaProps) => (
  <>
    <InfoTable size="tighter">
      {props.yhteystiedot.henkilönimi && (
        <InfoTableRow
          label={t("oppija__nimi")}
          value={props.yhteystiedot.henkilönimi}
        />
      )}
      {props.yhteystiedot.lähiosoite && (
        <InfoTableRow
          label={t("oppija__lähiosoite")}
          value={props.yhteystiedot.lähiosoite}
        />
      )}
      {(props.yhteystiedot.postitoimipaikka ||
        props.yhteystiedot.postinumero) && (
        <InfoTableRow
          label={t("oppija__postitoimipaikka")}
          value={[
            props.yhteystiedot.postinumero,
            props.yhteystiedot.postitoimipaikka,
          ]
            .filter(nonNull)
            .join(" ")}
        />
      )}
      {props.yhteystiedot.maa && (
        <InfoTableRow
          label={t("oppija__maa")}
          value={getLocalizedMaybe(props.yhteystiedot.maa)}
        />
      )}
      {props.yhteystiedot.puhelinnumero && (
        <InfoTableRow
          label={t("oppija__puhelin")}
          value={props.yhteystiedot.puhelinnumero}
        />
      )}
      {props.yhteystiedot.matkapuhelinnumero &&
        props.yhteystiedot.puhelinnumero !==
          props.yhteystiedot.matkapuhelinnumero && (
          <InfoTableRow
            label={t("oppija__matkapuhelin")}
            value={props.yhteystiedot.matkapuhelinnumero}
          />
        )}
      {props.yhteystiedot.sähköposti && (
        <InfoTableRow
          label={t("oppija__email")}
          value={props.yhteystiedot.sähköposti}
        />
      )}
    </InfoTable>
    {isHakemukselta(props.yhteystiedot) && (
      <div className={b("lahde")}>
        <T
          id="oppija__ilmoitetun_yhteystiedon_lahde"
          params={{
            haku: getLocalized(props.yhteystiedot.alkuperä.hakuNimi),
          }}
        />
      </div>
    )}
  </>
)
