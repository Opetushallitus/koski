import React from 'baret'
import Dropdown from '../../components/Dropdown'
import { modelData } from '../../editor/EditorModel'
import { t } from '../../i18n/i18n'
import { Infobox } from '../../components/Infobox'
import Text from '../../i18n/Text'

export const HuollettavaDropdown = ({ oppija, oppijaSelectionBus }) => {
  const kirjautunutHenkilo = modelData(oppija, 'userHenkilö')
  const valittuHenkilo = modelData(oppija, 'henkilö')
  const huollettavat = modelData(oppija, 'huollettavat')

  const options = [kirjautunutHenkilo].concat(
    huollettavat.sort(aakkosjarjestys)
  )

  return (
    options.length > 1 && (
      <div className="header__oppijanvalitsin">
        <h2 className="header__heading">
          {t('Kenen opintoja haluat tarkastella?')}
          <HuoltajaInfo />
        </h2>
        <select
          className="dropdown"
          data-testid="oppijanvalitsin-dropdown"
          value={valittuHenkilo.oid}
          onChange={(event) => {
            const selectedOption = options.find(
              (opt) => opt.oid === event.target.value
            )
            oppijaSelectionBus.push({ params: { oid: selectedOption.oid } })
          }}
        >
          {options.map(({ etunimet, sukunimi, oid }) => (
            <option key={oid} value={oid} disabled={!oid}>
              {etunimet +
                ' ' +
                sukunimi +
                (oid ? '' : ` (${t('Ei opintoja')})`)}
            </option>
          ))}
        </select>
      </div>
    )
  )
}

const HuoltajaInfo = () => (
  <Infobox>
    <Text
      name="Alaikäisten huollettavien tiedot haetaan Digi- ja väestötietovirastolta."
      className="huoltaja-info"
    />
    <br />
    <a
      href="https://www.suomi.fi/ohjeet-ja-tuki/tietoa-valtuuksista/toisen-henkilon-puolesta-asiointi"
      target="_blank"
      className="huoltaja-info"
      rel="noreferrer"
    >
      {t('Lisätietoa')}
    </a>
  </Infobox>
)

const aakkosjarjestys = (a, b) => a.etunimet.localeCompare(b.etunimet)
