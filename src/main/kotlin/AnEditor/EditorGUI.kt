package AnEditor

enum class RENDER_CONSTANTS(val value: Int){
    TAB_STOP(8)
}

class EditorGUI(private var editor: AnEditor) {
    private fun drawStatusBar(buf: String): String{
        var temp = buf
        temp = temp.plus("\u001B[7m")
        var len = editor.fileName.length
        temp = temp.plus(editor.fileName)
        temp = temp.plus(" ")
        len++
        val mod = "Modified: ${editor.notSaved}"
        temp = temp.plus(mod)
        len+= mod.length
        temp = temp.plus(" ")
        len++
        if(len < editor.cols) {
            temp = temp.plus((editor.cursor_y+1).toString())
            temp = temp.plus(";")
            len += editor.cols.toString().length + 1
        }
        if(len < editor.cols) {
            temp = temp.plus((editor.cursor_x+1).toString())
            len += editor.rows.toString().length
        }
        while(len < editor.cols){
        temp = temp.plus(" ")
        len++
        }
        temp = temp.plus("\u001B[m")
        temp = temp.plus("\r\n")
        return temp
    }

    fun setStatusMessage(args: Array<String>){
        editor.statusMsg = ""
        for(arg in args) {
            editor.statusMsg += arg
            editor.statusMsg += " "
        }
    }

    private fun drawMessageBar(buf: String): String{
        var temp = buf
        temp += ("\u001B[K")
        var msglen = editor.statusMsg.length
        if(msglen > editor.cols)
            msglen -= editor.cols.toString().length
        temp += editor.statusMsg.substring(0,msglen)
        return temp
    }
    private fun drawRows(buf: String) : String{
        var temp = buf
        for(ys in 0..<editor.rows) {
            var fileRow = ys + editor.rowOffset
            if (fileRow >= editor.num_rows) {
                temp = temp.plus("${fileRow+1}")
                temp = temp.plus("\u001B[K")

            }
            else{
                var len = editor.renders[fileRow].length - editor.colOffset
                if(len > editor.cols - editor.lineNumOffset - 2)
                    len = editor.cols - editor.lineNumOffset - 2
                temp = temp.plus("${fileRow+1}")
                temp = temp.plus(" ".repeat(editor.lineNumOffset+1-(fileRow+1).toString().length))
                if(len > 0)
                    temp = temp.plus(editor.renders[fileRow].substring(editor.colOffset, len+editor.colOffset))
                temp = temp.plus("\u001B[K")
            }
            temp = temp.plus("\r\n")
        }
        return temp
    }

    private fun scroll(){
        editor.render_x = 0
        if (editor.cursor_y < editor.num_rows){
            editor.render_x = editor.cursor.CxToRx(editor.cursor_y, editor.cursor_x)
        }
        if(editor.cursor_y < editor.rowOffset){
            editor.rowOffset = editor.cursor_y
        }
        if(editor.cursor_y >= editor.rowOffset + editor.rows){
            editor.rowOffset = editor.cursor_y - editor.rows + 1
        }
        if(editor.cursor_x < editor.colOffset){
            editor.colOffset = editor.render_x
        }
        if(editor.cursor_x >= editor.colOffset + (editor.cols-editor.lineNumOffset-1)){
            editor.colOffset = editor.render_x - (editor.cols-editor.lineNumOffset-1) + 1
        }
    }


    fun prompt(prompt: String): String{
        var buf = ""
        while(true){
            setStatusMessage(arrayOf(prompt, buf))
            refreshScreen()
            val c = editor.processor.readKey()
            if(c == KEYS.BACKSPACE.key){
                if (buf.length >= 1){
                    buf = buf.substring(0, buf.length-1)
                }
            }
            else if(c.toChar() == ''){
                buf = ""
                return buf
            }
            else if(c.toChar() == '\r'){
                setStatusMessage(arrayOf(""))
                return buf
            }
            else if(!c.toChar().isISOControl() && c < 128){
                buf += c.toChar()
            }
        }
    }

    fun refreshScreen(){
        editor.lineNumOffset = editor.num_rows.toString().length
        scroll()
        var buf = ""
        System.out.write("\u001B[?25l".toByteArray())
        System.out.write("\u001b[H".toByteArray())
        buf = drawRows(buf)
        buf = drawStatusBar(buf)
        buf = drawMessageBar(buf)
        buf = buf.plus("\u001B[${editor.cursor_y-editor.rowOffset+1};${editor.render_x - editor.colOffset + editor.lineNumOffset+2}H")
        System.out.write(buf.toByteArray())
        System.out.write("\u001B[?25h".toByteArray())
    }
    fun welcomeMessage(){
        refreshScreen()
        System.out.write("\u001B[${editor.rows/5};${editor.cols*3/5}H".toByteArray())
        print("Welcome to AnEditor! Press any key to start")
        System.out.write("\u001B[H".toByteArray())
    }
}