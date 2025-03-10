package AnEditor
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import sun.font.TrueTypeFont
import kotlin.system.exitProcess

class AnEditor {
    var ogTermios = LibC.Termios()
    var cols = 0
    var rows = 0
    private fun ctrl(key: Int): Int{
        return key and 0x1f
    }
//    private fun isctrl(key: Int): Boolean{
//        return key and 0xf1 shr 1
//    }
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
    fun die(msg: String) {
        System.out.write("\u001B[2J".toByteArray())
        System.out.write("\u001B[H".toByteArray())
        System.out.flush()
        println(msg)
        disableRaw()
        exitProcess(1)
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
            ctrl(81)->return 1
        }
        println(c.toChar())
        return 0
    }

    private fun welcomeMessage(){
        refreshScreen()
        System.out.write("\u001B[${rows/5};${cols*3/5}H".toByteArray())
        print("Welcome to AnEditor! Press any key to start")
        System.out.write("\u001B[H".toByteArray())
        readKey()
    }

    fun getCursorPos() : Int{
        var buf = ByteArray(32)
        var i: Int = 0
        System.out.write("\u001B[6n".toByteArray())
        while (i < buf.size - 1){
            if(System.`in`.read(buf, i, 1) == -1)
                break
            if(buf[i].toInt().toChar() =='R') break
            i++
        }
        buf[i] = 0
        val str = buf.decodeToString().substring(2)
        val regex = """(\d+);(\d+)""".toRegex()
        val matchResult = regex.find(str)
        if (matchResult == null) return -1
        val (rowsStr, colsStr) = matchResult.destructured
        rows = rowsStr.toIntOrNull() ?: return -1
        cols = colsStr.toIntOrNull() ?: return -1
        readKey()
        return 0
    }
    fun refreshScreen(){
        var buf = ""
        System.out.write("\u001B[?25l".toByteArray())
        System.out.write("\u001b[H".toByteArray())
        buf = drawRows(buf)
        buf = buf.plus("\u001B[H")
        System.out.write(buf.toByteArray())
        System.out.write("\u001B[?25h".toByteArray())
    }
    fun runEditor(){
        enableRaw()
        getWindowSize()
        var status = 0
        welcomeMessage()
        while(status != 1){
            refreshScreen()
            status = processKey()
        }
        die("AnEditor exit\r")
        disableRaw()
    }
    fun getWindowSize(): Int{
        var winsize = LibC.Winsize()
        if(LibC.INSTANCE.ioctl(LibC.Constants.SYSTEM_OUT_FD, LibC.Constants.TIOCGWINSZ, winsize) == -1 || winsize.ws_col.toInt() == 0){
            System.out.write("\u001B[999C\u001B[999B".toByteArray())
            return getCursorPos()
        }else{
            cols = winsize.ws_col.toInt()
            rows = winsize.ws_row.toInt()
            return 0
        }
    }
    fun drawRows(buf: String) : String{
        getWindowSize()
        var temp = buf
        for(ys in 1..rows){
            temp = temp.plus("$ys")
            temp = temp.plus("\u001B[K")
            if(ys < rows)
                 temp = temp.plus("\r\n")
        }
        return temp
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
