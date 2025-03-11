package AnEditor
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import java.io.File
import kotlin.system.exitProcess

class AnEditor() {
    var ogTermios = LibC.Termios()
    var cols = 0
    var rows = 0
    var coursor_x = 0
    var coursor_y = 0
    var in_rows: List<String> = emptyList()
    var num_rows = 0
    val writer = EditorWriter(this)
    val procceser = EditorKeyProcceser(this)
    val io = EditorIO(this)
    var rowOffset = 0
    var lineNumOffset = 0
    var co
    enum class KEYS(val key: Int){
        ARROW_LEFT(5000), ARROW_RIGHT(5001), ARROW_UP(5002), ARROW_DOWN(5003), PAGE_UP(2000), PAGE_DOWN(2001), HOME_KEY(2002), END_KEY(2003), DEL_KEY(2004)
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
        procceser.readKey()
        return 0
    }
    fun runEditor(args: Array<String>){
        enableRaw()
        getWindowSize()
        var status = 0
        writer.refreshScreen()
        if(args.size >= 1) {
            io.open(args[0])
        }
        else{
            writer.welcomeMessage()
        }
        while(status != 1){
            getWindowSize()
            writer.refreshScreen()
            status = procceser.processKey()
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
            cols = winsize.ws_col.toInt() - 1
            rows = winsize.ws_row.toInt() - 1
            return 0
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

