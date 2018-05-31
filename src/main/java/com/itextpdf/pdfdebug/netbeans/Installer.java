/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans;

import java.util.List;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
//import org.netbeans.modules.debugger.jpda.actions.StepIntoActionProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    private final ActionsProviderListener actionsProviderListener  = new ActionsProviderListener() {
        @Override
        public void actionStateChange(Object action, boolean enabled) {
            System.out.println(action + " = " + enabled);
        }
    };

    @Override
    public void restored() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        dm.addDebuggerListener(new DebuggerVariablesListener());
    }

}
