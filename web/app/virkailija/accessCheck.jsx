import React from 'react'
import { userP } from '../util/user'
import Text from '../i18n/Text'

export const onlyIfHasReadAccess = (content) =>
  userP
    .flatMap((user) =>
      user && user.hasAnyReadAccess
        ? content
        : { title: '', content: noAccessContent() }
    )
    .toProperty()

const noAccessContent = () => (
  <div className="content-area no-access">
    <h2>
      <Text name="Ei käyttöoikeuksia" />
    </h2>
    <p>
      <Text name="Käyttääksesi Koski-palvelua tarvitset käyttöoikeudet." />
    </p>
  </div>
)

noAccessContent.displayName = 'noAccessContent'
