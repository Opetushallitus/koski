package fi.vm.sade.utils.tcp;

class PortFromSystemPropertyOrFindFree(systemPropertyName: String) extends PortChooser {
  lazy val chosenPort = System.getProperty(systemPropertyName, PortChecker.findFreeLocalPort.toString).toInt
}
