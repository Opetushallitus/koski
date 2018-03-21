import React from 'baret'
import Bacon from 'baconjs'
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

const wrapAsSection = Component => Component ? <div><hr/>{Component}</div> : Component

export const Yhteystiedot = ({henkilö, yhteystietoP}) => {
  const values = yhteystietoP
    .skipErrors()
    .map(yhteystieto => !yhteystieto ? null
      : !yhteystieto.email ? <EiYhteystietoa/>
        : <Yhteystieto henkilö={henkilö} yhteystieto={yhteystieto}/>)

  const errors = yhteystietoP
    .errors()
    .mapError()
    .map(() => <MuuVirhe/>)

  return <div>{Bacon.mergeAll(values, errors).map(wrapAsSection)}</div>
}
