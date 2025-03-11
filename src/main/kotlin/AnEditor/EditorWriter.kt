package AnEditor

import java.io.File

class EditorWriter(var editor: AnEditor) {
    var curr_row = ""
    var num_rows = 0
    fun open(fileName: String){
        val lines = File(fileName).readLines()
        num_rows = 1
        curr_row = lines[0]
    }
    fun drawRows(buf: String) : String{
        var temp = buf
        for(ys in 1..editor.rows) {
            if (ys > num_rows) {
                temp = temp.plus("$ys")
                temp = temp.plus("\u001B[K")
                if (ys < editor.rows)
                    temp = temp.plus("\r\n")
            }
            else{
                var len = curr_row.length
                if(len > editor.cols)
                    len = editor.cols
                temp = temp.plus(curr_row.substring(0, len))
            }
        }
        return temp
    }
    fun refreshScreen(){
        var buf = ""
        System.out.write("\u001B[?25l".toByteArray())
        System.out.write("\u001b[H".toByteArray())
        buf = drawRows(buf)
        buf = buf.plus("\u001B[${editor.coursor_y};${editor.coursor_x}H")
        System.out.write(buf.toByteArray())
        System.out.write("\u001B[?25h".toByteArray())
    }
    fun welcomeMessage(){
        refreshScreen()
        System.out.write("\u001B[${editor.rows/5};${editor.cols*3/5}H".toByteArray())
        print("Welcome to AnEditor! Press any key to start")
        System.out.write("\u001B[H".toByteArray())
        editor.procceser.readKey()
    }
}