package hdmi.hdmiscope
 
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.experimental._

// Scaler parameters
case class ClockGenParams(
    CLKIN_PERIOD : Int = 8,    // input clock period (8ns)
    CLK_MULTIPLY : Int = 8,    // multiplier
    CLK_DIVIDE   : Int = 1,    // divider
    CLKOUT0_DIV  : Int = 8,    // serial clock divider
    CLKOUT1_DIV  : Int = 40    // pixel clock divider
)

class ClockGen (params: ClockGenParams) extends RawModule {

    val clk_i  = IO(Input(Clock()))     // Input clock
    val clk0_o = IO(Output(Clock()))    // Output serial clock
    val clk1_o = IO(Output(Clock()))    // pixel clock

    val pll = Module(new PLLE2_BASE(Map(
        "clkin1_period"  -> fromIntToIntParam(params.CLKIN_PERIOD),
        "clkfbout_mult"  -> fromIntToIntParam(params.CLK_MULTIPLY),
        "clkout0_divide" -> fromIntToIntParam(params.CLKOUT0_DIV),
        "clkout1_divide" -> fromIntToIntParam(params.CLKOUT1_DIV),
        "divclk_divide"  -> fromIntToIntParam(params.CLK_DIVIDE)
    )))
    pll.io.rst      := 0.U
    pll.io.pwrdwn   := 0.U
    pll.io.clkin1   := clk_i
    pll.io.clkfbin  := pll.io.clkfbout

    val clk0buf = Module(new BUFG)
    clk0buf.io.I := pll.io.clkout0
    clk0_o    := clk0buf.io.O

    val clk1buf = Module(new BUFG)
    clk1buf.io.I := pll.io.clkout1
    clk1_o    := clk1buf.io.O
}

// Generate verilog
object ClockGenApp extends App
{
  (new ChiselStage).execute(Array("--target-dir", "verilog/ClockGen"), Seq(ChiselGeneratorAnnotation(() => new ClockGen(params = ClockGenParams()))))
}