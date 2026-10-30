import React from 'react'
import { TestIdText } from '../appstate/useTestId'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { ISO2FinnishDate } from '../date/date'

// Osasuorituksen alkamispäivä tulee tiedonsiirroista, eikä vanhakaan
// käyttöliittymä tarjoa sitä muokattavaksi, joten se näytetään vain annettuna.
export const OsasuorituksenAlkamispäivä: React.FC<{
  alkamispäivä?: string
}> = ({ alkamispäivä }) =>
  alkamispäivä ? (
    <OsasuoritusProperty label="Alkamispäivä">
      <OsasuoritusPropertyValue>
        <TestIdText id="alkamispäivä">
          {ISO2FinnishDate(alkamispäivä)}
        </TestIdText>
      </OsasuoritusPropertyValue>
    </OsasuoritusProperty>
  ) : null
