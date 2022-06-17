package hdmi

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

// Scaler parameters
case class TimingGeneratorParams(
    H_VIDEO     : Int = 1920,
    H_FP        : Int = 88,
    H_SYNC      : Int = 44,
    H_BP        : Int = 148,
    H_TOTAL     : Int = 2200,
    V_VIDEO     : Int = 1080,
    V_FP        : Int = 4,
    V_SYNC      : Int = 5,
    V_BP        : Int = 36,
    V_TOTAL     : Int = 1125,
    H_POL       : Int = 1,
    V_POL       : Int = 1,
    ACTIVE      : Int = 1,
    OBJECT_SIZE : Int = 16
)

class TimingGenerator (params: TimingGeneratorParams) extends Module {

    val io = IO(new Bundle {
        val hsync        = Output(Bool())
        val vsync        = Output(Bool())
        val video_active = Output(Bool())
        val pixel_x      = Output(UInt((params.OBJECT_SIZE).W))
        val pixel_y      = Output(UInt((params.OBJECT_SIZE).W))
    })

    // horizontal and vertical counters
    val hcount  = RegInit(0.U((params.OBJECT_SIZE).W))
    val vcount  = RegInit(0.U((params.OBJECT_SIZE).W))

    // pixel counters
    when (hcount === (params.H_TOTAL).U) {
        hcount := 0.U
        when (vcount === (params.V_TOTAL).U) {
            vcount := 0.U
        }
        .otherwise{
            vcount := vcount + 1.U
        }
    }
    .otherwise{
        hcount := hcount + 1.U
    }

    // generate video_active, hsync, and vsync signals based on the counters
    when ((hcount < (params.H_VIDEO).U) && (vcount < (params.V_VIDEO).U)){
        io.video_active := (params.ACTIVE).U
    }
    .otherwise {
        io.video_active := ~((params.ACTIVE).U)
    }

    when ((hcount >= (params.H_VIDEO + params.H_FP).U) && (hcount < (params.H_TOTAL - params.H_BP).U)) {
        io.hsync := (params.H_POL).U
    }
    .otherwise{
        io.hsync := ~((params.H_POL).U)
    }

    when ((vcount >= (params.V_VIDEO + params.V_FP).U) && (vcount < (params.V_TOTAL - params.V_BP).U)) {
        io.vsync := (params.V_POL).U
    }
    .otherwise{
        io.vsync := ~((params.V_POL).U)
    }

    // send pixel locations
    when (hcount < (params.H_VIDEO).U) {
        io.pixel_x := hcount
    }
    .otherwise {
        io.pixel_x := 0.U
    }

    when (vcount < (params.V_VIDEO).U) {
        io.pixel_y := vcount
    }
    .otherwise {
        io.pixel_y := 0.U
    }
}

// Generate verilog
object TimingGeneratorApp extends App
{
  (new ChiselStage).execute(Array("--target-dir", "verilog/TimingGenerator"), Seq(ChiselGeneratorAnnotation(() => new TimingGenerator(params = TimingGeneratorParams()))))
}