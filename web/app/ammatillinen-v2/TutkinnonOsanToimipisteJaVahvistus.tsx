import React from 'react'
import { TestIdText } from '../appstate/useTestId'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { ISO2FinnishDate } from '../date/date'
import { t } from '../i18n/i18n'
import { HenkilövahvistusValinnaisellaTittelillä } from '../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaTittelilla'
import { OrganisaatioWithOid } from '../types/fi/oph/koski/schema/OrganisaatioWithOid'

type TutkinnonOsanToimipisteJaVahvistusProps = {
  toimipiste?: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

// Tutkinnon osan omat toimipiste ja vahvistus ovat valinnaisia (ne tulevat
// tyypillisesti tiedonsiirroista), joten ne näytetään vain, jos ne on annettu.
export const TutkinnonOsanToimipisteJaVahvistus: React.FC<
  TutkinnonOsanToimipisteJaVahvistusProps
> = ({ toimipiste, vahvistus }) => (
  <>
    {toimipiste && (
      <OsasuoritusProperty label="Oppilaitos / toimipiste">
        <OsasuoritusPropertyValue>
          <TestIdText id="toimipiste">{t(toimipiste.nimi)}</TestIdText>
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
    )}
    {vahvistus && (
      <OsasuoritusProperty label="Vahvistus">
        <OsasuoritusPropertyValue>
          <TestIdText id="vahvistus">
            {[
              ISO2FinnishDate(vahvistus.päivä),
              ...vahvistus.myöntäjäHenkilöt.map((h) =>
                h.titteli ? `${h.nimi}, ${t(h.titteli)}` : h.nimi
              )
            ].join(' ')}
          </TestIdText>
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
    )}
  </>
)
