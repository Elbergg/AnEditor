package AnEditor



class EditorWriter(var editor: AnEditor) {
    fun drawRows(buf: String) : String{
        var temp = buf
        for(ys in 0..editor.rows-1) {
            var fileRow = ys + editor.rowOffset
            if (fileRow >= editor.num_rows) {
                temp = temp.plus("${fileRow+1}")
                temp = temp.plus("\u001B[K")

            }
            else{
                var len = editor.in_rows[fileRow].length
                if(len > editor.cols)
                    len = editor.cols
                temp = temp.plus(editor.in_rows[fileRow].substring(0, len))
                temp = temp.plus("\u001B[K")
            }
            if (ys < editor.rows)
                temp = temp.plus("\r\n")
        }
        return temp
    }
    fun scroll(){
        if(editor.coursor_y < editor.rowOffset){
            editor.rowOffset = editor.coursor_y
        }
        if(editor.coursor_y >= editor.rowOffset + editor.rows){
            editor.rowOffset = editor.coursor_y - editor.rows + 1
        }
    }
    fun refreshScreen(){
        scroll()
        var buf = ""
        System.out.write("\u001B[?25l".toByteArray())
        System.out.write("\u001b[H".toByteArray())
        buf = drawRows(buf)
        buf = buf.plus("\u001B[${editor.coursor_y-editor.rowOffset+1};${editor.coursor_x+1}H")
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