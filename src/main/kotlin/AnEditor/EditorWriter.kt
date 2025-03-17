package AnEditor

class EditorWriter(val editor: AnEditor) {
    fun rowInsertChar(idx: Int, at: Int, c: Char){
        val builder = StringBuilder(editor.in_rows[idx])
        builder.insert(at, c.toString())
        editor.in_rows[idx] = builder.toString()
        editor.io.updateRow(idx)
        editor.notSaved = true
    }

    fun insertChar(c: Char){
        if(editor.coursor_y == editor.num_rows){
            insertRow(editor.num_rows, "")
            editor.rows++
        }
        rowInsertChar(editor.coursor_y, editor.coursor_x, c)
        editor.coursor_x++
        editor.notSaved = true
    }
    fun rowDelChar(row_idx: Int, at: Int){
        val builder = StringBuilder(editor.in_rows[row_idx])
        builder.deleteCharAt(at)
        editor.in_rows[row_idx] = builder.toString()
        editor.io.updateRow(row_idx)
        editor.notSaved = true
    }
    fun delChar(){
        if(editor.coursor_y == editor.num_rows || editor.coursor_x == editor.in_rows[editor.coursor_y].length){return}
        if(editor.coursor_x >= 0){
            rowDelChar(editor.coursor_y, editor.coursor_x)
        }
    }
    fun insertRow(at: Int, buf: String){
        editor.in_rows.add(at, buf)
        editor.num_rows++
    }
    fun insertNewLine(){
        if(editor.coursor_x == 0){
            insertRow(editor.coursor_y, "")
        }
        else{
            val temp1 = editor.in_rows[editor.coursor_y].substring(0, editor.coursor_x)
            val temp2 = editor.in_rows[editor.coursor_y].substring(editor.coursor_x, editor.in_rows[editor.coursor_y].length)
            editor.in_rows[editor.coursor_y] = temp1
            insertRow(editor.coursor_y+1, temp2)
        }
        editor.renders.add("")
        editor.io.updateRows()
        editor.coursor_x = 0
        editor.coursor_y++
        editor.notSaved = true
    }
    fun bckspcChar(){
        if(editor.coursor_y == editor.num_rows){return}
        if(editor.coursor_x > 0){
            rowDelChar(editor.coursor_y, editor.coursor_x-1)
            editor.coursor_x--
        }
    }
}