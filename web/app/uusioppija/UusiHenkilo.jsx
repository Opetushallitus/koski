import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import Http from '../http'
import {elementWithLoadingIndicator} from '../AjaxLoadingIndicator.jsx'

export default ({ hetu, oid, henkilöAtom, henkilöValidAtom, henkilöErrorsAtom }) => {
  const etunimetAtom = henkilöAtom.view('etunimet')
  const kutsumanimiAtom = henkilöAtom.view('kutsumanimi')
  const sukunimiAtom = henkilöAtom.view('sukunimi')

  const validKutsumanimiP = Bacon.combineWith(kutsumanimiAtom, etunimetAtom, (kutsumanimi, etunimet) => kutsumanimi && etunimet ? etunimet.split(' ').indexOf(kutsumanimi) > -1 || etunimet.split('-').indexOf(kutsumanimi) > -1: true)
  const kutsumanimiClassNameP = validKutsumanimiP.map(valid => valid ? 'kutsumanimi' : 'kutsumanimi error')

  const henkilöValidP = etunimetAtom.and(sukunimiAtom).and(kutsumanimiAtom).and(validKutsumanimiP)
  henkilöValidP.changes().onValue((valid) => henkilöValidAtom.set(valid))

  const errorsP = validKutsumanimiP.map(valid => valid ? [] : [{field: 'kutsumanimi', message: 'Kutsumanimen on oltava yksi etunimistä.'}])
  errorsP.changes().onValue((errors) => henkilöErrorsAtom.set(errors))

  const existingHenkilöP = hetu ? Http.cachedGet('/koski/api/henkilo/hetu/' + hetu).map('.0') : Http.cachedGet('/koski/api/henkilo/oid/' + oid).map('.0')
  existingHenkilöP.filter(R.identity).onValue((henkilö) => henkilöAtom.set(henkilö))

  return (
    <div className='henkilo'>
      {
        elementWithLoadingIndicator(existingHenkilöP.map(henkilö => {
            let existing = !!(henkilö && henkilö.oid)
            return (<span>
              <label className='hetu'>
                Henkilötunnus
                <span className='value'>{hetu}</span>
              </label>
              <label className='etunimet'>
                Etunimet
                <InputOrValue existing={existing} atom={etunimetAtom}/>
              </label>
              <label className={kutsumanimiClassNameP}>
                Kutsumanimi
                <InputOrValue existing={existing} atom={kutsumanimiAtom}/>
              </label>
              <label className='sukunimi'>
                Sukunimi
                <InputOrValue existing={existing} atom={sukunimiAtom}/>
              </label>
            </span>)
          }), 'Ladataan...'
        )
      }
    </div>
  )
}

const InputOrValue = ({ existing, atom }) => existing
  ? <input type="text" disabled value={ atom.or('') }></input>
  : <input type="text" value={ atom.or('') } onChange={ (e) => atom.set(e.target.value) }></input>