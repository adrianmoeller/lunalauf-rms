package lunalauf.rms.modelapi

import LunaLaufLanguage.LunaLauf
import LunaLaufLanguage.LunaLaufLanguagePackage
import LunaLaufLanguage.impl.LunaLaufLanguageFactoryImpl
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

sealed class DataModelResourceManager {
    companion object {
        private val logger = LoggerFactory.getLogger(Companion::class.java)

        const val fileExtension = "ll"

        fun initialize(): DataModelResourceManager {
            return try {
                Accessible(initResourceSet())
            } catch (e: Exception) {
                InitializationError(
                    message = "Resource set initialization failed. File creation and loading unavailable.",
                    exception = e
                )
            }
        }

        @Throws(IOException::class)
        private fun initResourceSet(): ResourceSet {
            val rs: ResourceSet = ResourceSetImpl()
            rs.packageRegistry[LunaLaufLanguagePackage.eINSTANCE.nsURI] = LunaLaufLanguagePackage.eINSTANCE
            rs.resourceFactoryRegistry.extensionToFactoryMap[fileExtension] = XMIResourceFactoryImpl()
            val conv = rs.uriConverter
            conv.uriMap[URI.createPlatformResourceURI("", true)] =
                URI.createFileURI(File("./../").getCanonicalPath() + "\\")
            rs.uriConverter = conv
            return rs
        }
    }

    data class InitializationError(
        val message: String,
        val exception: Exception
    ) : DataModelResourceManager() {
        init {
            logger.error(message, exception)
        }
    }

    class Accessible(
        private val resSet: ResourceSet
    ) : DataModelResourceManager() {
        private var resource: Resource? = null
        private var preSaveProcessing: Runnable? = null

        fun newFile(uri: URI, year: Int): DataModelResult {
            if (resource != null) {
                if (save() is SaveResult.Error)
                    return DataModelResult.Error("Unable to create new file since open model cannot be saved")
            }
            try {
                resSet.resources.clear()
            } catch (e: Exception) {
                logger.warn("Failed clearing resources in the resource set", e)
            }
            resource = resSet.createResource(uri)
            if (resource == null)
                return DataModelResult.Error("Failed creating resource")

            val model = createModel(year)
            try {
                resource!!.contents.add(model)
            } catch (e: Exception) {
                return DataModelResult.Error("Failed adding model to resource", e)
            }
            return DataModelResult.Available(model)
        }

        fun load(uri: URI): DataModelResult {
            if (resource != null) {
                if (save() is SaveResult.Error)
                    return DataModelResult.Error("Unable to load file since open model cannot be saved")
            }
            try {
                resSet.resources.clear()
            } catch (e: Exception) {
                logger.warn("Failed clearing resources in the resource set", e)
            }
            resource = resSet.createResource(uri)
            if (resource == null)
                return DataModelResult.Error("Failed creating resource")

            try {
                resource!!.load(null)
            } catch (e: IOException) {
                DataModelResult.Error("Failed loading resource", e)
            }

            val model = try {
                resource!!.contents[0] as LunaLauf
            } catch (index: IndexOutOfBoundsException) {
                return DataModelResult.Error("Corrupted file: no contents found in the resource", index)
            } catch (e: Exception) {
                return DataModelResult.Error("Corrupted file", e)
            }

            return DataModelResult.Available(model)
        }

        fun save(): SaveResult {
            try {
                preSaveProcessing?.run()
            } catch (ignored: Exception) {
            }
            if (resource == null)
                return SaveResult.NoFileOpen
            try {
                resource!!.save(null)
            } catch (e: IOException) {
                return SaveResult.Error("Failed saving model", e)
            }
            return SaveResult.Success(resource!!.uri)
        }

        fun close(): CloseResult {
            resource = try {
                resSet.resources.clear()
                null
            } catch (e: Exception) {
                return CloseResult.NoFileOpen
            }
            logger.info("Closed file successfully")
            return CloseResult.Success
        }

        fun removePreSaveProcessing() {
            preSaveProcessing = null
        }

        fun setPreSaveProcessing(preSaveProcessing: Runnable?) {
            this.preSaveProcessing = preSaveProcessing
        }

        val isFileOpen: Boolean
            get() = resource != null

        private fun createModel(year: Int): LunaLauf {
            val model = LunaLaufLanguageFactoryImpl.eINSTANCE.createLunaLauf()
            model.year = year
            logger.info("Model created")
            return model
        }
    }

    sealed class DataModelResult {
        class Available(val dataModel: LunaLauf) : DataModelResult()
        class Error(val message: String, val exception: Exception? = null) : DataModelResult() {
            init {
                if (exception == null) logger.error(message) else logger.error(message)
            }
        }
    }

    sealed class SaveResult {
        data object NoFileOpen : SaveResult()
        class Success(val uri: URI) : SaveResult()
        class Error(val message: String, val exception: Exception) : SaveResult() {
            init {
                logger.error(message, exception)
            }
        }
    }

    sealed class CloseResult {
        data object NoFileOpen : CloseResult()
        data object Success : CloseResult()
    }
}