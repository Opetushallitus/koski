import React from 'baret'
import Bacon from 'baconjs'

export default ({ hetu, henkilöAtom, henkilöValidAtom, henkilöErrorsAtom }) => {
  const etunimetAtom = henkilöAtom.view('etunimet')
  const kutsumanimiAtom = henkilöAtom.view('kutsumanimi')
  const sukunimiAtom = henkilöAtom.view('sukunimi')

  const validKutsumanimiP = Bacon.combineWith(kutsumanimiAtom, etunimetAtom, (kutsumanimi, etunimet) => kutsumanimi && etunimet ? etunimet.split(' ').indexOf(kutsumanimi) > -1 || etunimet.split('-').indexOf(kutsumanimi) > -1: true)
  const kutsumanimiClassNameP = validKutsumanimiP.map(valid => valid ? 'kutsumanimi' : 'kutsumanimi error')

  const henkilöValidP = etunimetAtom.and(sukunimiAtom).and(kutsumanimiAtom).and(validKutsumanimiP)
  henkilöValidP.changes().onValue((valid) => henkilöValidAtom.set(valid))

  const errorsP = validKutsumanimiP.map(valid => valid ? [] : [{field: 'kutsumanimi', message: 'Kutsumanimen on oltava yksi etunimistä.'}])
  errorsP.changes().onValue((errors) => henkilöErrorsAtom.set(errors))

  return (
    <div className='henkilo'>
      <label className='hetu'>
        Henkilötunnus
        <span className='value'>{hetu}</span>
      </label>
      <label className='etunimet'>
        Etunimet
        <Input atom={etunimetAtom}/>
      </label>
      <label className={kutsumanimiClassNameP}>
        Kutsumanimi
        <Input atom={kutsumanimiAtom}/>
      </label>
      <label className='sukunimi'>
        Sukunimi
        <Input atom={sukunimiAtom}/>
      </label>
    </div>
  )
}

const Input = ({ atom }) => <input type="text" onChange={ (e) => atom.set(e.target.value) }></input>