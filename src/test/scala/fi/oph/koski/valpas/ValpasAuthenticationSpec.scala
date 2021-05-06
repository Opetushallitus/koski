package fi.oph.koski.valpas

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class ValpasAuthenticationSpec extends ValpasTestBase {
  val täysinAvoinUri = s"/api/koodisto/suoritustyypit"
  val sessionVaativaUri = s"/api/organisaatio/hierarkia"
  val virkailjanTaiPalvelukäyttäjänVaativaUri = s"/api/raportit/organisaatiot"
  val kansalaisUri = s"/api/omattiedot/editor"
  val valpasSessionVaativaUri = s"/valpas/api/user"

  "Tunnistautumaton käyttäjä" - {
    "voi kutsua" - {
      "avoimia routeja" taggedAs(ValpasBackendTag) in {
        get(täysinAvoinUri) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        get(sessionVaativaUri) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated("Käyttäjä ei ole tunnistautunut."))
        }
      }

      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        get(virkailjanTaiPalvelukäyttäjänVaativaUri) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated("Käyttäjä ei ole tunnistautunut."))
        }
      }

      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        get(kansalaisUri) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated("Käyttäjä ei ole tunnistautunut."))
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        get(valpasSessionVaativaUri) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated("Käyttäjä ei ole tunnistautunut."))
        }
      }
    }
  }

  "Vain Valpas-oikeudellinen käyttäjä" - {
    val user = ValpasMockUsers.valpasJklNormaalikoulu

    "voi kutsua" - {
      "avoimia routeja" taggedAs (ValpasBackendTag) in {
        authGet(täysinAvoinUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        authGet(sessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        authGet(valpasSessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        authGet(virkailjanTaiPalvelukäyttäjänVaativaUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman Koski-palvelun käyttöoikeuksia"))
        }
      }

      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        authGet(kansalaisUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
        }
      }
    }
  }

  "Vain Koski-oikeudellinen käyttäjä" - {
    val user = MockUsers.helsinginKaupunkiEsiopetus

    "voi kutsua" - {
      "avoimia routeja" taggedAs (ValpasBackendTag) in {
        authGet(täysinAvoinUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        authGet(sessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        authGet(virkailjanTaiPalvelukäyttäjänVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        authGet(kansalaisUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        authGet(valpasSessionVaativaUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu näillä käyttöoikeuksilla"))
        }
      }
    }
  }

  "Valpas- jas Koski-oikeudellinen käyttäjä" - {
    val user = ValpasMockUsers.valpasJklNormaalikouluJaKoskiHelsinkiTallentaja

    "voi kutsua" - {
      "avoimia routeja" taggedAs (ValpasBackendTag) in {
        authGet(täysinAvoinUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        authGet(sessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        authGet(virkailjanTaiPalvelukäyttäjänVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        authGet(valpasSessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        authGet(kansalaisUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
        }
      }
    }
  }

  "Kansalaiskäyttäjä" - {
    val oppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021
    "voi kutsua" - {
      "avoimia routeja" taggedAs (ValpasBackendTag) in {
        get(täysinAvoinUri, headers = kansalainenLoginHeaders(oppija.hetu.get)) {
          verifyResponseStatusOk()
        }
      }

      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        get(sessionVaativaUri, headers = kansalainenLoginHeaders(oppija.hetu.get)) {
          verifyResponseStatusOk()
        }
      }

      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        get(kansalaisUri, headers = kansalainenLoginHeaders(oppija.hetu.get)) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        get(virkailjanTaiPalvelukäyttäjänVaativaUri, headers = kansalainenLoginHeaders(oppija.hetu.get)) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainVirkailija("Sallittu vain virkailija-käyttäjille"))
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        get(valpasSessionVaativaUri, headers = kansalainenLoginHeaders(oppija.hetu.get)) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu näillä käyttöoikeuksilla"))
        }
      }
    }
  }

  "Valppaan pääkäyttäjä" - {
    val user = ValpasMockUsers.valpasOphPääkäyttäjä

    "voi kutsua" - {
      "avoimia routeja" taggedAs (ValpasBackendTag) in {
        authGet(täysinAvoinUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        authGet(sessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        authGet(valpasSessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        authGet(virkailjanTaiPalvelukäyttäjänVaativaUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman Koski-palvelun käyttöoikeuksia"))
        }
      }

      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        authGet(kansalaisUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
        }
      }
    }
  }

  "Kosken pääkäyttäjä" - {
    val user = MockUsers.paakayttaja

    "voi kutsua" - {
      "avoimia routeja" taggedAs (ValpasBackendTag) in {
        authGet(täysinAvoinUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "minkä tahansa session vaativia routeja" taggedAs (ValpasBackendTag) in {
        authGet(sessionVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }

      "virkailija- tai palvelukäyttäjärouteja" taggedAs (ValpasBackendTag) in {
        authGet(virkailjanTaiPalvelukäyttäjänVaativaUri, user = user) {
          verifyResponseStatusOk()
        }
      }
    }

    "ei voi kutsua" - {
      "kansalaisrouteja" taggedAs (ValpasBackendTag) in {
        authGet(kansalaisUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
        }
      }

      "Valpas-routeja" taggedAs (ValpasBackendTag) in {
        authGet(valpasSessionVaativaUri, user = user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu näillä käyttöoikeuksilla"))
        }
      }
    }
  }
}
