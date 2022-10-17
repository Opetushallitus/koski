import React from 'baret'
import Text from '../i18n/Text'

const textInput = (valueAtom, placeholderAtom) => (
  <input
    type="text"
    value={valueAtom.or('')}
    onChange={(e) => valueAtom.set(e.target.value)}
    placeholder={
      placeholderAtom === null || placeholderAtom === undefined
        ? ''
        : placeholderAtom.or('')
    }
  />
)

const textArea = (valueAtom) => (
  <textarea
    value={valueAtom.or('')}
    onChange={(e) => valueAtom.set(e.target.value)}
  />
)

export default ({ nimi, koodiarvo, kuvaus }) => (
  <div className="koulutusmoduuli">
    <label className="nimi">
      <Text name="Nimi" />
      {textInput(nimi)}
    </label>

    <label className="koodiarvo">
      <Text name="Koodiarvo" />
      {textInput(koodiarvo, nimi)}
    </label>

    <label className="kuvaus">
      <Text name="Kuvaus" />
      {textArea(kuvaus)}
    </label>
  </div>
)
