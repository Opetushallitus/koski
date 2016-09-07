import React from 'react'
import Http from './http'
import {ExistingOppija} from './Oppija.jsx'

const omatTiedotP = () => Http.get('/koski/api/editor/omattiedot').toProperty()

export const omatTiedotContentP = () => omatTiedotP().map((oppija) =>
  <div className="content-area">
    <nav className="sidebar"></nav>
    <ExistingOppija className="main-content" oppija={oppija}/>
  </div>
)