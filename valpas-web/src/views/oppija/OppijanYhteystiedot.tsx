import React from "react"
import { InfoTable, InfoTableRow } from "../../components/tables/InfoTable"
import { TertiaryHeading } from "../../components/typography/headings"
import { Haku, OppijaHakutilanteilla } from "../../state/oppijat"

export type OppijanYhteystiedotProps = {
  oppija: OppijaHakutilanteilla
}

export const OppijanYhteystiedot = (props: OppijanYhteystiedotProps) => {
  const haku = Haku.latest(props.oppija.haut || [])
  const hakuYht = haku && getYhteystiedot(haku)
  const huoltajaYht = haku && getHuoltajanYhteystiedot(haku)

  return (
    <div>
      {hakuYht && (
        <Yhteystietolista
          label="Ilmoitetut yhteystiedot"
          yhteystiedot={hakuYht}
        />
      )}
      {huoltajaYht && (
        <Yhteystietolista
          label="Ilmoitetut huoltajan yhteystiedot"
          yhteystiedot={huoltajaYht}
        />
      )}
    </div>
  )
}

type YhteystietolistaProps = {
  label: string
  yhteystiedot: Yhteystiedot
}

const Yhteystietolista = (props: YhteystietolistaProps) => (
  <div>
    <TertiaryHeading>{props.label}</TertiaryHeading>
    <InfoTable>
      {props.yhteystiedot.nimi && (
        <InfoTableRow label="Nimi" value={props.yhteystiedot.nimi} />
      )}
      {props.yhteystiedot.osoite && (
        <InfoTableRow label="Osoite" value={props.yhteystiedot.osoite} />
      )}
      {props.yhteystiedot.puhelin && (
        <InfoTableRow label="Puhelin" value={props.yhteystiedot.puhelin} />
      )}
      {props.yhteystiedot.sähköposti && (
        <InfoTableRow
          label="Sähköposti"
          value={props.yhteystiedot.sähköposti}
        />
      )}
    </InfoTable>
  </div>
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
