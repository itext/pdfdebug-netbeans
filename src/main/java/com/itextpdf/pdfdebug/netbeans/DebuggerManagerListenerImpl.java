package com.itextpdf.pdfdebug.netbeans;

import com.itextpdf.pdfdebug.netbeans.utilities.PdfDocumentUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

class DebuggerManagerListenerImpl implements DebuggerManagerListener {

    private static final String COMPONENT_NAME = "RUPSTopComponent";
    private static final String VARIABLES_TAB_NAME = "localsView";
    private boolean preventUpdate = false;

    /**
     * Listen to whether one of the variable lists is clicked
     */
    private final LookupListener variablesSelectListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent le) {
            Lookup.Result res = (Lookup.Result) le.getSource();
            List<? extends ObjectVariable> list = (List) res.allInstances();
            if (list.size() == 1) {
                ObjectVariable pdfObj = list.get(0);
                if (PdfDocumentUtilities.isPdfDocument(pdfObj)) {
                    RUPSController.showRups(pdfObj);
                } else if(!preventUpdate){
                    RUPSController.hideRups();
                }
            } else if(!preventUpdate){
                RUPSController.hideRups();
            }
            preventUpdate = false;

        }
    };

    private final PropertyChangeListener debuggerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (JPDADebugger.PROP_STATE.equals(propName)) {
                Object newVal = evt.getNewValue();
                if (newVal instanceof Integer && (int) newVal == JPDADebugger.STATE_STOPPED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            TopComponent locals = WindowManager.getDefault().findTopComponent(VARIABLES_TAB_NAME);
                            Node[] activatedNodes = locals.getActivatedNodes();
                            if (activatedNodes != null && activatedNodes.length == 1) {
                                ObjectVariable obj = activatedNodes[0].getLookup().lookup(ObjectVariable.class);
                                if (obj != null) {
                                    ObjectVariable pdfObj = (ObjectVariable) obj;
                                    if (PdfDocumentUtilities.isPdfDocument(pdfObj)) {
                                        preventUpdate = true;
                                        RUPSController.showRups(pdfObj);
                                    } else {
                                        RUPSController.hideRups();
                                    }
                                } else {
                                    RUPSController.hideRups();
                                }
                            }

                        }
                    });
                }
            } else {
                RUPSController.hideRups();
            }
        }
    };

    @Override
    public void engineAdded(DebuggerEngine de) {

        JPDADebugger dbg = de.lookupFirst(null, JPDADebugger.class);
        dbg.addPropertyChangeListener(debuggerListener);
    }

    @Override
    public void engineRemoved(DebuggerEngine de) {

        JPDADebugger dbg = de.lookupFirst(null, JPDADebugger.class);
        dbg.removePropertyChangeListener(debuggerListener);

    }

    @Override
    public void sessionAdded(Session sn) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent locals = WindowManager.getDefault().findTopComponent(VARIABLES_TAB_NAME);
                Lookup lookup = locals.getLookup();
                Lookup.Result lookupRs = lookup.lookupResult(ObjectVariable.class);
                lookupRs.addLookupListener(variablesSelectListener);
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
                Lookup.Result lookupRs = lookup.lookupResult(ObjectVariable.class);
                lookupRs.removeLookupListener(variablesSelectListener);
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
    public void propertyChange(PropertyChangeEvent evt) {
    }

}
