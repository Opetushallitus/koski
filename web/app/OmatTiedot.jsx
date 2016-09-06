import React from 'react'
import Http from './http'

const omatTiedotP = () => Http.get('/koski/api/omattiedot').toProperty()


export const omatTiedotContentP = omatTiedotP().map(() =>
  <div className="content-area">
    <div className="main-content">
      <h1>Omat tiedot</h1>
    </div>
  </div>
)