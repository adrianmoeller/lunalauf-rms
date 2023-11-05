package lunalauf.rms.utilities.network.util;

import LunaLaufLanguage.Runner;

import java.io.File;

public interface ImageViewer {

	void view(Runner runner, File image);
	boolean acceptFrom(Runner runner);
	
}
