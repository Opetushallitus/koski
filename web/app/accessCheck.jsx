import React from 'react'
import {userP} from './user'
import Text from './Text.jsx'

export const onlyIfHasReadAccess = (content) => userP.flatMap(user => user.hasAnyReadAccess ? content : { title: '', content: noAccessContent() }).toProperty()

const noAccessContent = () => (<div className='content-area no-access'>
  <h2><Text name="Ei käyttöoikeuksia"/></h2>
  <p><Text name="Käyttääksesi Koski-palvelua tarvitset käyttöoikeudet."/></p>
</div>)