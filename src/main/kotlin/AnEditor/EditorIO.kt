package AnEditor
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.system.exitProcess

class EditorIO (val editor: AnEditor) {
    fun open(fileName: String){
        try {
            val lines = File(fileName).readLines()
            editor.num_rows = lines.size
            editor.in_rows = lines.toMutableList()
            editor.renders = lines.toMutableList()
            updateRows()
            editor.lineNumOffset = lines.size.toString().length
        }catch(e: FileNotFoundException){
            editor.die("File not found")
        }catch(e: IOException){
            editor.die("IOError")
        }
    }
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
        for(i in 0..editor.in_rows.size-1){
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
    fun save(): Int{
        if(editor.fileName == "")
            return -1
        val buf = rowsToString()
        val file = File(editor.fileName)
        try{file.writeText(buf)}catch(e:IOException){
            editor.gui.setStatusMessage(arrayOf("Error saving file"))
            return -1
        }
        editor.gui.setStatusMessage(arrayOf("File saved succesfully"))
        editor.notSaved = false
        return 0
    }

}