import React from 'react'
import Text from '../../i18n/Text'
import {modelData, modelItems, modelLookup, modelTitle} from '../../editor/EditorModel'
import {flatMapArray} from '../../util/util'
import {suorituksenTyyppi, suoritusTitle} from '../../suoritus/Suoritus'

const suoritusId = (oppilaitos, suoritus) => [
  modelData(oppilaitos, 'oid'),
  modelData(suoritus, 'tyyppi.koodiarvo'),
  modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo')
].join('__')

const Ingressi = () => (
  <div className='suoritusjako-form__caption'>
    <Text name={
      'Luomalla jakolinkin voit näyttää suoritustietosi haluamillesi henkilöille (esimerkiksi työtä tai opiskelupaikkaa hakiessasi). ' +
      'Henkilötietoja tai muita arkaluontoisia tietoja ei jaeta.'}
    />
  </div>
)

const SelectableSuoritusList = ({opiskeluoikeudet}) => (
  <ul className='create-suoritusjako__list'>
    {
      opiskeluoikeudet.map(oppilaitoksenOpiskeluoikeudet => {
        const oppilaitos = modelLookup(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')
        const groupTitle = modelTitle(oppilaitos)
        const suoritukset = flatMapArray(
          modelItems(oppilaitoksenOpiskeluoikeudet, 'opiskeluoikeudet'),
          oo => modelItems(oo, 'suoritukset')
        )

        return [
          <li className='oppilaitos-group-header' key={groupTitle}>
            {groupTitle}
          </li>,
          suoritukset.map(s => {
            const id = suoritusId(oppilaitos, s)
            const title = suorituksenTyyppi(s) === 'perusopetuksenoppimaara'
              ? <Text name="Päättötodistus"/>
              : suoritusTitle(s)

            return (
              <li key={id}>
                <input type='checkbox' id={id} name='suoritusjako' value='newsletter'/>
                <label htmlFor={id}>{title}</label>
              </li>
            )
          })
        ]
      })
    }
  </ul>
)

const CreateNewButton = () => (
  <div className='create-suoritusjako__button'>
    <button>
      <Text name='Jaa valitsemasi opinnot'/>
    </button>
  </div>
)

const CreateSuoritusjako = ({opiskeluoikeudet}) => (
  <div className='suoritusjako-form__create-suoritusjako'>
    <h2><Text name='Valitse jaettavat suoritustiedot'/></h2>
    <div className='create-suoritusjako'>
      <SelectableSuoritusList opiskeluoikeudet={opiskeluoikeudet}/>
      <CreateNewButton/>
    </div>
  </div>
)

export const SuoritusjakoForm = ({opiskeluoikeudet}) => {
  return (
    <section className='suoritusjako-form'>
      <Ingressi/>
      <CreateSuoritusjako opiskeluoikeudet={opiskeluoikeudet}/>
    </section>
  )
}
