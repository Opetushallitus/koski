import {
  COLUMN_COUNT,
  ResponsiveValue,
  mapResponsiveValue
} from '../components-v2/containers/Columns'
import { OsasuoritusTableColumn } from '../components-v2/opiskeluoikeus/OsasuoritusTable'

type OppiaineSarakkeetParams = {
  columnHeader: string
  editMode: boolean
  // Nimisarakkeen leveys ruudukkosarakkeina. Riippuu taulukon leveydestä:
  // ryhmitellyt taulukot ovat leveällä näytöllä puolikkaan levyisiä (Column span
  // 12) ja kapealla täysleveitä, joten sama pikselileveys vaatii eri span-arvon.
  nimiSpan: ResponsiveValue<number>
  showArvosana: boolean
  showLaajuus: boolean
}

// Oppiainetaulukon sarakkeet. Nimisarakkeelle annetaan kiinteä leveys ja loput
// rivistä syödään tyhjällä päätesarakkeella: ilman sitä OsasuoritusTablen
// getSpans venyttää nimisarakkeen koko vapaan tilan levyiseksi, jolloin arvosana
// valuu rivin oikeaan reunaan kauas oppiaineen nimestä. Sekä otsikko- että
// suoritusrivit lukevat leveydet tästä samasta taulukosta, joten ne pysyvät
// kohdakkain niin näyttö- kuin muokkaustilassa.
export const oppiaineSarakkeet = ({
  columnHeader,
  editMode,
  nimiSpan,
  showArvosana,
  showLaajuus
}: OppiaineSarakkeetParams): Array<OsasuoritusTableColumn<string>> => {
  // Näkymässä arvosana on lyhyt (koodiarvo tai sanallinen arvosana) ja laajuus
  // tarvitsee tilaa yksikön nimelle (esim. vuosiviikkotuntia); muokkaustilassa
  // arvosanan pudotusvalikko tarvitsee tilaa ja laajuus on pelkkä numerokenttä.
  const arvosanaSpan = editMode ? 6 : 4
  const laajuusSpan = editMode ? 2 : 5
  // OsasuoritusTable varaa aina laajennussarakkeen ja muokkaustilassa lisäksi
  // poistopainikkeen sarakkeen; loput ruudukosta jaetaan sarakkeille.
  const vapaatSarakkeet = COLUMN_COUNT - 1 - (editMode ? 1 : 0)
  // Päätesarakkeelle annetaan vähintään yksi ruudukkosarake: span 0 ei tuota
  // ruudukkoluokkaa, jolloin sarake jäisi automaattisesti mitoitetuksi ja voisi
  // rikkoa rivityksen.
  const täytesarake = mapResponsiveValue((nimi: number) =>
    Math.max(
      1,
      vapaatSarakkeet -
        nimi -
        (showArvosana ? arvosanaSpan : 0) -
        (showLaajuus ? laajuusSpan : 0)
    )
  )(nimiSpan)

  return [
    { key: columnHeader },
    ...(showArvosana ? [{ key: 'Arvosana', span: arvosanaSpan }] : []),
    ...(showLaajuus ? [{ key: 'Laajuus', span: laajuusSpan }] : []),
    { key: ' ', label: '', span: täytesarake }
  ]
}
