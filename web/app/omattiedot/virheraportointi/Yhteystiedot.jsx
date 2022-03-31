import React from 'baret'
import Bacon from 'baconjs'
import {OppilaitoksenYhteystieto} from './OppilaitoksenYhteystieto'
import {modelData} from '../../editor/EditorModel'
import {CopyableText} from '../../components/CopyableText'
import Text from '../../i18n/Text'
import {VirheraporttiMessage} from './emailMessage'

const Yhteystieto = ({henkilö, yhteystieto}) => {
  const nimi = `${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`
  const oppijaOid = modelData(henkilö, 'oid')

  const messageDetails = VirheraporttiMessage.details(nimi, oppijaOid)
  const fullMessage = [
    VirheraporttiMessage.placeholder(),
    VirheraporttiMessage.spacer(),
    VirheraporttiMessage.brief(),
    messageDetails
  ].join('\n\n')

  return (
    <div>
      <OppilaitoksenYhteystieto yhteystieto={yhteystieto} message={fullMessage}/>
      <CopyableText heading={'Muista mainita sähköpostissa seuraavat tiedot:'} message={messageDetails}/>
    </div>
  )
}

Yhteystieto.displayName = 'Yhteystieto'

const EiYhteystietoa = () => <Text name={'Oppilaitokselle ei löytynyt yhteystietoja.'}/>

const MuuVirhe = () => <Text name={'httpStatus.500'}/>

const wrapAsSection = Component => {
  if(Component) {
    Component.displayName = `wrapAsSection(${Component.displayName})`
    return <div><hr/>{Component}</div>
  } else {
    return Component
  }
}

EiYhteystietoa.displayName = 'EiYhteystietoa'
MuuVirhe.displayName = 'MuuVirhe'

export const Yhteystiedot = ({henkilö, yhteystietoP, isLoadingA}) => {
  const results = Bacon.combineWith(yhteystietoP.skipErrors(), isLoadingA, (yhteystieto, loading) =>
    loading ? <div className='yhteystieto loading'><Text name='Haetaan'/></div>
      : !yhteystieto ? null
      : !yhteystieto.email ? <EiYhteystietoa/>
        : <Yhteystieto henkilö={henkilö} yhteystieto={yhteystieto}/>
  )

  const errors = yhteystietoP
    .errors()
    .mapError()
    .map(() => <MuuVirhe/>)

  return <div>{Bacon.mergeAll(results, errors).map(wrapAsSection)}</div>
}

Yhteystiedot.displayName = 'Yhteystiedot'
