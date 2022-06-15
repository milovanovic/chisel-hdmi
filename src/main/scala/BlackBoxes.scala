package hdmiscope 

import chisel3._
import chisel3.experimental._

object booleanToVerilogVectorParam extends (Boolean => RawParam) {
  def apply(b : Boolean) : RawParam =  if(b) RawParam("1") else RawParam("0")
}

object booleanToVerilogStringParam extends (Boolean => StringParam) {
  def apply(b : Boolean) : StringParam = if(b) StringParam("""TRUE""") else StringParam("""FALSE""")
}

// Xilinx buffer BUFG
class BUFG extends BlackBox {
  val io = IO(new Bundle {
    val O = Output(Clock())
    val I = Input(Clock())
  })
}

// Xilinx PLL PLLE2_BASE
class PLLE2_BASE(val paramsPLL: Map[String, Param]) extends BlackBox(paramsPLL)
{
    val io = IO(new Bundle {
        val rst      = Input(Bool());
        val pwrdwn   = Input(Bool());
        val clkin1   = Input(Clock());
        val clkfbin  = Input(Clock());
        val clkfbout = Output(Clock());
        val clkout0  = Output(Clock());
        val clkout1  = Output(Clock())
  })
}

// Xilinx differential buffer OBUFDS
class OBUFDS(val paramsOBUFDS: Map[String, Param]) extends BlackBox(paramsOBUFDS){
  val io = IO(new Bundle {
    val O  = Output(Bool())
    val OB = Output(Bool())
    val I  = Input(Bool())
  })
}

// Xilinx serializer OSERDESE2
class OSERDESE2(val paramsOSERDESE2: Map[String, Param]) extends BlackBox(paramsOSERDESE2)
{
    val io = IO(new Bundle {
        val OQ        = Output(Bool())
        val OFB       = Output(Bool())
        val TQ        = Output(Bool())
        val TFB       = Output(Bool())
        val SHIFTOUT1 = Output(Bool())
        val SHIFTOUT2 = Output(Bool())
        val TBYTEOUT  = Output(Bool())
        val CLK       = Input(Clock())
        val CLKDIV    = Input(Clock())
        val D1        = Input(Bool())
        val D2        = Input(Bool())
        val D3        = Input(Bool())
        val D4        = Input(Bool())
        val D5        = Input(Bool())
        val D6        = Input(Bool())
        val D7        = Input(Bool())
        val D8        = Input(Bool())
        val TCE       = Input(Bool())
        val OCE       = Input(Bool())
        val TBYTEIN   = Input(Bool())
        val RST       = Input(Bool())
        val SHIFTIN1  = Input(Bool())
        val SHIFTIN2  = Input(Bool())
        val T1        = Input(Bool())
        val T2        = Input(Bool())
        val T3        = Input(Bool())
        val T4        = Input(Bool())
  })
}