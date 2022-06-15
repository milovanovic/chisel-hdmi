package hdmiscope

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

// HDMIScope parameters
case class HDMIScopeParams(
    timingGeneratorParams : TimingGeneratorParams
)

class HDMIScope(params: HDMIScopeParams) extends RawModule {
    // IOs
    val rst          = IO(Input(Bool()))     // reset
    val pixelclock   = IO(Input(Clock()))    // low speed pixel clock 1x
    val serialclock  = IO(Input(Clock()))    // high speed serial clock 5x
    val data         = IO(Input(UInt(24.W)))
    val video_active = IO(Output(Bool()))
    val pixel_x      = IO(Output(UInt((params.timingGeneratorParams.OBJECT_SIZE).W)))
    val pixel_y      = IO(Output(UInt((params.timingGeneratorParams.OBJECT_SIZE).W)))
    val ser_clk_p    = IO(Output(Bool()))    // hdmi clk_p
    val ser_clk_n    = IO(Output(Bool()))    // hdmi clk_n
    val ser_data_p   = IO(Output(UInt(3.W))) // hdmi data_p
    val ser_data_n   = IO(Output(UInt(3.W))) // hdmi data_n

    // Timing Generator
    val timeGen = withClockAndReset(pixelclock, rst) {
      Module(new TimingGenerator(params = TimingGeneratorParams()))
    }
    
    video_active  := timeGen.io.video_active
    pixel_x := timeGen.io.pixel_x
    pixel_y := timeGen.io.pixel_y

    // RGB to TMDS
    val rgb2tmds = Module(new RGB2tmds)
    rgb2tmds.rst          := rst
    rgb2tmds.pixelclock   := pixelclock
    rgb2tmds.serialclock  := serialclock
    rgb2tmds.video_data   := data
    rgb2tmds.video_active := timeGen.io.video_active
    rgb2tmds.hsync        := timeGen.io.hsync
    rgb2tmds.vsync        := timeGen.io.vsync
    ser_clk_p  := rgb2tmds.clk_p
    ser_clk_n  := rgb2tmds.clk_n
    ser_data_p := rgb2tmds.data_p
    ser_data_n := rgb2tmds.data_n
}

// Generate verilog
object HDMIScopeApp extends App
{
  val params = HDMIScopeParams(
    timingGeneratorParams = TimingGeneratorParams()
  )
  (new ChiselStage).execute(Array("--target-dir", "verilog/HDMIScope"), Seq(ChiselGeneratorAnnotation(() => new HDMIScope(params))))
}