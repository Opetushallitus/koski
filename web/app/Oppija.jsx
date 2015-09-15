import React from "react"
import ReactDOM from "react-dom"

export const Oppija = ({oppija}) => oppija ?
  <div>{oppija.nimi} {oppija.etunimet} {oppija.sukunimi} {oppija.hetu}</div> :
  <div></div>
