import React from "react"
import ReactDOM from "react-dom"
import Http from "./http"

export const Oppija = ({oppija, oppijat}) => oppija ?
  <div className="oppija">
    <h2>{oppija.sukunimi}, {oppija.etunimet} <span className="hetu">{oppija.hetu}</span></h2>
    <hr></hr>
  </div> :
  <CreateOppija oppijat={oppijat}/>

const CreateOppija = React.createClass({
  render() {
    const {oppijat} = this.props
    const {etunimet, sukunimi, kutsumanimi, hetu} = this.state

    const submitDisabled = !etunimet || !sukunimi || !kutsumanimi || !hetu

    if(oppijat.query.length > 2 && oppijat.results.length === 0) {
      return (
        <form className="oppija stacked" onInput={this.onInput}>
          <label className="etunimet">
            Etunimet
            <input ref="etunimet"></input>
          </label>
          <label className="kutsumanimi">
            Kutsumanimi
            <input ref="kutsumanimi"></input>
          </label>
          <label className="sukunimi">
            Sukunimi
            <input ref="sukunimi"></input>
          </label>
          <label className="hetu">
            Henkilötunnus
            <input ref="hetu"></input>
          </label>
          <button className="button blue" disabled={submitDisabled} onClick={this.submit}>Lisää henkilö</button>
        </form>
      )
    } else {
      return <div></div>
    }
  },

  getInitialState() {
    return {etunimet: '', sukunimi: '', kutsumanimi: '', hetu: ''}
  },

  formState() {
    return {
      etunimet: this.refs.etunimet.value,
      sukunimi: this.refs.sukunimi.value,
      kutsumanimi: this.refs.kutsumanimi.value,
      hetu: this.refs.hetu.value
    }
  },

  onInput() {
    this.setState(this.formState())
  },

  submit(e) {
    e.preventDefault()
    Http.post('/tor/api/oppija', this.formState())
  }
})