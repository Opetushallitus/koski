package fi.oph.koski.virta

import com.google.common.util.concurrent.UncheckedExecutionException
import fi.oph.koski.http.{HttpConnectionException, HttpStatusException}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class VirtaErrorSpec extends AnyFreeSpec with Matchers {
  private val timeout504 =
    HttpStatusException(504, "Connection timed out", "POST", "https://ws.tietovaranto.csc.fi/luku/OpiskelijanTiedot")

  "VirtaError.isExpectedFailure" - {
    "Suora HttpStatusException on odotettu virhe (VirtaHenkilöRepository-polku)" in {
      VirtaError.isExpectedFailure(timeout504) should equal(true)
    }

    "Guavan UncheckedExecutionExceptioniin kääritty HttpStatusException on odotettu virhe (välimuistipolku)" in {
      // Tämä on yleisin tuotannossa nähty muoto: ExpiringCache (Guava) kääräisee Virran HttpStatusExceptionin.
      VirtaError.isExpectedFailure(new UncheckedExecutionException(timeout504)) should equal(true)
    }

    "HttpConnectionException on odotettu virhe" in {
      val e = HttpConnectionException("Connection refused", "POST", "https://ws.tietovaranto.csc.fi/luku/OpiskelijanTiedot")
      VirtaError.isExpectedFailure(e) should equal(true)
    }

    "Muu poikkeus (esim. bugi omassa koodissa) ei ole odotettu virhe" in {
      VirtaError.isExpectedFailure(new NullPointerException("oops")) should equal(false)
    }

    "Käärittykään muu poikkeus ei ole odotettu virhe" in {
      VirtaError.isExpectedFailure(new RuntimeException("wrapper", new IllegalStateException("bug"))) should equal(false)
    }

    "Cause-ketjuton poikkeus ei aiheuta virhettä" in {
      VirtaError.isExpectedFailure(new RuntimeException("no cause")) should equal(false)
    }
  }
}
