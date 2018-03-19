import React from 'baret'
import Atom from 'bacon.atom'
import Http from '../../util/http'
import {PuuttuvatTiedot} from './PuuttuvatTiedot'
import Text from '../../i18n/Text'
import {t} from '../../i18n/i18n'
import {modelData} from '../../editor/EditorModel'
import {ift} from '../../util/util'

const OppilaitosOption = ({oppilaitos, selectedOppilaitosA}) => (
  <li className='oppilaitos-options__option'>
    <input
      type='radio'
      name='oppilaitos'
      id={oppilaitos.oid}
      value={oppilaitos.oid}
      checked={selectedOppilaitosA.map(oid => oppilaitos.oid === oid)}
      onChange={v => selectedOppilaitosA.set(v.target.value)}
    />

    <label htmlFor={oppilaitos.oid}>
      {t(oppilaitos.nimi)}
    </label>
  </li>
)

const Yhteystiedot = ({yhteystiedot}) => (
  <div>
    {yhteystiedot.email}
  </div>
)


export const RaportoiVirheestäForm = ({opiskeluoikeudet}) => {
  const hasAcceptedDisclaimer = Atom(false)
  const selectedOppilaitosA = Atom()

  const oppilaitokset = opiskeluoikeudet.map(o => modelData(o, 'oppilaitos'))

  const yhteystiedotP = selectedOppilaitosA
    .filter(v => !!v)
    .flatMap(oid => Http.cachedGet(
      `/koski/api/organisaatio/sahkoposti-virheiden-raportointiin?organisaatio=${oid}`, {
        errorMapper: e => {
          if (e.httpStatus === 404) return ({email: 'not found'})
          else return ({email: 'error'})
        }
      }
    ))
    .toProperty()

  return (
    <div className='raportoi-virheestä-form'>
      <div className='puuttuvat-tiedot form-section' data-indent={0}>
        <PuuttuvatTiedot/>

        <input
          type='checkbox'
          id='puuttuvat-tiedot__checkbox'
          onChange={event => hasAcceptedDisclaimer.set(event.target.checked)}
        />
        <label htmlFor='puuttuvat-tiedot__checkbox'>
          <Text name={'Opintojeni kuuluisi yllämainitun perusteella löytyä Koski-palvelusta'}/>{'*'}
        </label>
      </div>

      {ift(hasAcceptedDisclaimer, (
        <div className='oppilaitos-options form-section' data-indent={1}>
          <h3><Text name={'Voit tiedustella asiaa oppilaitokseltasi.'}/></h3>
          <ul>
            {oppilaitokset.map(o => (
              <OppilaitosOption
                oppilaitos={o}
                selectedOppilaitosA={selectedOppilaitosA}
                key={o.oid}
              />
            ))}
          </ul>

          <Yhteystiedot baret-lift yhteystiedot={yhteystiedotP}/>
        </div>
      ))}
    </div>
  )
}
