import React from 'react'
import {OppilaitoksenYhteystieto} from './OppilaitoksenYhteystieto'
import {modelData, modelTitle} from '../../editor/EditorModel'
import {ISO2FinnishDate} from '../../date/date'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'

const Yhteystieto = ({henkilö, yhteystieto}) => {
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
      <OppilaitoksenYhteystieto yhteystieto={yhteystieto} message={message}/>
      <CopyableText heading={'Muista mainita sähköpostissa seuraavat tiedot:'} message={message}/>
    </div>
  )
}

const EiYhteystietoa = () => <Text name={'Oppilaitokselle ei löytynyt yhteystietoja.'}/>

const MuuVirhe = () => <Text name={'httpStatus.500'}/>

export const Yhteystiedot = ({henkilö, yhteystieto}) => {
  return (
    <div>
      <hr/>

      {!yhteystieto ? <MuuVirhe/>
        : !yhteystieto.email ? <EiYhteystietoa/>
          : <Yhteystieto henkilö={henkilö} yhteystieto={yhteystieto}/>}
    </div>
  )
}
