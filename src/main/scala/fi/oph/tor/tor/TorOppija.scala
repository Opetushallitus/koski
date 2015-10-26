package fi.oph.tor.tor

import fi.oph.tor.opintooikeus.OpintoOikeus
import fi.oph.tor.oppija.Oppija

/**
 *  TOR-järjestelmän näkymä Oppijaan. Sisältää Oppijan henkilötietojen lisäksi opinto-oikeudet.
 */
case class TorOppija(henkilo: Oppija, opintoOikeudet: Seq[OpintoOikeus])
