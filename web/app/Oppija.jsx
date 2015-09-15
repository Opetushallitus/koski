import React from "react"
import ReactDOM from "react-dom"

export const Oppija = ({oppija}) => oppija ?
  <h2 className="oppija">{oppija.nimi} {oppija.etunimet} {oppija.sukunimi} {oppija.hetu}</h2> :
  <h2></h2>
