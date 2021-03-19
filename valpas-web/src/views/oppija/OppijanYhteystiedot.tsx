import { isNonEmpty } from "fp-ts/lib/Array"
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

export type OppijanYhteystiedotProps = {
  oppija: OppijaHakutilanteilla
}

export const OppijanYhteystiedot = (props: OppijanYhteystiedotProps) => {
  const ilmoitetut = props.oppija.yhteystiedot.filter(Yhteystiedot.isIlmoitettu)
  const viralliset = props.oppija.yhteystiedot.filter(Yhteystiedot.isVirallinen)

  return (
    <ColumnsContainer>
      <Column size={6} id="ilmoitetut-yhteystiedot">
        <TertiaryHeading>
          <T id="oppija__ilmoitetut_yhteystiedot" />
        </TertiaryHeading>
        {isNonEmpty(ilmoitetut) ? (
          <Accordion
            items={ilmoitetut.map((yhteystiedot) => ({
              label:
                getLocalized(yhteystiedot.nimi) || t("oppija__yhteystiedot"),
              render: () => <Yhteystietolista yhteystiedot={yhteystiedot} />,
            }))}
          />
        ) : (
          <NoDataMessage>
            <T id="oppija__ilmoitetut_yhteystiedot_ei_hakemusta" />
          </NoDataMessage>
        )}
      </Column>
      <Column size={6} id="viralliset-yhteystiedot">
        <TertiaryHeading>
          <T id="oppija__viralliset_yhteystiedot" />
        </TertiaryHeading>
        {isNonEmpty(viralliset) ? (
          viralliset.map((yhteystiedot) => (
            <Yhteystietolista
              key={yhteystiedot.alkuperä.alkuperä.koodiarvo}
              yhteystiedot={yhteystiedot}
            />
          ))
        ) : (
          <NoDataMessage>
            <T id="oppija__yhteystietoja_ei_löytynyt" />
          </NoDataMessage>
        )}
      </Column>
    </ColumnsContainer>
  )
}

type YhteystietolistaProps = {
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>
}

const Yhteystietolista = (props: YhteystietolistaProps) => (
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
)
