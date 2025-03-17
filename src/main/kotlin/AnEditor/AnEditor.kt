package AnEditor
import kotlin.system.exitProcess

class AnEditor {
    var ogTermios = EditorTerminal.LibC.Termios()
    var cols = 0
    var rows = 0
    var cursor_x = 0
    var cursor_y = 0
    var in_rows: MutableList<String> = mutableListOf()
    var renders: MutableList<String> = mutableListOf()
    var num_rows = 0
    val gui = EditorGUI(this)
    val procceser = EditorKeyProcceser(this)
    val io = EditorIO(this)
    val writer = EditorWriter(this)
    val rowmng = EditorRowManager(this)
    val terminal = EditorTerminal(this)
    val cursor = EditorCursor(this)
    var rowOffset = 0
    var lineNumOffset = 0
    var colOffset = 0
    var render_x = 0
    var fileName = ""
    var statusMsg = ""
    var statusMsg_time = 0
    var notSaved = false

    fun die(msg: String) {
        System.out.write("\u001B[2J".toByteArray())
        System.out.write("\u001B[H".toByteArray())
        System.out.flush()
        println(msg)
        terminal.disableRaw()
        exitProcess(1)
    }
    fun runEditor(args: Array<String>){
        terminal.enableRaw()
        terminal.getWindowSize()
        var status = 0
        gui.setStatusMessage(arrayOf("HELP: Ctrl-Q = quit | Ctrl-S = save"))
        gui.refreshScreen()
        if(args.isNotEmpty()) {
            io.open(args[0])
            fileName = args[0]
        }
        else{
            gui.welcomeMessage()
        }
        while(status != 1){
            terminal.getWindowSize()
            gui.refreshScreen()
            status = procceser.processKey()
        }
        die("AnEditor exit\r")
        terminal.disableRaw()
    }
}

