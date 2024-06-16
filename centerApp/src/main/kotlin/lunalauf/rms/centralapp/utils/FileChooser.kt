package lunalauf.rms.centralapp.utils

import lunalauf.rms.modelapi.resource.ModelResourceManager
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

fun showNewFileChooser(): String? {
    val fileChooser = initFileChooser()
    fileChooser.setSelectedFile(File("filename.${ModelResourceManager.FILE_EXTENSION}"))
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
    fileChooser.fileFilter = FileNameExtensionFilter("Luna-Lauf File", ModelResourceManager.FILE_EXTENSION)
    fileChooser.isMultiSelectionEnabled = false
    return fileChooser
}
