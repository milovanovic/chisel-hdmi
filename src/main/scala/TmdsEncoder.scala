package hdmi

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

class TmdsEncoder extends Module {
  val io = IO(new Bundle {
    val en   = Input(Bool())   // Input enable
    val ctrl = Input(UInt(2.W))
    val din  = Input(UInt(8.W))
    val dout = Output(UInt(10.W))
  })

  io.dout := 0.U
  val q_m = Wire(UInt(width = 9.W))
  val ones_din = Wire(UInt(4.W))
  val ones_q_m = Wire(UInt(4.W))
  val diff_q_m = Wire(SInt(8.W))
  val disparity = Wire(SInt(8.W))
  val disparity_reg = RegInit(0.S(8.W))

  ones_din := PopCount(io.din)

  val q_ms = Wire(Vec(9, Bool()))
  for (n <- 0 to 8)
    q_ms(n) := false.B
  q_ms(0) := io.din(0)
  when (ones_din > 4.U || (ones_din === 4.U && io.din(0) === 0.U)) {
    for (n <- 1 to 7)
      q_ms(n) := !(q_ms(n-1) ^ io.din(n))
    q_ms(8) := false.B
  } .otherwise {
    for (n <- 1 to 7)
      q_ms(n) := (q_ms(n-1) ^ io.din(n))
    q_ms(8) := true.B
  }
  q_m := q_ms.asUInt

  ones_q_m := PopCount(q_m(7, 0))
  diff_q_m := (2.S * ones_q_m) - 8.S

  when (io.en === true.B) {
    when (disparity_reg === 0.S || ones_q_m === 4.U) {
      when (q_m(8) === false.B) {
        io.dout := Cat(~q_m(8), q_m(8), ~q_m(7, 0))
        disparity := disparity_reg - diff_q_m
      } .otherwise {
        io.dout := Cat(~q_m(8), q_m(8, 0))
        disparity := disparity_reg + diff_q_m
      }
    } .otherwise {
      when ((disparity_reg > 0.S && ones_q_m > 4.U)
         || (disparity_reg < 0.S && ones_q_m < 4.U)) {
        io.dout := Cat(true.B, q_m(8), ~q_m(7, 0))
        when (q_m(8) === false.B) {
          disparity := disparity_reg - diff_q_m
        } .otherwise {
          disparity := disparity_reg - diff_q_m + 2.S
        }
      } .otherwise {
        io.dout := Cat(false.B, q_m(8, 0))
        when (q_m(8) === false.B) {
          disparity := disparity_reg + diff_q_m - 2.S
        } .otherwise {
          disparity := disparity_reg + diff_q_m
        }
      }
    }
  } 
  .otherwise {
    switch (io.ctrl) {
      is ("b00".U) { io.dout := "b1101010100".U }
      is ("b01".U) { io.dout := "b0010101011".U }
      is ("b10".U) { io.dout := "b0101010100".U }
      is ("b11".U) { io.dout := "b1010101011".U }
    }
    disparity := 0.S
  }
  disparity_reg := disparity
}

// Generate verilog
object TmdsEncoderApp extends App
{
  (new ChiselStage).execute(Array("--target-dir", "verilog/TmdsEncoder"), Seq(ChiselGeneratorAnnotation(() => new TmdsEncoder)))
}