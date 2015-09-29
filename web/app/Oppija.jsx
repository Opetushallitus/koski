import React from "react"
import ReactDOM from "react-dom"

export const Oppija = ({oppija, oppijat}) => oppija ?
  <div className="oppija">
    <h2>{oppija.sukunimi}, {oppija.etunimet} <span className="hetu">{oppija.hetu}</span></h2>
    <hr></hr>
  </div> : <CreateOppija oppijat={oppijat}/>


const CreateOppija = ({oppijat}) => {
  if(oppijat.query.length > 2 && oppijat.results.length === 0) {
    return (
      <form className="oppija stacked">
        <label className="first-name">Etunimet<input></input></label>
        <label className="calling-name">Kutsumanimi<input></input></label>
        <label>Sukunimi<input></input></label>
        <label className="ssn">Henkilötunnus<input></input></label>
        <button className="button blue">Lisää henkilö</button>
      </form>
    )
  } else {
    return <div></div>
  }
}
