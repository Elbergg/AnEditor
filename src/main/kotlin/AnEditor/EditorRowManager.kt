package AnEditor

class EditorRowManager(private val editor: AnEditor) {
    fun updateRow(row_idx: Int) {
        var render = ""
        var idx = 0
        for(i in 0..<editor.in_rows[row_idx].length){
            if(editor.in_rows[row_idx][i] == '\t'){
                render+= ' '
                idx++
                while (idx%8 != 0){
                    render += ' '
                    idx++
                }
            } else {
                render += editor.in_rows[row_idx][i]
                idx++
            }
        }
        editor.renders[row_idx] = render
    }
    fun updateRows(){
        for(i in 0..<editor.in_rows.size){
            updateRow(i)
        }
    }
    fun rowsToString(): String{
        var buf = ""
        for(row in editor.in_rows){
            buf += row
            buf += '\n'
        }
        return buf
    }

}