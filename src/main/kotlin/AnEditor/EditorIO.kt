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
}