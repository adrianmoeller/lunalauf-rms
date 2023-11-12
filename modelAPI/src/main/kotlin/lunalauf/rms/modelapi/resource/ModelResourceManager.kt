package lunalauf.rms.modelapi.resource

import LunaLaufLanguage.LunaLauf
import LunaLaufLanguage.LunaLaufLanguagePackage
import LunaLaufLanguage.impl.LunaLaufLanguageFactoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.modelapi.ModelState
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

private val logger = LoggerFactory.getLogger(ModelResourceManager.Companion::class.java)

sealed class ModelResourceManager {
    companion object {
        const val FILE_EXTENSION = "ll"

        fun initialize(): ModelResourceManager {
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
            rs.resourceFactoryRegistry.extensionToFactoryMap[FILE_EXTENSION] = XMIResourceFactoryImpl()
            val conv = rs.uriConverter
            conv.uriMap[URI.createPlatformResourceURI("", true)] =
                URI.createFileURI(File("./../").getCanonicalPath() + "\\")
            rs.uriConverter = conv
            logger.info("Resource set initialized")
            return rs
        }
    }

    data class InitializationError(
        val message: String,
        val exception: Exception
    ) : ModelResourceManager() {
        init {
            logger.error(message, exception)
        }
    }

    class Accessible(
        private val resSet: ResourceSet
    ) : ModelResourceManager() {
        private val mutex = Mutex()

        private var resource: Resource? = null
        private var preSaveProcessing: () -> Unit = {}

        suspend fun newFile(uri: URI, year: Int): ModelResult {
            runPreSaveProcessing()
            mutex.withLock {
                if (resource != null) {
                    if (internalSave() is SaveResult.Error)
                        return ModelResult.Error("Unable to create new file since open model cannot be saved")
                }
                try {
                    resSet.resources.clear()
                } catch (e: Exception) {
                    logger.warn("Failed clearing resources in the resource set", e)
                }
                resource = resSet.createResource(uri)
                val constResource = resource
                    ?: return ModelResult.Error("Failed creating resource")

                val model = createModel(year)
                try {
                    constResource.contents.add(model)
                } catch (e: Exception) {
                    return ModelResult.Error("Failed adding model to resource", e)
                }
                return ModelResult.Available(mutex, uri.path() ?: "", model)
            }
        }

        suspend fun load(uri: URI): ModelResult {
            runPreSaveProcessing()
            mutex.withLock {
                if (resource != null) {
                    if (internalSave() is SaveResult.Error)
                        return ModelResult.Error("Unable to load file since open model cannot be saved")
                }
                try {
                    resSet.resources.clear()
                } catch (e: Exception) {
                    logger.warn("Failed clearing resources in the resource set", e)
                }
                resource = resSet.createResource(uri)
                val constResource = resource
                    ?: return ModelResult.Error("Failed creating resource")

                try {
                    constResource.load(null)
                } catch (e: IOException) {
                    ModelResult.Error("Failed loading resource", e)
                }

                val model = try {
                    constResource.contents[0] as LunaLauf
                } catch (index: IndexOutOfBoundsException) {
                    return ModelResult.Error("Corrupted file: no contents found in the resource", index)
                } catch (e: Exception) {
                    return ModelResult.Error("Corrupted file", e)
                }

                logger.info("Model loaded")
                return ModelResult.Available(mutex, uri.path() ?: "", model)
            }
        }

        suspend fun save(): SaveResult {
            runPreSaveProcessing()
            mutex.withLock {
                return internalSave()
            }
        }

        private fun runPreSaveProcessing() {
            try {
                preSaveProcessing()
            } catch (_: Exception) {
            }
        }

        private fun internalSave(): SaveResult {
            val constResource = resource
                ?: return SaveResult.NoFileOpen
            try {
                constResource.save(null)
            } catch (e: IOException) {
                return SaveResult.Error("Failed saving model", e)
            }
            return SaveResult.Success(constResource.uri)
        }

        suspend fun close(): CloseResult {
            mutex.withLock {
                if (resource == null) {
                    logger.warn("Close attempt, but no model open")
                    return CloseResult.NoFileOpen
                }
                resource = null

                try {
                    resSet.resources.clear()
                } catch (e: Exception) {
                    logger.warn("Failed clearing resources in the resource set", e)
                }
                logger.info("Closed model")
                return CloseResult.Success
            }
        }

        suspend fun removePreSaveProcessing() {
            mutex.withLock { preSaveProcessing = {} }
        }

        suspend fun setPreSaveProcessing(preSaveProcessing: () -> Unit) {
            mutex.withLock { this.preSaveProcessing = preSaveProcessing }
        }

        suspend fun isFileOpen(): Boolean {
            mutex.withLock { return resource != null }
        }

        private fun createModel(year: Int): LunaLauf {
            val model = LunaLaufLanguageFactoryImpl.eINSTANCE.createLunaLauf()
            model.year = year
            logger.info("Model created")
            return model
        }
    }
}

sealed class ModelResult {
    class Available(mutex: Mutex, path: String, model: LunaLauf) : ModelResult() {
        val modelState = ModelState.Loaded(mutex, path, model)
    }

    class Error(val message: String, val exception: Exception? = null) : ModelResult() {
        init {
            if (exception == null) logger.error(message) else logger.error(message, exception)
        }
    }
}

sealed class SaveResult {
    data object NoFileOpen : SaveResult()
    class Success(val uri: URI) : SaveResult() {
        init {
            logger.info("Model saved")
        }
    }

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