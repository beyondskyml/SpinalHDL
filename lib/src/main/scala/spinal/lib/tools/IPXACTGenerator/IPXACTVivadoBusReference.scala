package spinal.lib.tools.IPXACTGenerator

import IPXACT2009ScalaCases._
import IPXACT2009scalaxb._
import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba3.ahblite.AhbLite3
import spinal.lib.bus.amba3.apb.Apb3
import spinal.lib.bus.amba4.apb.Apb4
import spinal.lib.bus.amba4.axi.Axi4
import spinal.lib.bus.amba4.axilite.AxiLite4
import spinal.lib.bus.amba4.axis.Axi4Stream.Axi4StreamBundle
import spinal.lib.bus.avalon.AvalonMM
import spinal.lib.bus.bram.BRAM
import spinal.lib.com.uart.Uart
import spinal.lib.graphic.vga.Vga

object IPXACTVivadoBusReference {
  private def createLibraryRefType(busInterfaceName: String, libraryName: String = "interface", version: String = "1.0"): LibraryRefType = {
    val busTypeVendorRecord = DataRecord(Some(""), Some("spirit:vendor"), "xilinx.com")
    val busTypeLibraryRecord = DataRecord(Some(""), Some("spirit:library"), libraryName)
    val busTypeNameRecord = DataRecord(Some(""), Some("spirit:name"), busInterfaceName)
    val busTypeVersionRecord = DataRecord(Some(""), Some("spirit:version"), version)
    val busTypeAttributes = Map(
      "vendor" -> busTypeVendorRecord,
      "library" -> busTypeLibraryRecord,
      "name" -> busTypeNameRecord,
      "version" -> busTypeVersionRecord
    )
    val busLibraryRefType = LibraryRefType(busTypeAttributes)
    busLibraryRefType
  }

  private def createDirectionRecord(iMasterSlaveBus: IMasterSlave): IPXACT2009scalaxb.DataRecord[InterfaceModeOption] = {
    if (iMasterSlaveBus.isMasterInterface) {
      DataRecord(Some(""), Some("spirit:master"), Master())
    } else {
      DataRecord(Some(""), Some("spirit:slave"), Slave())
    }
  }

  def referenceReset(resetSignal: BaseType): BusInterfaceType = {
    val busName = resetSignal.name
    val busDirectionRecord = if (resetSignal.isOutput) {
      DataRecord(Some(""), Some("spirit:master"), Master())
    } else {
      DataRecord(Some(""), Some("spirit:slave"), Slave())
    }
    var portSeqMap: Seq[PortMap] = Seq()
    val signalPhysicalName = busName
    val signalLogicalName = "RST"
    val physicalPort = PhysicalPort(signalPhysicalName)
    val logicalPort = LogicalPort(signalLogicalName)
    val portMap = PortMap(logicalPort, physicalPort)
    portSeqMap = portSeqMap :+ portMap
    val portMaps = PortMaps(portSeqMap)
    val busType = createLibraryRefType(busInterfaceName = "reset", libraryName = "signal")
    val abstractionType = createLibraryRefType(busInterfaceName = "reset_rtl", libraryName = "signal")
    val busNameGroupSequence = NameGroupSequence(busName)
    BusInterfaceType(busNameGroupSequence, busType, Some(abstractionType), busDirectionRecord, portMaps = Some(portMaps))
  }

  def referenceClock(clock: ClockDomain, busClockMap: Map[String, String]): BusInterfaceType = {
    val clockSignal = clock.clock
    var parametersSeq: Seq[NameValuePairTypable] = Seq()
    if (clock.reset != null) {
      val resetNameGroup = NameGroupStringSequence("ASSOCIATED_RESET")
      val resetValueRecord = DataRecord(Some(""), Some("spirit:id"), "BUSIFPARAM_VALUE.CLK.ASSOCIATED_RESET")
      val resetAbstractionAttributes = Map("id" -> resetValueRecord)
      val resetValue = Value(value = clock.reset.name, attributes = resetAbstractionAttributes)
      val resetNameValue = NameValueTypeType(resetNameGroup, resetValue)
      parametersSeq = parametersSeq :+ resetNameValue
    }
    var busName = ""
    for (element <- busClockMap) {
      if (element._2 == clockSignal.name) {
        busName = busName + ":" + element._1
      }
    }
    if (busName != "") {
      busName = busName.stripPrefix(":")
      val busNameGroup = NameGroupStringSequence("ASSOCIATED_BUSIF")
      val busValueRecord = DataRecord(Some(""), Some("spiritid"), "BUSIFPARAM_VALUE.CLK.ASSOCIATED_BUSIF")
      val busAbstractionAttributes = Map("id" -> busValueRecord)
      val busValue = Value(value = busName, attributes = busAbstractionAttributes)
      val busNameValue = NameValueTypeType(busNameGroup, busValue)
      parametersSeq = parametersSeq :+ busNameValue
    }
    val parameters = Parameters(parametersSeq)
    val busDirectionRecord = if (clockSignal.isOutput) {
      DataRecord(Some(""), Some("spirit:master"), Master())
    } else {
      DataRecord(Some(""), Some("spirit:slave"), Slave())
    }
    var portSeqMap: Seq[PortMap] = Seq()
    val signalPhysicalName = clockSignal.name
    val signalLogicalName = "CLK"
    val physicalPort = PhysicalPort(signalPhysicalName)
    val logicalPort = LogicalPort(signalLogicalName)
    val portMap = PortMap(logicalPort, physicalPort)
    portSeqMap = portSeqMap :+ portMap
    val portMaps = PortMaps(portSeqMap)
    val busType = createLibraryRefType(busInterfaceName = "clock", libraryName = "signal")
    val abstractionType = createLibraryRefType(busInterfaceName = "clock_rtl", libraryName = "signal")
    val busNameGroupSequence = NameGroupSequence(busName)
    BusInterfaceType(busNameGroupSequence, busType, Some(abstractionType), busDirectionRecord, portMaps = Some(portMaps), parameters = Some(parameters))
  }

  def referenceMatchedBus[T <: IMasterSlave with Data](bus: T): BusInterfaceType = {
    val busName = bus.getClass.getSimpleName + "_" + bus.name
    val busChildren = bus.flatten
    val busDirectionRecord = createDirectionRecord(bus)
    val (portSeqMap, vivadoDefinitionName) = bus match {
      case _: Axi4 =>
        referenceAxi4(busChildren)
      case _: AxiLite4 =>
        referenceAxiLite4(busChildren)
      case _: Vga =>
        referenceVga(busChildren)
      case _: AvalonMM =>
        referenceAvalonMM(busChildren)
      case _: AhbLite3 =>
        referenceAhbLite3(busChildren, busDirectionRecord)
      case _: Apb3 =>
        referenceApb3(busChildren)
      case _: Apb4 =>
        referenceApb4(busChildren)
      case _: Uart =>
        referenceUART(busChildren)
      case _: BRAM =>
        referenceBRAM(busChildren)
      case _ =>
        (Seq(), "")
    }
    val portMaps = PortMaps(portSeqMap)
    val busType = createLibraryRefType(vivadoDefinitionName)
    val abstractionType = createLibraryRefType(vivadoDefinitionName + "_rtl")
    val busNameGroupSequence = NameGroupSequence(busName)
    BusInterfaceType(busNameGroupSequence, busType, Some(abstractionType), busDirectionRecord, portMaps = Some(portMaps))
  }


  private def referenceAvalonMM(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      var portMapCanAdd = true
      if (signalPhysicalName.split("_").last == "debugAccess") {
        portMapCanAdd = false
      }
      val signalLogicalName = if (signalPhysicalName.split("_").last == "waitRequestn") {
        "WAITREQUEST"
      } else {
        signalPhysicalName.split("_").last.toUpperCase
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      if (portMapCanAdd) {
        portSeqMap = portSeqMap :+ portMap
      }
    }
    (portSeqMap, "avalon")

  }

  private def referenceAhbLite3(busChildren: Seq[BaseType], busDirectionRecord: DataRecord[InterfaceModeOption]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      var portMapCanAdd = true
      val lastWord = signalPhysicalName.split("_").last
      if (lastWord == "HREADY" || (lastWord == "HSEL" && busDirectionRecord.value == Master())) {
        portMapCanAdd = false
      }

      val signalLogicalName = if (lastWord == "HREADYOUT") {
        "HREADY"
      } else if (lastWord == "HSEL") {
        "SEL"
      } else {
        lastWord
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      if (portMapCanAdd) {
        portSeqMap = portSeqMap :+ portMap
      }
    }
    (portSeqMap, "ahblite")

  }


  private def referenceApb3(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {

    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val signalLogicalName = if (signalPhysicalName.split("_").last == "PSLVERROR") {
        "PSLVERR"
      } else {
        signalPhysicalName.split("_").last
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "apb")

  }

  private def referenceApb4(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val signalLogicalName = signalPhysicalName.split("_").last
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "apb")

  }

  private def referenceVga(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val lastWord = signalPhysicalName.split("_").last
      val signalLogicalName = if (lastWord == "r") {
        "RED"
      } else if (lastWord == "g") {
        "GREEN"
      } else if (lastWord == "b") {
        "BLUE"
      } else if (lastWord == "colorEn") {
        "DE"
      } else {
        lastWord.toUpperCase
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "vga")
  }

  private def referenceUART(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {

    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val lastWord = signalPhysicalName.split("_").last
      val signalLogicalName = if (lastWord == "cts") {
        "CTSn"
      } else if (lastWord == "rts") {
        "RTSn"
      } else if (lastWord == "rxd") {
        "RxD"
      } else if (lastWord == "txd") {
        "TxD"
      } else {
        lastWord.toUpperCase
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "uart")
  }

  private def referenceAxi4(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val parts = signalPhysicalName.split("_").takeRight(3)
      val thirdLast = parts(0)
      val secondLast = parts(1)
      val last = parts(2)
      val signalLogicalName = if (last == "ready" || last == "valid") {
        (secondLast + last).toUpperCase
      } else {
        (thirdLast + last).toUpperCase
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "aximm")
  }

  private def referenceAxiLite4(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val parts = signalPhysicalName.split("_").takeRight(3)
      val thirdLast = parts(0)
      val secondLast = parts(1)
      val last = parts(2)
      val signalLogicalName = if (last == "ready" || last == "valid") {
        (secondLast + last).toUpperCase
      } else {
        (thirdLast + last).toUpperCase
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "aximm")
  }

  //  def referenceNormalFlow(flowSignal:Flow[_]):BusInterfaceType={
  //    val payloadClassName=flowSignal.payload.getClass.getSimpleName
  //    val busName =s"Stream_${payloadClassName}"+"_"+ flowSignal.name
  //    val busChildren = flowSignal.flatten
  //    val busDirectionRecord =getDirectionRecord(flowSignal)
  //    var portSeqMap: Seq[PortMap] = Seq()
  //    for (signal <- busChildren) {
  //      val signalPhysicalName = signal.name
  //      val physicalPort = PhysicalPort(signalPhysicalName)
  //      val lastWord=signalPhysicalName.split("_").last
  //      val signalLogicalName = if(lastWord=="payload"){
  //        "TDATA"
  //      }else{
  //        "T" + signalPhysicalName.split("_").last.toUpperCase
  //      }
  //      val logicalPort = LogicalPort(signalLogicalName)
  //      val portMap = PortMap(logicalPort, physicalPort)
  //      portSeqMap = portSeqMap :+ portMap
  //    }
  //    val portMaps = PortMaps(portSeqMap)
  //    val (busType, abstractionType) = createBusTypeAndAbstractionType("axis")
  //    val busNameGroupSequence = NameGroupSequence(busName)
  //    BusInterfaceType(busNameGroupSequence, busType, Some(abstractionType), busDirectionRecord, portMaps = Some(portMaps))
  //  }
  //
  //  def referenceNormalStream(streamSignal: Stream[_]): BusInterfaceType = {
  //    val payloadClassName = streamSignal.payload.getClass.getSimpleName
  //    val busName = s"Stream_$payloadClassName" + "_" + streamSignal.name
  //    val busChildren = streamSignal.flatten
  //    val busDirectionRecord = getDirectionRecord(streamSignal)
  //    var portSeqMap: Seq[PortMap] = Seq()
  //    for (signal <- busChildren) {
  //      val signalPhysicalName = signal.name
  //      val physicalPort = PhysicalPort(signalPhysicalName)
  //      val lastWord = signalPhysicalName.split("_").last
  //      val signalLogicalName = if (lastWord == "payload") {
  //        "TDATA"
  //      } else {
  //        "T" + signalPhysicalName.split("_").last.toUpperCase
  //      }
  //      val logicalPort = LogicalPort(signalLogicalName)
  //      val portMap = PortMap(logicalPort, physicalPort)
  //      portSeqMap = portSeqMap :+ portMap
  //    }
  //    val portMaps = PortMaps(portSeqMap)
  //    val busType=createBusDefinition("axis")
  //    val abstractionType=createBusDefinition("axis_rtl")
  //    val busNameGroupSequence = NameGroupSequence(busName)
  //    BusInterfaceType(busNameGroupSequence, busType, Some(abstractionType), busDirectionRecord, portMaps = Some(portMaps))
  //  }

  def referenceAxis4(axi4StreamBundle: Stream[Axi4StreamBundle]): BusInterfaceType = {
    val busName = "Axis4" + "_" + axi4StreamBundle.name
    val busChildren = axi4StreamBundle.flatten
    val busDirectionRecord = createDirectionRecord(axi4StreamBundle)
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val signalLogicalName = "T" + signalPhysicalName.split("_").last.toUpperCase
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    val portMaps = PortMaps(portSeqMap)
    val busType = createLibraryRefType("axis")
    val abstractionType = createLibraryRefType("axis_rtl")
    val busNameGroupSequence = NameGroupSequence(busName)
    BusInterfaceType(busNameGroupSequence, busType, Some(abstractionType), busDirectionRecord, portMaps = Some(portMaps))
  }

  private def referenceBRAM(busChildren: Seq[BaseType]): (Seq[PortMap], String) = {
    var portSeqMap: Seq[PortMap] = Seq()
    for (signal <- busChildren) {
      val signalPhysicalName = signal.name
      val physicalPort = PhysicalPort(signalPhysicalName)
      val lastWord = signalPhysicalName.split("_").last
      val signalLogicalName = if (lastWord == "wrdata") {
        "DIN"
      } else if (lastWord == "rddata") {
        "DOUT"
      } else {
        lastWord.toUpperCase
      }
      val logicalPort = LogicalPort(signalLogicalName)
      val portMap = PortMap(logicalPort, physicalPort)
      portSeqMap = portSeqMap :+ portMap
    }
    (portSeqMap, "bram")
  }

}
