package fi.oph.koski.henkilo

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class HetuSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val VALID_HETU: Vector[String] = Vector("261125-1531","210130-5616","080278-8433","061109-011D","070696-522Y","010844-509V","160153-832M","210720-4861","160661-0129","270469-163P","150541-096C","150919-7899","280983-745S","011139-217K","200790-1310","280389-158J","150976-473T","190663-5215","130547-1169","031139-642K","160917-0696","200136-8093","110923-074N","300141-679M","020871-2150","040509-482F","140201-555T","130938-448W","280528-854V","301076-235L","230344-7593","081055-0833","130641-793C","050661-892K","170352-5684","280517-338C","260288-4719","110304-841N","020102-371K","111182-169K","060563-659D","020204-5600","020542-365X","080311-3656","011017-040C","291134-805A","170680-0834","120234-8961","150972-293Y","011058-085D","040956-067E","170896-890W","060942-6210","250304-429D","210406-002C","151166-227W","041081-534S","260860-557N","051179-380R","100686-5795","210853-894H","150593-681N","180450-5748","190420-208B","160367-579P","081041-896P","241087-120V","060928-602S","280752-420N","100858-6649","230968-086D","140860-760F","050639-1587","170744-4084","120501-326J","060110-344B","220431-506R","220721-4902","160876-6113","090171-765L","110822-874D","041171-887C","280756-783D","280846-866A","111106-841M","160913-796L","210592-658J","220470-812N","060740-3172","220166-4031","290567-412A","240894-8310","290533-5599","281125-742A","110938-3725","261166-426D","220255-333R","200378-291U","130511-6507","170249-378D","170249X378D","170249Y378D","170249W378D","170249V378D","170249U378D","061109A011D","061109B011D","061109C011D","061109D011D","061109E011D","061109F011D")
  private val SYNTHETIC_HETU: Vector[String] = Vector("300169-915F","211135-989B","220285-9391","210871-963C","070526-9862","100812-958W","081094-902K","161126-922M","280537-9487","140840-9360")
  private val INVALID_HETU: Vector[String] = Vector("111111-111A", "121212-1212", "131137-133T", "123456-7890", "EI HETU", "\u0012", "")

  private val hetuValidator = new Hetu(acceptSyntheticHetus = false)
  private val acceptSyntheticHetuValidator = new Hetu(acceptSyntheticHetus = true)

  "Hetu" - {
    "Valideja hetuja" in {
      VALID_HETU foreach {hetu =>
        hetuValidator.validate(hetu) should equal(Right(hetu))
      }
    }
    "Synteettisiä hetuja" in {
      SYNTHETIC_HETU foreach {hetu =>
        hetuValidator.validate(hetu) shouldBe a [Left[_, _]]
        acceptSyntheticHetuValidator.validate(hetu) should equal(Right(hetu))
      }
    }
    "Virheellisiä hetuja" in {
      INVALID_HETU foreach {hetu =>
        hetuValidator.validate(hetu) shouldBe a [Left[_, _]]
      }
    }
  }
}
