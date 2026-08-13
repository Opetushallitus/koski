import React, { createContext, useContext } from 'react'
import { useLayout } from '../../util/useDepth'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Column, ColumnRow } from '../containers/Columns'
import { OSASUORITUSTABLE_DEPTH_KEY } from './OsasuoritusTable'
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

/** Sisennys ruudukkosarakkeina; annetaan CSS:lle muuttujana. */
type IndentStyle = React.CSSProperties & { '--osasuoritus-indent': number }

export type OsasuoritusPropertiesProps = CommonPropsWithChildren<{
  indent: number
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
    style={{ '--osasuoritus-indent': props.indent } as IndentStyle}
  >
    {props.children}
  </section>
)

/**
 * Nimen viemä sisennys sellaiselle sisällölle, joka ei ole nimi tai arvo vaan
 * oma asettelunsa (esim. sisäkkäinen osasuoritustaulukko kentän arvossa).
 */
const LABEL_INDENT_COLUMNS = 4

export type OsasuoritusPropertyProps = CommonPropsWithChildren<{
  label: string
}>

export const OsasuoritusProperty: React.FC<OsasuoritusPropertyProps> = (
  props
) => {
  const [, LayoutProvider] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  return (
    <ColumnRow {...common(props, ['OsasuoritusProperty'])} valign="top">
      <OsasuoritusPropertyLabel>{t(props.label)}</OsasuoritusPropertyLabel>
      <LayoutProvider indent={LABEL_INDENT_COLUMNS}>
        <PropertyLevelContext.Provider value={1}>
          {props.children}
        </PropertyLevelContext.Provider>
      </LayoutProvider>
    </ColumnRow>
  )
}

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
