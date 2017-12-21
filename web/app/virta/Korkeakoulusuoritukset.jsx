import React from 'react'
import R from 'ramda'
import {Editor} from '../editor/Editor'
import {addContext, modelData, modelItems, modelLookup, modelSetValue} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {Suoritustaulukko} from '../suoritus/Suoritustaulukko'

const Korkeakoulusuoritukset = ({opiskeluoikeus}) => {
  const suoritukset = modelItems(opiskeluoikeus, 'suoritukset')
  const [tutkinnot, opintojaksot] = R.partition(s => modelData(s, 'tyyppi').koodiarvo === 'korkeakoulututkinto', suoritukset)
  const modelWithoutTutkinnot = modelSetValue(opiskeluoikeus, opintojaksot, 'suoritukset')

  const hasTutkintoja = tutkinnot.length > 0
  const hasOpintojaksoja = opintojaksot.length > 0

  return (
    <div className='suoritukset'>
      {tutkinnot.map((t, i) => <Editor key={i} model={t} alwaysUpdate='true'/>)}

      {hasOpintojaksoja && (
        <div>
          {hasTutkintoja && <h4><Text name='Opintojaksot'/></h4>}
          <IrrallisetOpintojaksot opiskeluoikeus={modelWithoutTutkinnot}/>
        </div>
      )}
    </div>
  )
}

const IrrallisetOpintojaksot = ({opiskeluoikeus}) => {
  const model = addContext(opiskeluoikeus, {suoritus: opiskeluoikeus})
  return <Suoritustaulukko suorituksetModel={modelLookup(model, 'suoritukset')}/>
}

export {Korkeakoulusuoritukset}
