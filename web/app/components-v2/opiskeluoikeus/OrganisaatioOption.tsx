import React from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'

/**
 * Organisaatiovalikon vaihtoehdon näyttöasu. Lakkautetut organisaatiot
 * merkitään, koska niitä ei voi piilottaa: vanhaan suoritukseen voi olla
 * tarpeen valita sittemmin lakkautettu toimipiste.
 *
 * Palauttaa undefined aktiiviselle organisaatiolle, jolloin Select käyttää
 * vaihtoehdon tavallista labelia.
 */
export const organisaatioOptionDisplay = (org: {
  aktiivinen: boolean
  nimi: LocalizedString
}): React.ReactNode | undefined =>
  org.aktiivinen ? undefined : (
    <span className="LakkautettuOrganisaatio">
      {t(org.nimi)} {'(' + t('lakkautettu') + ')'}
    </span>
  )
