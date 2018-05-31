package com.itextpdf.pdfdebug.netbeans;

import org.netbeans.api.debugger.DebuggerManager;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {
    @Override
    public void restored() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        dm.addDebuggerListener(new DebuggerManagerListenerImpl());
    }

}
