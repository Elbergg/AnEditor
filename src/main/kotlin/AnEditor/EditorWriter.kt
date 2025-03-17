package AnEditor

class EditorWriter(val editor: AnEditor) {
    fun rowInsertChar(idx: Int, at: Int, c: Char){
        val builder = StringBuilder(editor.in_rows[idx])
        builder.insert(at, c.toString())
        editor.in_rows[idx] = builder.toString()
        editor.rowmng.updateRow(idx)
        editor.notSaved = true
    }
    fun insertChar(c: Char){
        if(editor.cursor_y == editor.num_rows){
            insertRow(editor.num_rows, "")
            editor.rows++
        }
        rowInsertChar(editor.cursor_y, editor.cursor_x, c)
        editor.cursor_x++
        editor.notSaved = true
    }
    fun rowDelChar(row_idx: Int, at: Int){
        val builder = StringBuilder(editor.in_rows[row_idx])
        builder.deleteCharAt(at)
        editor.in_rows[row_idx] = builder.toString()
        editor.rowmng.updateRow(row_idx)
        editor.notSaved = true
    }
    fun delChar(){
        if(editor.cursor_y == editor.num_rows || editor.cursor_x == editor.in_rows[editor.cursor_y].length){return}
        if(editor.cursor_x >= 0){
            rowDelChar(editor.cursor_y, editor.cursor_x)
        }
    }
    fun insertRow(at: Int, buf: String){
        editor.in_rows.add(at, buf)
        editor.num_rows++
    }
    fun insertNewLine(){
        if(editor.cursor_x == 0){
            insertRow(editor.cursor_y, "")
        }
        else{
            val temp1 = editor.in_rows[editor.cursor_y].substring(0, editor.cursor_x)
            val temp2 = editor.in_rows[editor.cursor_y].substring(editor.cursor_x, editor.in_rows[editor.cursor_y].length)
            editor.in_rows[editor.cursor_y] = temp1
            insertRow(editor.cursor_y+1, temp2)
        }
        editor.renders.add("")
        editor.rowmng.updateRows()
        editor.cursor_x = 0
        editor.cursor_y++
        editor.notSaved = true
    }
    fun bckspcChar(){
        if(editor.cursor_y == editor.num_rows){return}
        if(editor.cursor_x > 0){
            rowDelChar(editor.cursor_y, editor.cursor_x-1)
            editor.cursor_x--
        }
    }
}