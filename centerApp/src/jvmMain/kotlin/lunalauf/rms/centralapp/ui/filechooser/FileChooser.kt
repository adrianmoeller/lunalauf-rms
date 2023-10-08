package lunalauf.rms.centralapp.ui.filechooser

import lunalauf.rms.modelapi.LunaLaufAPI
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

fun showNewFileChooser(): String? {
    val fileChooser = initFileChooser()
    fileChooser.setSelectedFile(File("filename.${LunaLaufAPI.fileExtension}"))
    val result = fileChooser.showSaveDialog(null)

    if (result == JFileChooser.CANCEL_OPTION)
        return null
    return fileChooser.selectedFile?.absolutePath
}

fun showOpenFileChooser(): String? {
    val fileChooser = initFileChooser()
    val result = fileChooser.showOpenDialog(null)

    if (result == JFileChooser.CANCEL_OPTION)
        return null
    return fileChooser.selectedFile?.absolutePath
}

private fun initFileChooser(): JFileChooser {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    val fileChooser = JFileChooser()
    fileChooser.fileFilter = FileNameExtensionFilter("Luna-Lauf File", LunaLaufAPI.fileExtension)
    fileChooser.isMultiSelectionEnabled = false
    return fileChooser
}
