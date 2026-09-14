import React from 'react'
import { mapSuccess, useApiOnce } from '../api-fetch'
import { Trans } from '../components-v2/texts/Trans'
import { ISO2FinnishDate } from '../date/date'
import { t } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'
import { isTäydellisetHenkilötiedot } from '../types/fi/oph/koski/schema/TaydellisetHenkilotiedot'
import { fetchOmatTiedotOppija, fetchUser } from '../util/koskiApi'
import OmaDataOAuth2AnnaHyvaksynta from './OmaDataOAuth2AnnaHyvaksynta'

export type OmaDataOAuth2UusiHyvaksyntaProps = {
  durationInMin: number
  clientId: string
  clientName: LocalizedString
  scope: string
  onAuthorization: () => void
  onDecline: () => void
}

const OmaDataOAuth2UusiHyvaksynta = ({
  durationInMin,
  clientId,
  clientName,
  scope,
  onAuthorization,
  onDecline
}: OmaDataOAuth2UusiHyvaksyntaProps) => {
  const user = useApiOnce(fetchUser)
  const oppija = useApiOnce(fetchOmatTiedotOppija)

  // Syntymäaika jätetään näyttämättä, jos oppijan haku epäonnistui tai henkilöltä puuttuu syntymäaika
  const birthDate = mapSuccess(oppija, ({ henkilö }) =>
    isTäydellisetHenkilötiedot(henkilö)
      ? ISO2FinnishDate(henkilö.syntymäaika)
      : undefined
  )

  return (
    <div className="acceptance-container">
      <div className="heading">
        <h1>
          <Trans>{'Henkilökohtaisten tietojen käyttö'}</Trans>
        </h1>
      </div>
      <div className="user">
        <div className="username">{mapSuccess(user, ({ name }) => name)}</div>
        <div className="dateofbirth">
          {' '}
          {birthDate && `${t('syntynyt')} ${birthDate}`}
        </div>
      </div>

      <OmaDataOAuth2AnnaHyvaksynta
        clientId={clientId}
        clientName={clientName}
        scope={scope}
        onAcceptClick={onAuthorization}
        onDeclineClick={onDecline}
        durationInMin={durationInMin}
      />
    </div>
  )
}

export default OmaDataOAuth2UusiHyvaksynta
