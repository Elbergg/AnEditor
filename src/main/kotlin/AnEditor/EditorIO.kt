package AnEditor
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class EditorIO (val editor: AnEditor) {
    fun open(fileName: String){
        try {
            val lines = File(fileName).readLines()
            editor.num_rows = lines.size
            editor.in_rows = lines
        }catch(e: FileNotFoundException){
            editor.die("File not found")
        }catch(e: IOException){
            editor.die("IOError")
        }
    }
}