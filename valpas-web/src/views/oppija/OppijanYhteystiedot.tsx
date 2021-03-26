import bem from "bem-ts"
import { isNonEmpty, uniq } from "fp-ts/lib/Array"
import * as string from "fp-ts/string"
import React from "react"
import { Accordion } from "../../components/containers/Accordion"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { getLocalized, t, T } from "../../i18n/i18n"
import {
  OppijaHakutilanteilla,
  Yhteystiedot,
  YhteystietojenAlkuperä,
} from "../../state/oppijat"
import { nonNull } from "../../utils/arrays"
import { formatDate } from "../../utils/date"
import "./OppijanYhteystiedot.less"

const b = bem("oppijanyhteystiedot")

export type OppijanYhteystiedotProps = {
  oppija: OppijaHakutilanteilla
}

export const OppijanYhteystiedot = (props: OppijanYhteystiedotProps) => {
  const ilmoitetut = props.oppija.yhteystiedot.filter(Yhteystiedot.isIlmoitettu)
  const viralliset = props.oppija.yhteystiedot.filter(Yhteystiedot.isVirallinen)
  const viewIlmoitetut = ilmoitetut.length > 0

  return (
    <ColumnsContainer>
      {viewIlmoitetut && (
        <Column size={6} id="ilmoitetut-yhteystiedot">
          <TertiaryHeading>
            <T id="oppija__ilmoitetut_yhteystiedot" />
          </TertiaryHeading>
          <YhteistietoAccordion
            yhteystiedot={ilmoitetut}
            label={(yt) =>
              (getLocalized(yt.yhteystietoryhmänNimi) ||
                t("oppija__yhteystiedot")) +
              " – " +
              formatDate(yt.alkuperä.haunAlkamispaivämäärä)
            }
          />
        </Column>
      )}

      <Column size={viewIlmoitetut ? 6 : 12} id="viralliset-yhteystiedot">
        <TertiaryHeading>
          <T id="oppija__viralliset_yhteystiedot" />
        </TertiaryHeading>
        <YhteistietoAccordion
          yhteystiedot={viralliset}
          label={(yt) =>
            uniq(string.Eq)([
              getLocalized(yt.alkuperä.alkuperä.nimi)!,
              getLocalized(yt.alkuperä.tyyppi.nimi)!,
            ]).join(": ")
          }
          noDataMessage={t(
            props.oppija.oppija.henkilö.turvakielto
              ? "oppija__henkilöllä_turvakielto"
              : "oppija__yhteystietoja_ei_löytynyt"
          )}
        />
      </Column>
    </ColumnsContainer>
  )
}

type YhteystietoAccordionProps<T extends YhteystietojenAlkuperä> = {
  yhteystiedot: Array<Yhteystiedot<T>>
  label: (yt: Yhteystiedot<T>) => string
  noDataMessage?: string
}

const YhteistietoAccordion = <T extends YhteystietojenAlkuperä>(
  props: YhteystietoAccordionProps<T>
) =>
  isNonEmpty(props.yhteystiedot) ? (
    <Accordion
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
    <InfoTable>
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
      {(props.yhteystiedot.kunta || props.yhteystiedot.postinumero) && (
        <InfoTableRow
          label={t("oppija__postitoimipaikka")}
          value={[props.yhteystiedot.postinumero, props.yhteystiedot.kunta]
            .filter(nonNull)
            .join(" ")}
        />
      )}
      {props.yhteystiedot.maa && (
        <InfoTableRow label={t("oppija__maa")} value={props.yhteystiedot.maa} />
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
    {Yhteystiedot.isIlmoitettu(props.yhteystiedot) && (
      <div className={b("lahde")}>
        <T
          id="oppija__ilmoitetun_yhteystiedon_lahde"
          params={{
            haku: getLocalized(props.yhteystiedot.alkuperä.hakuNimi) || "?",
          }}
        />
      </div>
    )}
  </>
)
