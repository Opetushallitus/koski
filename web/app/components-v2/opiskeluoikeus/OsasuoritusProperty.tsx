import React, { createContext, useContext } from 'react'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Column, ColumnRow } from '../containers/Columns'
import { t } from '../../i18n/i18n'

/**
 * Kentän nimen sisennystaso laajennetun osasuorituksen ruudukossa.
 *
 * 0 = OsasuoritusPropertyn oma nimi, 1 = sen arvo tai OsasuoritusSubpropertyn
 * nimi, 2 = alikentän arvo. Taso kertoo, monennessako ruudukkosarakkeessa
 * elementti on: nimet saavat oman max-content-sarakkeensa ja arvo alkaa heti
 * oman tasonsa nimisarakkeen jälkeen. Ks. OsasuoritusProperty.less.
 */
const PropertyLevelContext = createContext(0)

type SisennysStyle = React.CSSProperties & {
  '--osasuoritus-taso': number
  '--osasuoritus-merkkitasot': number
}

export type OsasuoritusPropertiesProps = CommonPropsWithChildren<{
  /**
   * Laajennetun rivin sisällön sisennys taulukossa: sisällön taso ja niiden
   * ylempien tasojen määrä, joiden taulukossa on valmis-merkki. Kentät
   * asettuvat samaan kohtaan kuin sisäkkäisen taulukon rivit.
   */
  taso: number
  merkkitasot: number
}>

/**
 * Laajennetun osasuorituksen kenttien yhteinen ruudukko. Nimisarakkeet ovat
 * max-content-levyisiä ja jaettu kaikkien rivien kesken (rivit ovat subgrid),
 * jotta nimi vie täsmälleen tarvitsemansa tilan ja arvot ovat silti linjassa —
 * kuten vanhassa käyttöliittymässä, jossa kentät olivat taulukon soluja.
 *
 * Kiinteä sarakemäärä ei riitä, koska ruudukkosarakkeen leveys riippuu
 * taulukon leveydestä: perusopetuksessa oppiainetaulukoita on kaksi rinnakkain,
 * jolloin sama neljän sarakkeen nimikenttä on 95 px eikä 219 px kuten
 * ammatillisen koko leveyden taulukossa.
 */
export const OsasuoritusProperties: React.FC<OsasuoritusPropertiesProps> = (
  props
) => (
  <section
    {...common(props, ['OsasuoritusProperties'])}
    style={
      {
        '--osasuoritus-taso': props.taso,
        '--osasuoritus-merkkitasot': props.merkkitasot
      } as SisennysStyle
    }
  >
    {props.children}
  </section>
)

export type OsasuoritusPropertyProps = CommonPropsWithChildren<{
  label: string
  /** Kenttä nimineen harmaalla taustalla (esim. ammatillisen näyttö). */
  korostettu?: boolean
}>

export const OsasuoritusProperty: React.FC<OsasuoritusPropertyProps> = (
  props
) => (
  <ColumnRow
    {...common(props, [
      'OsasuoritusProperty',
      props.korostettu && 'OsasuoritusProperty--korostettu'
    ])}
    valign="top"
  >
    <OsasuoritusPropertyLabel>{t(props.label)}</OsasuoritusPropertyLabel>
    <PropertyLevelContext.Provider value={1}>
      {props.children}
    </PropertyLevelContext.Provider>
  </ColumnRow>
)

export type OsasuoritusSubpropertyProps = CommonPropsWithChildren<{
  label: string
  rowNumber?: number
}>

export const OsasuoritusSubproperty: React.FC<OsasuoritusSubpropertyProps> = (
  props
) => {
  const level = useContext(PropertyLevelContext)
  return (
    <>
      <OsasuoritusPropertyLabel row={props.rowNumber}>
        {t(props.label)}
      </OsasuoritusPropertyLabel>
      <PropertyLevelContext.Provider value={level + 1}>
        <OsasuoritusPropertyValue row={props.rowNumber}>
          {props.children}
        </OsasuoritusPropertyValue>
      </PropertyLevelContext.Provider>
    </>
  )
}

export type OsasuoritusPropertyLabel = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyLabel: React.FC<OsasuoritusPropertyLabel> = (
  props
) => {
  const level = useContext(PropertyLevelContext)
  return (
    <Column
      row={props.row || 0}
      {...common(props, [
        'OsasuoritusPropertyLabel',
        `OsasuoritusPropertyLabel--level-${level}`
      ])}
    >
      {props.children}
    </Column>
  )
}

export type OsasuoritusPropertyValueProps = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyValue: React.FC<
  OsasuoritusPropertyValueProps
> = (props) => {
  const level = useContext(PropertyLevelContext)
  return (
    <Column
      row={props.row || 0}
      {...common(props, [
        'OsasuoritusPropertyValue',
        `OsasuoritusPropertyValue--level-${level}`
      ])}
    >
      {props.children}
    </Column>
  )
}
