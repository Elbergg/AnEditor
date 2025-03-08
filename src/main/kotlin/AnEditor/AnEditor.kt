package AnEditor
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import kotlin.system.exitProcess

class AnEditor {
    var ogTermios = LibC.Termios()
    var cols = 0
    var rows = 0
    private fun ctrl(key: Int): Int{
        return key and 0x1f
    }
    fun enableRaw() {
        val termios = LibC.Termios()
        val rc = LibC.INSTANCE.tcgetattr(LibC.Constants.SYSTEM_OUT_FD, termios)
        ogTermios =  LibC.of(termios)
        if (rc != 0) {
            exitProcess(1)
        }
        termios.c_lflag = termios.c_lflag and (LibC.Constants.ECHO or LibC.Constants.ICANON or LibC.Constants.IEXTEN or LibC.Constants.ISIG).inv()
        termios.c_iflag = termios.c_iflag and (LibC.Constants.IXON or LibC.Constants.ICRNL).inv()
        termios.c_oflag = termios.c_oflag and (LibC.Constants.OPOST).inv()
        termios.c_cc[LibC.Constants.VMIN] = 0
        termios.c_cc[LibC.Constants.VTIME] = 1

        LibC.INSTANCE.tcsetattr(LibC.Constants.SYSTEM_OUT_FD, LibC.Constants.TCSAFLUSH, termios)
    }
    fun disableRaw() {
        LibC.INSTANCE.tcsetattr(LibC.Constants.SYSTEM_OUT_FD, LibC.Constants.TCSAFLUSH, ogTermios)
    }
    private fun readKey() : Int {
        val key = System.`in`.read()
        return key
    }

    private fun processKey() : Int {
        val c = readKey()

        when (c) {
            ctrl(c)->return -1
        }
        println(c.toChar())
        return 0
    }
    fun refreshScreen(){
        System.out.write("\u001b[2J".toByteArray())
        drawRows()
        System.out.write("\u001b[H".toByteArray())
    }
    fun runEditor(){
        enableRaw()
        getWindowSize()
        var status = 0
        while(status != -1){
            refreshScreen()
            status = processKey()
        }
        disableRaw()
    }
    fun getWindowSize(): Int{
        var winsize = LibC.Winsize()
        if(LibC.INSTANCE.ioctl(LibC.Constants.SYSTEM_OUT_FD, LibC.Constants.TIOCGWINSZ, winsize) == -1 || winsize.ws_col.toInt() == 0){
            return -1
        }else{
            cols = winsize.ws_col.toInt()
            rows = winsize.ws_row.toInt()
            return 0
        }
    }
    fun drawRows(){
        for(ys in 1..rows){
            System.out.write("\u001b[$ys;1H".toByteArray())
            System.out.write("$ys\n".toByteArray())
        }
    }

    interface LibC : Library {
        object Constants {
            const val SYSTEM_OUT_FD = 0

            const val ISIG = 1
            const val ICANON = 2
            const val ECHO = 10
            const val TCSAFLUSH = 2

            const val IXON = 2000
            const val ICRNL = 400
            const val IEXTEN = 100000
            const val OPOST = 1
            const val VMIN = 6
            const val VTIME = 5
            const val TIOCGWINSZ = 0x5413
        }
        @Structure.FieldOrder("c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc")
        class Termios : Structure() {
            @JvmField var c_iflag: Int = 0
            @JvmField var c_oflag: Int = 0
            @JvmField var c_cflag: Int = 0
            @JvmField var c_lflag: Int = 0
            @JvmField var c_cc: ByteArray = ByteArray(19)

        }
        companion object {
            val INSTANCE: LibC = Native.load("c", LibC::class.java)
            fun of(t: Termios): Termios {
                val copy = Termios()
                copy.c_iflag = t.c_iflag
                copy.c_oflag = t.c_oflag
                copy.c_cflag = t.c_cflag
                copy.c_lflag = t.c_lflag
                copy.c_cc = t.c_cc.copyOf()
                return copy
            }
        }
        @Structure.FieldOrder("ws_row", "ws_col")
        class Winsize : Structure() {
            @JvmField var ws_row: Short = 0
            @JvmField var ws_col: Short = 0
        }
        fun ioctl(fd: Int, op: Int, winsize: Winsize): Int
        fun tcgetattr(fd: Int, termios: Termios): Int
        fun tcsetattr(fd: Int, optional_actions: Int, termios: Termios): Int
    }
}
