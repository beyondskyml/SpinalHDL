package spinal.core.internals

import spinal.core._

private[core] class RegisterModule[T <: Data](
  postfix: String,
  dataType: HardType[T],
  initValue: Option[T]
) extends Component {

  val io = new Bundle {
    val d = in(dataType())
    val q = out(dataType())
  }

  // Build register features
  private val features = List(
    if (initValue.isDefined) "INIT" else "",
    if (clockDomain.hasResetSignal)       (if(clockDomain.config.resetActiveLevel       == HIGH) "ARST" else "ARST_N") else "",
    if (clockDomain.hasClockEnableSignal) (if(clockDomain.config.clockEnableActiveLevel == HIGH) "CE"   else "CE_N"  ) else ""
  ).filter(_.nonEmpty)
  private val moduleBaseName = s"REG_${features.mkString("_")}_${dataType.getBitsWidth}B"

  setDefinitionName(moduleBaseName)
        //   regModule.setName(regModule.definitionName + "_" + origName)
  //   setCompositeName(this, postfix)
  setName(this.definitionName + "_" + postfix)

  val reg = Reg(dataType())
  initValue.foreach(reg.init(_))

  if(clockDomain.hasClockEnableSignal) {
    when(clockDomain.isClockEnableActive) {
      reg := io.d
    }
  } else {
    reg := io.d
  }

  io.q := reg
}
