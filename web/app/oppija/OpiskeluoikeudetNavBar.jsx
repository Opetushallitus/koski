import Atom from 'bacon.atom'
import React from 'baret'
import Link from '../components/Link'
import { yearFromIsoDateString } from '../date/date'
import { modelData, modelItems, modelTitle } from '../editor/EditorModel'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import { currentLocation, navigateTo } from '../util/location.js'
import { userP } from '../util/user'
import { UusiOpiskeluoikeusDialog } from '../uusiopiskeluoikeus/UusiOpiskeluoikeusDialog'
import { postNewOppija } from '../uusioppija/UusiOppija'
import { reloadOppija } from '../virkailija/VirkailijaOppijaView'

export default ({ oppijaOid, opiskeluoikeusTyypit, selectedIndex }) => {
  const addingAtom = Atom(false)
  const toggleAdd = (event) => {
    addingAtom.modify((x) => !x)
  }
  const addOpiskeluoikeus = (opiskeluoikeus) =>
    new Promise((resolve, reject) => {
      if (!opiskeluoikeus) {
        addingAtom.set(false)
      } else {
        const oppija = {
          henkilö: { oid: oppijaOid },
          opiskeluoikeudet: [opiskeluoikeus]
        }
        const tyyppi = opiskeluoikeus.tyyppi.koodiarvo
        postNewOppija(oppija)
          .doError(() => {
            reject()
          })
          .onValue(() => {
            reloadOppija()
            navigateTo('?opiskeluoikeudenTyyppi=' + tyyppi)
            resolve()
          })
      }
    })

  const canAddOpiskeluoikeusP = userP.map((u) => !!u.hasWriteAccess)
  return (
    <ul
      className="opiskeluoikeustyypit-nav"
      data-testid="opiskeluoikeustyypit-navigation"
    >
      {opiskeluoikeusTyypit.map((opiskeluoikeudenTyyppi, tyyppiIndex) => {
        const selected = tyyppiIndex === selectedIndex
        const koodiarvo = modelData(opiskeluoikeudenTyyppi).tyyppi.koodiarvo
        const className = selected ? koodiarvo + ' selected' : koodiarvo
        const content = (
          <div>
            <div className="opiskeluoikeustyyppi">
              {modelTitle(opiskeluoikeudenTyyppi, 'tyyppi')}
            </div>
            <ul className="oppilaitokset">
              {modelItems(opiskeluoikeudenTyyppi, 'opiskeluoikeudet').map(
                (oppilaitoksenOpiskeluoikeudet, oppilaitosIndex) => (
                  <li key={oppilaitosIndex}>
                    <span className="oppilaitos">
                      {modelTitle(oppilaitoksenOpiskeluoikeudet, 'oppilaitos')}
                    </span>
                    <ul className="opiskeluoikeudet">
                      {modelItems(
                        oppilaitoksenOpiskeluoikeudet,
                        'opiskeluoikeudet'
                      ).map((opiskeluoikeus, opiskeluoikeusIndex) => (
                        <li
                          className="opiskeluoikeus"
                          key={opiskeluoikeusIndex}
                        >
                          <span className="koulutus">
                            {koulutuksenNimi(opiskeluoikeus)}
                          </span>{' '}
                          {modelData(opiskeluoikeus, 'alkamispäivä') ? (
                            <span>
                              <span className="alku pvm">
                                {yearFromIsoDateString(
                                  modelTitle(opiskeluoikeus, 'alkamispäivä')
                                )}
                              </span>
                              {'—'}
                              <span className="loppu pvm">
                                {yearFromIsoDateString(
                                  modelTitle(opiskeluoikeus, 'päättymispäivä')
                                )}
                                {', '}
                              </span>
                            </span>
                          ) : null}
                          <span className="tila">
                            {modelTitle(
                              opiskeluoikeus,
                              'tila.opiskeluoikeusjaksot.-1.tila'
                            )}
                          </span>
                        </li>
                      ))}
                    </ul>
                  </li>
                )
              )}
            </ul>
          </div>
        )
        return (
          <li
            className={className}
            key={tyyppiIndex}
            data-testid={`opiskeluoikeustyyppi-${koodiarvo}`}
            data-tyyppi-index={tyyppiIndex}
            data-selected={selected}
          >
            {selected ? (
              content
            ) : (
              <Link href={'?opiskeluoikeudenTyyppi=' + koodiarvo}>
                {content}
              </Link>
            )}
          </li>
        )
      })}
      {canAddOpiskeluoikeusP.map(
        (canAdd) =>
          canAdd && (
            <li
              key="new"
              className={
                'add-opiskeluoikeus' +
                (currentLocation().params.edit ? ' disabled' : '')
              }
            >
              <span className="plus" onClick={toggleAdd}>
                {''}
              </span>
              <a onClick={toggleAdd}>
                <Text name="Lisää opiskeluoikeus" />
              </a>
              {addingAtom.map(
                (adding) =>
                  adding && (
                    <UusiOpiskeluoikeusDialog
                      onClose={() => addingAtom.set(false)}
                      onSubmit={addOpiskeluoikeus}
                    />
                  )
              )}
            </li>
          )
      )}
    </ul>
  )
}

const koulutuksenNimi = (opiskeluoikeus) => {
  const tyyppi = modelData(opiskeluoikeus, 'tyyppi.koodiarvo')
  if (tyyppi === 'taiteenperusopetus') {
    const oppimäärä = modelData(opiskeluoikeus, 'oppimäärä.koodiarvo')
    const oppimääräStr =
      t(
        {
          laajaoppimaara: 'Laajan oppimäärän suoritus',
          yleinenoppimaara: 'Yleisen oppimäärän suoritus'
        }[oppimäärä]
      ) || modelTitle(opiskeluoikeus, 'oppimäärä')

    const taiteenala = modelTitle(
      opiskeluoikeus,
      'suoritukset.0.koulutusmoduuli.taiteenala'
    ).toLowerCase()

    return `${oppimääräStr} (${taiteenala})`
  }

  if (tyyppi === 'ammatillinenkoulutus') {
    const koulutusmoduulinTunniste = modelData(
      opiskeluoikeus,
      'suoritukset.0.koulutusmoduuli.tunniste.koodiarvo'
    )
    if (
      koulutusmoduulinTunniste ===
      'ammatillinentutkintoosittainenuseastatutkinnosta'
    ) {
      return t(
        modelData(opiskeluoikeus, 'suoritukset.0.koulutusmoduuli.tunniste.nimi')
      )
    }
  }

  return modelTitle(opiskeluoikeus, 'suoritukset.0.tyyppi')
}
