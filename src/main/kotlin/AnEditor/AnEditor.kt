package AnEditor
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import sun.font.TrueTypeFont
import sun.security.krb5.internal.ktab.KeyTabInputStream
import kotlin.system.exitProcess

class AnEditor {
    var ogTermios = LibC.Termios()
    var cols = 0
    var rows = 0
    var coursor_x = 10
    var coursor_y = 10
    private fun ctrl(key: Int): Int{
        return key and 0x1f
    }
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
    private fun readKey() : Int {
        val key = System.`in`.read()
        if (key == 27)
        {
            var seq  = ByteArray(3)
            System.`in`.read(seq)
            if(seq[0].toInt() !=0 && seq[1].toInt() != 1)
            {
                if(seq[0].toInt().toChar() =='['){
                    if (seq[1].toInt().toChar() >= '0' && seq[1].toInt().toChar() <= '9')
                    {
                        if(seq[2].toInt().toChar() == '~'){
                            when(seq[1].toInt().toChar()){
                                '1'->return KEYS.HOME_KEY.key
                                '3'->return KEYS.DEL_KEY.key
                                '4'->return KEYS.END_KEY.key
                                '5'->return KEYS.PAGE_UP.key
                                '6'->return KEYS.PAGE_DOWN.key
                                '7'->return KEYS.HOME_KEY.key
                                '8'->return KEYS.END_KEY.key
                            }
                        }
                    }
                    when(seq[1].toInt().toChar()){
                        'A'->return KEYS.ARROW_UP.key
                        'B'->return KEYS.ARROW_DOWN.key
                        'C'->return KEYS.ARROW_RIGHT.key
                        'D'->return KEYS.ARROW_LEFT.key
                        'H'->return KEYS.HOME_KEY.key
                        'F'->return KEYS.END_KEY.key
                    }
                }
            }
        }
        return key
    }
    private fun processKey() : Int {
        val c = readKey()
        when (c) {
            ctrl(81)->return 1
            KEYS.ARROW_UP.key ->moveCoursor(KEYS.ARROW_UP.key)
            KEYS.ARROW_DOWN.key ->moveCoursor(KEYS.ARROW_DOWN.key)
            KEYS.ARROW_LEFT.key ->moveCoursor(KEYS.ARROW_LEFT.key)
            KEYS.ARROW_RIGHT.key ->moveCoursor(KEYS.ARROW_RIGHT.key)
            KEYS.PAGE_UP.key, KEYS.PAGE_DOWN.key ->{
                var max = rows - 1
                while(max-- != 0){
                    moveCoursor(if (c == KEYS.PAGE_UP.key) KEYS.ARROW_UP.key else KEYS.ARROW_DOWN.key)
                }
            }
            KEYS.HOME_KEY.key->coursor_x = 1
            KEYS.END_KEY.key->coursor_x = cols-1
        }
        return 0
    }

    private fun welcomeMessage(){
        refreshScreen()
        System.out.write("\u001B[${rows/5};${cols*3/5}H".toByteArray())
        print("Welcome to AnEditor! Press any key to start")
        System.out.write("\u001B[H".toByteArray())
        readKey()
    }

    fun moveCoursor(key: Int){
        when (key){
            KEYS.ARROW_DOWN.key->{
                if(coursor_y!=rows-1)
                    coursor_y++
            }
            KEYS.ARROW_UP.key->{
                if(coursor_y!=1)
                coursor_y--
            }
            KEYS.ARROW_LEFT.key->{
                if(coursor_x!=1)
                    coursor_x--
            }
            KEYS.ARROW_RIGHT.key->{
                if(coursor_x!=cols-1)
                    coursor_x++
            }
        }
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
        buf = buf.plus("\u001B[${coursor_y};${coursor_x}H")
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
            cols = winsize.ws_col.toInt() - 1
            rows = winsize.ws_row.toInt() - 1
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
