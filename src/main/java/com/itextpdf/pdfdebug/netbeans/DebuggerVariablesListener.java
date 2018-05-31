/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans;

import com.itextpdf.pdfdebug.netbeans.utilities.PdfDocumentUtilities;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author boram
 */
// todo 이름변경하기
public class DebuggerVariablesListener implements DebuggerManagerListener {

    private static final String COMPONENT_NAME = "RUPSTopComponent";
    private static final String MODE = "navigator";

    private final LookupListener listener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent le) {
            Lookup.Result res = (Lookup.Result) le.getSource();
            List<? extends Object> list = (List) res.allInstances();
            RUPSController rupsController = new RUPSController();
            Object obj = list.get(0);
            if (obj instanceof ObjectVariable) {
                ObjectVariable pdfObj = (ObjectVariable) obj;
                if (PdfDocumentUtilities.isPdfDocument(pdfObj)) {
                    rupsController.showRups(pdfObj);
                } else {
                    rupsController.hideRups();
                }
            } else {
                rupsController.hideRups();
            }
        }
    };

    @Override
    public void sessionAdded(Session sn) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent locals = WindowManager.getDefault().findTopComponent("localsView");
                Lookup lookup = locals.getLookup();
                Lookup.Result lookupRs = lookup.lookupResult(Object.class);
                lookupRs.addLookupListener(listener);
            }
        });

    }

    @Override
    public void sessionRemoved(Session sn) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RUPSTopComponent rupsComponent = (RUPSTopComponent) WindowManager.getDefault().findTopComponent(COMPONENT_NAME);
                if (rupsComponent != null) {
                    rupsComponent.close();
                    rupsComponent.disposePdfWindow();
                }

                TopComponent locals = WindowManager.getDefault().findTopComponent("localsView");
                Lookup lookup = locals.getLookup();
                Lookup.Result lookupRs = lookup.lookupResult(Object.class);
                lookupRs.removeLookupListener(listener);
            }

        });
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        return null;
    }

    @Override
    public void breakpointAdded(Breakpoint brkpnt) {
    }

    @Override
    public void breakpointRemoved(Breakpoint brkpnt) {
    }

    @Override
    public void initWatches() {

    }

    @Override
    public void watchAdded(Watch watch) {
    }

    @Override
    public void watchRemoved(Watch watch) {
    }

    @Override
    public void engineAdded(DebuggerEngine de) {
        ActionsManager am = de.getActionsManager ();
        am.addActionsManagerListener(new DebuggerActionListener());
    }

    @Override
    public void engineRemoved(DebuggerEngine de) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

}
