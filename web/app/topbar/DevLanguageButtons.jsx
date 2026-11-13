import React from '../util/baret'
import { lang, supportedLanguages } from '../i18n/i18n'
import http from '../util/http'

/**
 * Virkailijan kieli tulee asiointikielestä eikä sitä voi vaihtaa käyttöliittymästä. Paikallisessa
 * kehitysympäristössä kielen saa silti vaihdettua asettamalla mock-käyttäjän asiointikielen, jolloin
 * kieli ratkeaa täsmälleen samaa reittiä kuin tuotannossa. Ks. MockAsiointikieliServlet ja
 * MockDirectoryClient.
 *
 * Napit näytetään vain siellä, missä rajapinta toimii: se on mountattu vain paikallisesti ja
 * testeissä, ja se vaatii kirjautumisen. Raamien puuttuminen ei riitä ehdoksi, koska LocalTopBar
 * näkyy palvelinympäristöissäkin kirjautumattomalle dokumentaatiosivulla ja kansalaisen sivujen
 * virhesivuilla (esim. OmaData-OAuth2). Ympäristöehto hyväksyy sekä `make run` -ajon (local) että
 * CI:n (unittest), jotta paikallisesti nauhoitetut visual-baselinet kelpaavat CI:lle.
 */
const devEnvironments = ['local', 'unittest']

const setAsiointikieli = (language) =>
  http
    .post('/koski/api/test/asiointikieli/' + language, {})
    .onValue(() => window.location.reload())

export const DevLanguageButtons = ({ user }) =>
  user && devEnvironments.includes(window.environment) ? (
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
  ) : null
