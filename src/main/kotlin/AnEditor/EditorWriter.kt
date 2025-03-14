package AnEditor

class EditorWriter(val editor: AnEditor) {
    fun rowInsertChar(idx: Int, at: Int, c: Char){
        val builder = StringBuilder(editor.in_rows[idx])
        builder.insert(at, c.toString())
        editor.in_rows[idx] = builder.toString()
        editor.io.updateRow(idx)
    }

    fun insertChar(c: Char){
        if(editor.coursor_y == editor.num_rows){
            editor.in_rows.add("")
            editor.rows++
        }
        rowInsertChar(editor.coursor_y, editor.coursor_x, c)
        editor.coursor_x++
    }
}