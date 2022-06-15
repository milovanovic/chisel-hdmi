package hdmiscope 

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._

class RGB2tmds extends RawModule {

    // reset and clocks
    val rst         = IO(Input(Bool()))    // reset
    val pixelclock  = IO(Input(Clock()))   // low speed pixel clock 1x
    val serialclock = IO(Input(Clock()))   // high speed serial clock 5x

    // video signals
    val video_data   = IO(Input(UInt(24.W)))
    val video_active = IO(Input(Bool()))
    val hsync        = IO(Input(Bool()))
    val vsync        = IO(Input(Bool()))

    // tmds output ports
    val clk_p  = IO(Output(Bool()))
    val clk_n  = IO(Output(Bool()))
    val data_p = IO(Output(UInt(3.W)))
    val data_n = IO(Output(UInt(3.W)))


    val enred   = Wire(UInt(10.W))
    val engreen = Wire(UInt(10.W))
    val enblue  = Wire(UInt(10.W))
    val sync    = Wire(UInt(2.W))

    sync := Cat(vsync, hsync)

    // tmds encoder
    withClockAndReset(pixelclock, rst){
         val tb = Module(new TmdsEncoder)
        tb.io.en   := video_active
        tb.io.ctrl := sync 
        tb.io.din  := video_data(7,0)
        enblue  := tb.io.dout

        val tr = Module(new TmdsEncoder)
        tr.io.en   := video_active
        tr.io.ctrl := 0.U 
        tr.io.din  := video_data(23,16)
        enred  := tr.io.dout

        val tg = Module(new TmdsEncoder)
        tg.io.en   := video_active
        tg.io.ctrl := 0.U 
        tg.io.din  := video_data(15,8)
        engreen  := tg.io.dout
    }
   
    // tmds output serializers
    val ser_b = Module(new Serializer)
    ser_b.pixclk   := pixelclock
    ser_b.serclk   := serialclock
    ser_b.rst      := rst
    ser_b.endata_i := enblue

    val ser_g = Module(new Serializer)
    ser_g.pixclk   := pixelclock
    ser_g.serclk   := serialclock
    ser_g.rst      := rst
    ser_g.endata_i := engreen

    val ser_r = Module(new Serializer)
    ser_r.pixclk   := pixelclock
    ser_r.serclk   := serialclock
    ser_r.rst      := rst
    ser_r.endata_i := enred

    // tmds clock serializer to phase align with data signals
    val ser_c = Module(new Serializer)
    ser_c.pixclk   := pixelclock
    ser_c.serclk   := serialclock
    ser_c.rst      := rst
    ser_c.endata_i := "b1111100000".U
    clk_p := ser_c.s_p
    clk_n := ser_c.s_n


    data_p := Cat(ser_r.s_p, ser_g.s_p, ser_b.s_p)
    data_n := Cat(ser_r.s_n, ser_g.s_n, ser_b.s_n)
}

// Generate verilog
object RGB2tmdsApp extends App
{
  (new ChiselStage).execute(Array("--target-dir", "verilog/RGB2tmds"), Seq(ChiselGeneratorAnnotation(() => new RGB2tmds)))
}