import React from 'react'
import { useVirkailijaUser } from '../appstate/user'
import { naviLink } from './naviLink'

export const tiedostonTuontiPath = '/koski/tiedonsiirrot/tiedoston-tuonti'

export type TiedonsiirrotNaviProps = {
  location: string
}

export const TiedonsiirrotNavi: React.FC<TiedonsiirrotNaviProps> = ({
  location
}) => {
  const user = useVirkailijaUser()
  return (
    <nav className="sidebar tiedonsiirrot-navi">
      {naviLink(
        '/koski/tiedonsiirrot/yhteenveto',
        'Yhteenveto',
        location,
        'yhteenveto-link'
      )}
      {naviLink(
        '/koski/tiedonsiirrot',
        'Tiedonsiirtoloki',
        location,
        'tiedonsiirto-link'
      )}
      {naviLink(
        '/koski/tiedonsiirrot/virheet',
        'Virheet',
        location,
        'virheet-link'
      )}
      {user?.hasPääkäyttäjäAccess &&
        naviLink(
          tiedostonTuontiPath,
          'Tiedoston tuonti',
          location,
          'tiedoston-tuonti-link'
        )}
    </nav>
  )
}
