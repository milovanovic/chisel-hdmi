package hdmiscope 

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.experimental._


class Serializer extends RawModule {

    val rst      = IO(Input(Bool()))    // reset
    val pixclk   = IO(Input(Clock()))   // low speed pixel clock 1x
    val serclk   = IO(Input(Clock()))   // high speed serial clock 5x
    val endata_i = IO(Input(UInt(10.W)))
    val s_p      = IO(Output(Bool()))
    val s_n      = IO(Output(Bool()))

    val sdata    = Wire(Bool())
    val cascade1 = Wire(Bool())
    val cascade2 = Wire(Bool())


    val obuf = Module(new OBUFDS(Map("IOSTANDARD" -> fromStringToStringParam("TMDS_33"))))
    obuf.io.I := sdata
    s_p := obuf.io.O
    s_n := obuf.io.OB

    // serializer 10:1 (5:1 DDR)
    // master-slave cascaded since data width > 8
    val master = Module(new OSERDESE2(Map(
        "DATA_RATE_OQ"   -> fromStringToStringParam("DDR"),
        "DATA_RATE_TQ"   -> fromStringToStringParam("SDR"),
        "DATA_WIDTH"     -> fromIntToIntParam(10),
        "SERDES_MODE"    -> fromStringToStringParam("MASTER"),
        "TRISTATE_WIDTH" -> fromIntToIntParam(1)
    )))
    sdata := master.io.OQ
    master.io.OFB       := DontCare
    master.io.TQ        := DontCare
    master.io.TFB       := DontCare
    master.io.SHIFTOUT1 := DontCare
    master.io.SHIFTOUT2 := DontCare
    master.io.TBYTEOUT  := DontCare
    master.io.CLK       := serclk
    master.io.CLKDIV    := pixclk
    master.io.D1        := endata_i(0)
    master.io.D2        := endata_i(1)
    master.io.D3        := endata_i(2)
    master.io.D4        := endata_i(3)
    master.io.D5        := endata_i(4)
    master.io.D6        := endata_i(5)
    master.io.D7        := endata_i(6)
    master.io.D8        := endata_i(7)
    master.io.TCE       := 0.U
    master.io.OCE       := 1.U
    master.io.TBYTEIN   := 0.U
    master.io.RST       := rst
    master.io.SHIFTIN1  := cascade1
    master.io.SHIFTIN2  := cascade2
    master.io.T1        := 0.U
    master.io.T2        := 0.U
    master.io.T3        := 0.U
    master.io.T4        := 0.U

    val slave = Module(new OSERDESE2(Map(
        "DATA_RATE_OQ"   -> fromStringToStringParam("DDR"),
        "DATA_RATE_TQ"   -> fromStringToStringParam("SDR"),
        "DATA_WIDTH"     -> fromIntToIntParam(10),
        "SERDES_MODE"    -> fromStringToStringParam("SLAVE"),
        "TRISTATE_WIDTH" -> fromIntToIntParam(1)
    )))
    slave.io.OQ       := DontCare
    slave.io.OFB      := DontCare
    slave.io.TQ       := DontCare
    slave.io.TFB      := DontCare
    cascade1          := slave.io.SHIFTOUT1
    cascade2          := slave.io.SHIFTOUT2
    slave.io.TBYTEOUT := DontCare
    slave.io.CLK      := serclk
    slave.io.CLKDIV   := pixclk
    slave.io.D1       := 0.U
    slave.io.D2       := 0.U
    slave.io.D3       := endata_i(8)
    slave.io.D4       := endata_i(9)
    slave.io.D5       := 0.U
    slave.io.D6       := 0.U
    slave.io.D7       := 0.U
    slave.io.D8       := 0.U
    slave.io.TCE      := 0.U
    slave.io.OCE      := 1.U
    slave.io.TBYTEIN  := 0.U
    slave.io.RST      := rst
    slave.io.SHIFTIN1 := 0.U
    slave.io.SHIFTIN2 := 0.U
    slave.io.T1       := 0.U
    slave.io.T2       := 0.U
    slave.io.T3       := 0.U
    slave.io.T4       := 0.U
}

// Generate verilog
object SerializerApp extends App
{
  (new ChiselStage).execute(Array("--target-dir", "verilog/Serializer"), Seq(ChiselGeneratorAnnotation(() => new Serializer)))
}