import React from 'baret'
import {userP} from '../util/user'
import Http from '../util/http'
import Text from '../i18n/Text'
import AnnaHyvaksynta from './AnnaHyvaksynta'
import HyvaksyntaAnnettu from './HyvaksyntaAnnettu'
import {getBirthDate} from './luvanhallinta/LuvanHallinta'

const editorP = Http.cachedGet('/koski/api/omattiedot/editor', { errorMapper: () => undefined }).toProperty()

export default ({memberName, memberPurpose, logoutURL, onAuthorization, authorizationGiven}) => (
  <div className='acceptance-container'>
    <div className='heading'><h1><Text name='Henkilökohtaisten tietojen käyttö'/></h1></div>
    <div className='user'>
      <div className='username'>{userP.map(user => user && user.name)}</div>
      <div className='dateofbirth'> {editorP.map(s => getBirthDate(s))}</div>
    </div>

    { authorizationGiven ?
      <HyvaksyntaAnnettu logoutURL={logoutURL} /> :
      <AnnaHyvaksynta
        memberName={memberName}
        memberPurpose={memberPurpose}
        onAcceptClick={() => onAuthorization()}
        logoutURL={logoutURL} />
    }

  </div>
)
