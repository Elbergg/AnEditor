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
            editor.in_rows = lines
            updateRows()
            editor.lineNumOffset = lines.size.toString().length
        }catch(e: FileNotFoundException){
            editor.die("File not found")
        }catch(e: IOException){
            editor.die("IOError")
        }
    }
    fun updateRow(row: String): String {
        var render = ""
        for(i in 0..row.length-1){
            if(row[i] == '\t'){
                while (i%8 != 0){
                    render += ' '
                }
            } else {
                render += row[i]
            }

        }
        return render
    }
    fun updateRows(){
        for(i in 0..editor.in_rows.size-1){
            editor.renders = editor.renders.plus(updateRow(editor.in_rows[i]))
        }
    }
}