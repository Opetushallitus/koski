import React from "react"
import ReactDOM from "react-dom"

export const Oppija = ({oppija, oppijat}) => oppija ?
  <div className="oppija">
    <h2>{oppija.sukunimi}, {oppija.etunimet} <span className="hetu">{oppija.hetu}</span></h2>
    <hr></hr>
  </div> :
  <CreateOppija oppijat={oppijat}/>

const CreateOppija = React.createClass({
  render() {
    const {oppijat} = this.props
    const {firstNames, surname, callingName, ssn} = this.state

    const submitDisabled = !firstNames || !surname || !callingName || !ssn

    if(oppijat.query.length > 2 && oppijat.results.length === 0) {
      return (
        <form className="oppija stacked">
          <label className="first-name">
            Etunimet
            <input ref="firstNames" onInput={this.onInput}></input>
          </label>
          <label className="calling-name">
            Kutsumanimi
            <input ref="callingName" onInput={this.onInput}></input>
          </label>
          <label>
            Sukunimi
            <input ref="surname" onInput={this.onInput}></input>
          </label>
          <label className="ssn">
            Henkilötunnus
            <input ref="ssn" onInput={this.onInput}></input>
          </label>
          <button className="button blue" disabled={submitDisabled}>Lisää henkilö</button>
        </form>
      )
    } else {
      return <div></div>
    }
  },

  getInitialState() {
    return {firstNames: '', surname: '', callingName: '', ssn: ''}
  },

  formState() {
    return {
      firstNames: this.refs.firstNames.value,
      surname: this.refs.surname.value,
      callingName: this.refs.callingName.value,
      ssn: this.refs.ssn.value
    }
  },

  onInput() {
    this.setState(this.formState())
  }

})