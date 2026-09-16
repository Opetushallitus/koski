import React from 'react'
import { TestIdText } from '../../appstate/useTestId'
import { ISO2FinnishDate } from '../../date/date'

export type PäivämääräväliViewProps = {
  alku?: string
  loppu?: string
}

/**
 * Jakson päivämäärät muodossa "alku — loppu" kuten vanhan käyttöliittymän
 * PäivämääräväliEditorissa: avoin jakso näytetään "alku —", ja jos kumpaakaan
 * päivää ei ole annettu, ei näytetä mitään.
 */
export const PäivämääräväliView: React.FC<PäivämääräväliViewProps> = ({
  alku,
  loppu
}) =>
  alku || loppu ? (
    <>
      <TestIdText id="alku">{alku && ISO2FinnishDate(alku)}</TestIdText>
      {' — '}
      <TestIdText id="loppu">{loppu && ISO2FinnishDate(loppu)}</TestIdText>
    </>
  ) : null
