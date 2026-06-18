package fi.oph.koski.html

import fi.oph.koski.TestEnvironment
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}

class ScriptCacheBusterSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterAll {
  private var tempRoot: Option[Path] = None

  override protected def afterAll(): Unit = {
    tempRoot.foreach(path => FileUtils.deleteDirectory(path.toFile))
    super.afterAll()
  }

  "ScriptCacheBuster" - {
    "käyttää palvelinympäristössä build-versiota" in {
      resolveScriptVersion(isLocal = false, buildVersion = Some("commit")) shouldEqual "commit"
    }

    "vaatii palvelinympäristössä build-version" in {
      intercept[IllegalStateException] {
        resolveScriptVersion(isLocal = false, buildVersion = None)
      }
    }

    "käyttää lokaaliympäristössä skriptitiedoston aikaleimaa build-version sijaan" in {
      val scriptBundleName = "bundle.js"
      val (resourceBase, timestamp) = createBundle(scriptBundleName)
      resolveScriptVersion(
        isLocal = true,
        buildVersion = Some("commit"),
        resourceBase = resourceBase,
        scriptBundleName = scriptBundleName
      ) shouldEqual timestamp.toString
    }
  }

  private def resolveScriptVersion(
    isLocal: Boolean,
    buildVersion: Option[String],
    resourceBase: Path = Path.of("unset"),
    scriptBundleName: String = "unset.js"
  ): String =
    ScriptCacheBuster.resolveScriptVersion(
      isLocalDevelopmentEnvironment = isLocal,
      buildVersion = buildVersion,
      scriptBundleName = scriptBundleName,
      resourceBase = resourceBase.toString
    )

  private def createBundle(scriptBundleName: String): (Path, Long) = {
    val resourceBase = getTempRoot
    val bundlePath = resourceBase.resolve("koski").resolve("js").resolve(scriptBundleName)
    FileUtils.writeByteArrayToFile(bundlePath.toFile, Array[Byte](1))
    val timestamp = Files.getLastModifiedTime(bundlePath).toMillis
    (resourceBase, timestamp)
  }

  private def getTempRoot: Path =
    tempRoot.getOrElse {
      val path = Files.createTempDirectory("script-cache-buster-test")
      tempRoot = Some(path)
      path
    }
}
