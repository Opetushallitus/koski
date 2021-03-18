import React from "react"
import { Accordion } from "../../components/containers/Accordion"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { t, T } from "../../i18n/i18n"
import { Haku, OppijaHakutilanteilla } from "../../state/oppijat"

export type OppijanYhteystiedotProps = {
  oppija: OppijaHakutilanteilla
}

export const OppijanYhteystiedot = (props: OppijanYhteystiedotProps) => {
  const haku = Haku.latest(props.oppija.haut || [])
  const hakuYht = haku && getYhteystiedot(haku)
  const huoltajaYht = haku && getHuoltajanYhteystiedot(haku)
  const ilmoitettujaYhteystietoja = !!(hakuYht || huoltajaYht)

  return (
    <ColumnsContainer>
      <Column size={6} id="ilmoitetut-yhteystiedot">
        <TertiaryHeading>
          <T id="oppija__ilmoitetut_yhteystiedot" />
        </TertiaryHeading>
        {ilmoitettujaYhteystietoja ? (
          <Accordion
            items={[
              hakuYht && {
                label: t("oppija__yhteystiedot"),
                render: () => <Yhteystietolista yhteystiedot={hakuYht} />,
              },
              huoltajaYht && {
                label: t("oppija__huoltaja"),
                render: () => <Yhteystietolista yhteystiedot={huoltajaYht} />,
              },
            ]}
          />
        ) : (
          <NoDataMessage>
            <T id="oppija__ilmoitetut_yhteystiedot_ei_hakemusta" />
          </NoDataMessage>
        )}
      </Column>
      <Column size={6}>
        <TertiaryHeading>
          <T id="oppija__viralliset_yhteystiedot" />
        </TertiaryHeading>
        <NoDataMessage>Tämä ominaisuus on toteuttamatta</NoDataMessage>
      </Column>
    </ColumnsContainer>
  )
}

type YhteystietolistaProps = {
  yhteystiedot: Yhteystiedot
}

const Yhteystietolista = (props: YhteystietolistaProps) => (
  <InfoTable>
    {props.yhteystiedot.nimi && (
      <InfoTableRow label={t("oppija__nimi")} value={props.yhteystiedot.nimi} />
    )}
    {props.yhteystiedot.osoite && (
      <InfoTableRow
        label={t("oppija__osoite")}
        value={props.yhteystiedot.osoite}
      />
    )}
    {props.yhteystiedot.puhelin && (
      <InfoTableRow
        label={t("oppija__puhelin")}
        value={props.yhteystiedot.puhelin}
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

type Yhteystiedot = {
  nimi?: string
  osoite?: string
  puhelin?: string
  sähköposti?: string
}

const getYhteystiedot = (haku: Haku): Yhteystiedot | null =>
  haku.osoite || haku.puhelinnumero || haku.sähköposti
    ? {
        osoite: haku.osoite,
        puhelin: haku.puhelinnumero,
        sähköposti: haku.sähköposti,
      }
    : null

const getHuoltajanYhteystiedot = (haku: Haku): Yhteystiedot | null =>
  haku.huoltajanNimi || haku.huoltajanPuhelinnumero || haku.huoltajanSähköposti
    ? {
        nimi: haku.huoltajanNimi,
        puhelin: haku.puhelinnumero,
        sähköposti: haku.huoltajanSähköposti,
      }
    : null
