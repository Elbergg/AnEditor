package AnEditor

enum class RENDER_CONSTANTS(val value: Int){
    TAB_STOP(8)
}

class EditorGUI(var editor: AnEditor) {
    fun drawStatusBar(buf: String): String{
        var temp = buf
        temp = temp.plus("\u001B[7m")
        var len = editor.fileName.length
        temp = temp.plus(editor.fileName)
        temp = temp.plus(" ")
        len++
        var mod = "Modified: ${editor.notSaved}"
        temp = temp.plus(mod)
        len+= mod.length
        temp = temp.plus(" ")
        len++
        if(len < editor.cols) {
            temp = temp.plus(editor.coursor_y.toString())
            temp = temp.plus(";")
            len += editor.cols.toString().length + 1
        }
        if(len < editor.cols) {
            temp = temp.plus(editor.coursor_x.toString())
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
        editor.statusMsg = args[0]
    }

    fun drawMessageBar(buf: String): String{
        var temp = buf
        temp += ("\u001B[K")
        var msglen = editor.statusMsg.length
        if(msglen > editor.cols)
            msglen -= editor.cols.toString().length
        temp += editor.statusMsg.substring(0,msglen)
        return temp
    }
    fun drawRows(buf: String) : String{
        var temp = buf
        for(ys in 0..editor.rows-1) {
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

    fun scroll(){
        editor.render_x = 0
        if (editor.coursor_y < editor.num_rows){
            editor.render_x = editorRowCxToRx(editor.coursor_y, editor.coursor_x)
        }
        if(editor.coursor_y < editor.rowOffset){
            editor.rowOffset = editor.coursor_y
        }
        if(editor.coursor_y >= editor.rowOffset + editor.rows){
            editor.rowOffset = editor.coursor_y - editor.rows + 1
        }
        if(editor.coursor_x < editor.colOffset){
            editor.colOffset = editor.render_x
        }
        if(editor.coursor_x >= editor.colOffset + (editor.cols-editor.lineNumOffset-1)){
            editor.colOffset = editor.render_x - (editor.cols-editor.lineNumOffset-1) + 1
        }
    }

    fun editorRowCxToRx(row_idx: Int,cx: Int ): Int
    {
        var rx = 0
        for(j in 0..<cx){
            if (editor.in_rows[row_idx][j] == '\t')
                rx += (RENDER_CONSTANTS.TAB_STOP.value - 1) - (rx % RENDER_CONSTANTS.TAB_STOP.value)
            rx++
        }
        return rx
    }

    fun refreshScreen(){
        scroll()
        var buf = ""
        System.out.write("\u001B[?25l".toByteArray())
        System.out.write("\u001b[H".toByteArray())
        buf = drawRows(buf)
        buf = drawStatusBar(buf)
        buf = drawMessageBar(buf)
        buf = buf.plus("\u001B[${editor.coursor_y-editor.rowOffset+1};${editor.render_x - editor.colOffset + editor.lineNumOffset+2}H")
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