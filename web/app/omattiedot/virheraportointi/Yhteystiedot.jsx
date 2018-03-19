import React from 'react'
import {OppilaitoksenYhteystieto} from './OppilaitoksenYhteystieto'
import {modelData, modelTitle} from '../../editor/EditorModel'
import {ISO2FinnishDate} from '../../date/date'
import {CopyableText} from './CopyableText'

export const Yhteystiedot = ({henkilö, yhteystieto}) => {
  const nimi = `${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`
  const syntymäaika = ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))
  const oppijaOid = modelData(henkilö, 'oid')

  const message = [
    `Nimi: ${nimi}`,
    syntymäaika && `Syntymäaika: ${syntymäaika}`,
    `Oppijanumero (oid): ${oppijaOid}`
  ].filter(v => !!v).join('\n')

  return (
    <div>
      <hr/>

      <OppilaitoksenYhteystieto yhteystieto={yhteystieto} message={message}/>
      <CopyableText heading={'Muista mainita sähköpostissa seuraavat tiedot:'} message={message}/>
    </div>
  )
}
