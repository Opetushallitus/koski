import React from "react"
import ReactDOM from "react-dom"

export const Oppija = ({oppija}) => oppija ?
  <div className="oppija">
    <h2>{oppija.sukunimi}, {oppija.etunimet} <span className="hetu">{oppija.hetu}</span></h2>
    <hr></hr>
  </div> :
  <div className="oppija">
  </div>
