import React from 'react'
import Text from '../i18n/Text'
import '../style/main.less'

export default class HyvaksyntaAnnettu extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      logoutURL: this.props.logoutURL,
      timeout: 4000
    }

    this.moveToCallbackURL = this.moveToCallbackURL.bind(this)
  }

  componentDidMount() {
    setTimeout(() => this.moveToCallbackURL(), this.state.timeout)
  }

  moveToCallbackURL() {
    window.location.href = this.state.logoutURL
  }

  render() {
    return (
      <div className="acceptance-success-box">
        <div className="success-container">
          <img
            className="acceptance-image"
            src="/koski/images/check_mark.svg"
          />
          <div className="acceptance-title-success">
            <Text name="Omadata hyväksytty otsikko" />
          </div>
        </div>
        <div className="acceptance-control-mydata">
          <Text name="Voit hallita tietojasi" />
        </div>
        <div className="acceptance-return-container">
          <div className="acceptance-return-automatically">
            <Text name="Palataan palveluntarjoajan sivulle" />
          </div>
          <button
            className="acceptance-return-button koski-button"
            onClick={this.moveToCallbackURL}
          >
            <Text name="Palaa palveluntarjoajan sivulle" />
          </button>
        </div>
      </div>
    )
  }
}
