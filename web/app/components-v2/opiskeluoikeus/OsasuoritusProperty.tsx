import React, { createContext, useContext } from 'react'
import { useLayout } from '../../util/useDepth'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Column, ColumnRow, COLUMN_COUNT } from '../containers/Columns'
import {
  AmmatillinenTyyliContext,
  OSASUORITUSTABLE_DEPTH_KEY
} from './OsasuoritusTable'
import { t } from '../../i18n/i18n'

const LABEL_WIDTH_COLUMNS = 4

/**
 * Kertoo arvosarakkeelle, että ympäröivä OsasuoritusProperty on jo varannut
 * nimisarakkeen siirtämällä asettelukontekstia LABEL_WIDTH_COLUMNS:n verran.
 * Ilman tätä tietoa OsasuoritusPropertyValue varaa nimisarakkeen toistamiseen,
 * jolloin nimen ja arvon väliin jää neljä tyhjää saraketta (mitattuna 219 px).
 *
 * OsasuoritusSubproperty nollaa lipun, koska se renderöi oman nimiönsä samaan
 * ruudukkoon: siellä arvon kuuluukin alkaa vasta nimisarakkeen jälkeen.
 */
const NimisarakeVarattuContext = createContext(false)

export type OsasuoritusPropertyProps = CommonPropsWithChildren<{
  label: string
}>

export const OsasuoritusProperty: React.FC<OsasuoritusPropertyProps> = (
  props
) => {
  const [indentation, LayoutProvider] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const ammatillinenTyyli = useContext(AmmatillinenTyyliContext)
  return (
    <ColumnRow
      {...common(props, [
        'OsasuoritusProperty',
        ammatillinenTyyli && 'OsasuoritusProperty--ammatillinen'
      ])}
      valign="top"
      indent={indentation}
    >
      <OsasuoritusPropertyLabel>{t(props.label)}</OsasuoritusPropertyLabel>
      <LayoutProvider indent={LABEL_WIDTH_COLUMNS}>
        <NimisarakeVarattuContext.Provider value={true}>
          {props.children}
        </NimisarakeVarattuContext.Provider>
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
  return (
    <NimisarakeVarattuContext.Provider value={false}>
      <OsasuoritusPropertyLabel row={props.rowNumber}>
        {t(props.label)}
      </OsasuoritusPropertyLabel>
      <OsasuoritusPropertyValue row={props.rowNumber}>
        {props.children}
      </OsasuoritusPropertyValue>
    </NimisarakeVarattuContext.Provider>
  )
}

export type OsasuoritusPropertyLabel = CommonPropsWithChildren<{
  row?: number
}>

export const OsasuoritusPropertyLabel: React.FC<OsasuoritusPropertyLabel> = (
  props
) => {
  const [indentation] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  return (
    <Column
      row={props.row || 0}
      start={indentation}
      span={LABEL_WIDTH_COLUMNS}
      {...common(props, ['OsasuoritusPropertyLabel'])}
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
  const [indentation] = useLayout(OSASUORITUSTABLE_DEPTH_KEY)
  const ammatillinenTyyli = useContext(AmmatillinenTyyliContext)
  const nimisarakeVarattu = useContext(NimisarakeVarattuContext)
  // Rajataan korjaus toistaiseksi ammatilliseen; muissa koulutusmuodoissa rako
  // säilyy ennallaan, koska niiden asettelua ei ole pyydetty muuttamaan.
  const nimisarakeOffset =
    nimisarakeVarattu && ammatillinenTyyli ? 0 : LABEL_WIDTH_COLUMNS
  const span = COLUMN_COUNT - indentation - nimisarakeOffset - 1

  return (
    <Column
      row={props.row || 0}
      start={indentation + nimisarakeOffset}
      span={span}
      {...common(props, ['OsasuoritusPropertyValue'])}
    >
      {props.children}
    </Column>
  )
}
