import React from 'react'
import Bacon from 'baconjs'
import { tiedonsiirrotContentP } from './Tiedonsiirrot.jsx'

const inputData = {'henkilö':{'oid':'1.2.246.562.24.00000000002'},'opiskeluoikeudet':[{'suoritukset':[{'koulutusmoduuli':{'tunniste':{'koodiarvo':'357305','nimi':{'fi':'Autoalan työnjohdon erikoisammattitutkinto','sv':'Specialyrkesexamen för arbetsledning inom bilbranschen','en':'Supervisor Vehicle Tech., SQ'},'lyhytNimi':{'fi':'Autoalan työnjohdon eat','sv':'Specialyrkesexamen för arbetsledning inom bilbranschen','en':'Supervisor in Vehicle Technology, Specialist Qualification'},'koodistoUri':'koulutus','koodistoVersio':6},'perusteenDiaarinumero':'39/011/2014'},'toimipiste':{'oid':'1.2.246.562.10.52251087186','oppilaitosnumero':{'koodiarvo':'10105','nimi':{'fi':'Stadin ammattiopisto'},'lyhytNimi':{'fi':'Stadin ammattiopisto'},'koodistoUri':'oppilaitosnumero'},'nimi':{'fi':'Stadin ammattiopisto'}},'tila':{'koodiarvo':'KESKEN','nimi':{'fi':'Suoritus kesken','sv':'Prestationen icke slutförd'},'koodistoUri':'suorituksentila','koodistoVersio':1},'tyyppi':{'koodiarvo':'ammatillinentutkinto','nimi':{'fi':'Ammatillinen tutkinto'},'koodistoUri':'suorituksentyyppi','koodistoVersio':1}}],'id':64825,'versionumero':1,'oppilaitos':{'oid':'1.2.246.562.10.52251087186','oppilaitosnumero':{'koodiarvo':'10105','nimi':{'fi':'Stadin ammattiopisto'},'lyhytNimi':{'fi':'Stadin ammattiopisto'},'koodistoUri':'oppilaitosnumero'},'nimi':{'fi':'Stadin ammattiopisto'}},'koulutustoimija':{'oid':'1.2.246.562.10.346830761110','nimi':{'fi':'HELSINGIN KAUPUNKI','sv':'Helsingfors stad'}},'alkamispäivä':'2000-01-01','tavoite':{'koodiarvo':'ammatillinentutkinto','nimi':{'fi':'Ammatillinen tutkinto'},'koodistoUri':'suorituksentyyppi','koodistoVersio':1},'tila':{'opiskeluoikeusjaksot':[{'alku':'2000-01-01','tila':{'koodiarvo':'lasna','nimi':{'fi':'Läsnä'},'koodistoUri':'koskiopiskeluoikeudentila','koodistoVersio':1}}]},'tyyppi':{'koodiarvo':'ammatillinenkoulutus','nimi':{'fi':'Ammatillinen koulutus'},'lyhytNimi':{'fi':'Ammatillinen koulutus'},'koodistoUri':'opiskeluoikeudentyyppi','koodistoVersio':1}}]}
const data = [
  {
    aika: '10.8.2016 11:32',
    oppija: {hetu: '010101-123N', kutsumanimi: 'Eero', sukunimi: 'Esimerkki', oid: '1.2.246.562.24.00000000001'},
    oppilaitos: {oid: '1.2.246.562.10.14613773812', nimi: {fi: 'Jyväskylän normaalikoulu'}},
    virhe: 'Organisaatiota 1.2.246.562.100.24253545345 ei löydy organisaatiopalvelusta.',
    inputData: inputData
  }
]

export const tiedonsiirtolokiContentP = tiedonsiirrotContentP('/koski/tiedonsiirrot', Bacon.constant().map(() => <Tiedonsiirtoloki/>))

const Tiedonsiirtoloki = React.createClass({
  render() {
    const showDataForRow = this.state && this.state.showDataForRow
    return (<div>
    Viimeisimmät KOSKI-palveluun siirtyneet opiskelijatiedot
    <table>
      <thead>
        <tr>
          <th className="aika">Aika</th>
          <th className="hetu">Henkilötunnus</th>
          <th className="nimi">Nimi</th>
          <th className="oppilaitos">Oppilaitos</th>
          <th className="virhe">Virhe</th>
        </tr>
      </thead>
      <tbody>
      {
        data.map((row) => <Lokirivi row={row} parent={this}/>)
      }
      </tbody>
    </table>
    {
      showDataForRow && <LokirivinData row={showDataForRow} parent={this}/>
    }
  </div>)
  }
})

const Lokirivi = React.createClass({
  render() {
    const {row, parent} = this.props
    const showData = () => parent.setState({showDataForRow: row})
    const nimi = row.oppija.kutsumanimi + ' ' + row.oppija.sukunimi
    return (<tr>
      <td className="aika">{row.aika}</td>
      <td className="hetu">{row.oppija.hetu}</td>
      <td className="nimi">{
        row.oppija.oid
          ? <a href={`/koski/oppija/${row.oppija.oid}`}>{nimi}</a> : nimi
      }</td>
      <td className="oppilaitos">{row.oppilaitos.nimi.fi}</td>
      <td className="virhe">{row.virhe} (<a onClick={showData}>tiedot</a>)</td>
    </tr>)
  }
})

const LokirivinData = React.createClass({
  render() {
    const { row, parent } = this.props
    console.log('showing row', row)
    return (<div className="lokirividata-popup">
      <a className="close" onClick={() => parent.setState({showDataForRow: null})}>Sulje</a>
      <pre>{JSON.stringify(row.inputData, null, 2)}</pre>
    </div>)
  }
})