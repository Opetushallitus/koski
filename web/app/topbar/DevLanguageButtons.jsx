import React from 'baret'
import { lang, supportedLanguages } from '../i18n/i18n'
import http from '../util/http'

/**
 * Virkailijan kieli tulee asiointikielestä eikä sitä voi vaihtaa käyttöliittymästä. Paikallisessa
 * kehitysympäristössä kielen saa silti vaihdettua asettamalla mock-käyttäjän asiointikielen, jolloin
 * kieli ratkeaa täsmälleen samaa reittiä kuin tuotannossa. Ks. MockAsiointikieliServlet ja
 * MockDirectoryClient.
 *
 * Ei omaa ympäristötarkistusta: LocalTopBar renderöidään vain ilman virkailijan raameja, ja raamit
 * ovat päällä kaikissa palvelinympäristöissä (VirkailijaHtmlServlet), joten napit näkyvät vain
 * paikallisesti ja testeissä - samoissa ympäristöissä, joissa rajapinta on käytössä.
 * window.environment-tarkistus teki sivusta erilaisen `make run` -ajossa (local) ja CI:ssä
 * (unittest), jolloin paikallisesti nauhoitetut visual-baselinet eivät kelvanneet CI:lle.
 */
export const DevLanguageButtons = () => {
  const setAsiointikieli = (language) =>
    http
      .post('/koski/api/test/asiointikieli/' + language, {})
      .onValue(() => window.location.reload())

  return (
    <span className="dev-languages">
      {supportedLanguages.map((language) => (
        <a
          key={language}
          className={language + (language === lang ? ' selected' : '')}
          onClick={() => setAsiointikieli(language)}
        >
          {language}
        </a>
      ))}
    </span>
  )
}
