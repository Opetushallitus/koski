import React from 'react'
import Text from '../i18n/Text'
import '../style/main.less'

export default class HyvaksyntaAnnettu extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      callback: this.props.callback,
      timeout: this.props.timeout || 3000
    }
  }

  componentDidMount() {
    // this.moveToCallbackURL()
  }

  moveToCallbackURL() {
    setTimeout(() => {
      window.location.href = this.state.callback
    }, this.state.timeout)
  }

  render() {
    return (
      <div className="acceptance-success-box">
        <div className="success-container">
          <img className="acceptance-image" src="/koski/images/check_mark.svg" />
          <div className="acceptance-title-success">
            <Text name="Omadata hyväksytty otsikko"/>
          </div>
        </div>
        <div className="acceptance-control-mydata">
          <Text name="Voit hallita tietojasi"/>
        </div>
        <div className="acceptance-return-container">
          <div className="acceptance-return-automatically"><Text name="Palataan palveluntarjoajan sivulle"/></div>
          <a href={this.state.callback}>
            <div className="acceptance-return-button button"><Text name="Palaa palveluntarjoajan sivulle"/></div>
          </a>
        </div>
      </div>
    )
  }
}
