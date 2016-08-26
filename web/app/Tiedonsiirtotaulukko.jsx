import React from 'react'
import fecha from 'fecha'

export const Tiedonsiirtotaulukko = React.createClass({
  render() {
    const { rivit } = this.props
    const showDataForRow = this.state && this.state.showDataForRow
    return (<div>
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
          rivit.flatMap((oppijaRivi) => {
            return oppijaRivi.rivit.map(rivi => <Lokirivi row={rivi} parent={this}/>)
          })
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
    const nimi = row.oppija && (row.oppija.kutsumanimi + ' ' + row.oppija.sukunimi)
    return (<tr>
      <td className="aika">{fecha.format(fecha.parse(row.aika, 'YYYY-MM-DDThh:mm'), 'D.M.YYYY h:mm')}</td>
      <td className="hetu">{row.oppija && row.oppija.hetu}</td>
      <td className="nimi">{
        (row.oppija && row.oppija.oid)
          ? <a href={`/koski/oppija/${row.oppija.oid}`}>{nimi}</a> : nimi
      }</td>
      <td className="oppilaitos">{row.oppilaitos && row.oppilaitos.nimi && row.oppilaitos.nimi.fi}</td>
      <td className="virhe">{row.virhe && <span>{row.virhe} (<a onClick={showData}>tiedot</a>)</span>}</td>
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