import React from 'baret'
import {userP} from '../../util/user'
import Text from '../../i18n/Text'

export const LuvanHallintaHeadline = ({birthday}) => (
  <div className='kayttoluvat-headline' tabIndex={0}>
    <div className='kayttoluvat-info'>
      <h1><Text name='Tietojeni käyttö'/></h1>
      <Text className='info' name={'Tällä sivulla voit tarkastella ja hallinnoida antamiasi käyttölupia tietoihisi. Lisäksi näet mitkä tahot, esim. viranomaiset, ovat katsoneet opintotietojasi.'}
      />
    </div>
    <h3 className='oppija-nimi'><span className='nimi'>{userP.map(user => user && user.name)}</span><span className='pvm'>{` s. ${birthday}`}</span></h3>
  </div>
)
