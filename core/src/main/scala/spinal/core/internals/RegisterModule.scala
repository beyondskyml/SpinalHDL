package spinal.core.internals

import spinal.core._

private[core] class RegisterModule[T <: Data](
  dataType: HardType[T],
  initValue: Option[T],
  hasReset: Boolean,
  resetActiveHigh: Boolean,
  hasClockEnable: Boolean,
  clockEnableActiveHigh: Boolean,
  clockDomainConfig: ClockDomainConfig
) extends Component {
  
  val io = new Bundle {
    val d = in(dataType())
    val q = out(dataType())
  }

  val reg = Reg(dataType())
  initValue.foreach(reg.init(_))

  val cd = ClockDomain.current

  if(hasClockEnable) {
    when(clockDomain.isClockEnableActive) {
      reg := io.d
    }
  } else {
    reg := io.d
  }

  io.q := reg
}