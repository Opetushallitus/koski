import React from '../util/baret'
import { PerusteDropdown } from '../suoritus/PerusteDropdown'
import Text from '../i18n/Text'

export default (options) => (
  <label className="peruste">
    <Text name="Peruste" />
    <PerusteDropdown {...options} />
  </label>
)
