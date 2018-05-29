/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans;

import org.netbeans.api.debugger.DebuggerManager;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        dm.addDebuggerListener(new DebuggerListener());
    }

}
