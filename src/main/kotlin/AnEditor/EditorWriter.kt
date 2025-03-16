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
            editor.in_rows.add("")
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
        if(editor.coursor_y == editor.num_rows){return}
        if(editor.coursor_x > 0){
            rowDelChar(editor.coursor_y, editor.coursor_x)
            editor.coursor_x--
        }
    }
}