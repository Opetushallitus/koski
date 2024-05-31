package fi.vm.sade.utils.tcp

class ChooseFreePort extends PortChooser {
  lazy val chosenPort = PortChecker.findFreeLocalPort
}
