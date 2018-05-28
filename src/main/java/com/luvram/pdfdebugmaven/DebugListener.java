/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luvram.pdfdebugmaven;

import com.itextpdf.rups.model.LoggerHelper;
import com.luvram.pdfdebugmaven.utilities.PdfDocumentUtilities;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.LocalVariable;
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
public class DebugListener implements DebuggerManagerListener {

    private final LookupListener listener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent le) {
            System.out.println("Result Changed");
            Lookup.Result res = (Lookup.Result) le.getSource();
            List<? extends Object> list = (List) res.allInstances();
            Object obj = list.get(0);
            if (obj instanceof ObjectVariable) {
                boolean isPdfDocument = false;
                ObjectVariable pdfObj = (ObjectVariable) obj;
                isPdfDocument = pdfObj.getClassType().isInstanceOf("com.itextpdf.kernel.pdf.PdfDocument");
                if (isPdfDocument) {
                    showRups(pdfObj);

                }
            }

        }
    };

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
                RUPSTopComponent rupsComponent = (RUPSTopComponent) WindowManager.getDefault().findTopComponent("RUPSTopComponent");
                rupsComponent.close();

                TopComponent locals = WindowManager.getDefault().findTopComponent("localsView");
                Lookup lookup = locals.getLookup();
                Lookup.Result lookupRs = lookup.lookupResult(Object.class);
                lookupRs.removeLookupListener(listener);
            }

        });
    }

    @Override
    public void engineAdded(DebuggerEngine de) {

    }

    @Override
    public void engineRemoved(DebuggerEngine de) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    public void showRups(final ObjectVariable finalPdfObj) {
        try {
            final RUPSTopComponent rupsComponent = (RUPSTopComponent) WindowManager.getDefault().findTopComponent("RUPSTopComponent");
            Runnable runnable = new Runnable() {
                public void run() {
                    byte[] rawDocument = PdfDocumentUtilities.getDocumentDebugBytes(finalPdfObj);
                    if (finalPdfObj instanceof LocalVariable) {
                        rupsComponent.setVariableName(((LocalVariable) finalPdfObj).getName());
                    } else if (finalPdfObj instanceof Field) {
                        rupsComponent.setVariableName(((Field) finalPdfObj).getName());
                    } else {
                        rupsComponent.setVariableName("");
                    }

                    rupsComponent.setDocumentRawBytes(rawDocument);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (!rupsComponent.isOpened()) {
                                rupsComponent.open();
                                rupsComponent.requestActive();
                            }
                        }
                    });

                    rupsComponent.draw();
                }
            };
            Thread t = new Thread(runnable);
            t.start();
        } catch (Exception e) {
            LoggerHelper.error("Error is accured during read bytes", e, getClass());
        }

    }

}
