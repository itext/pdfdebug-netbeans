/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans;

import com.itextpdf.pdfdebug.netbeans.utilities.PdfDocumentUtilities;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author boram
 */
public class DebuggerActionListener implements ActionsManagerListener {

    @Override
    public void actionPerformed(Object o) {
    }

    @Override
    public void actionStateChanged(Object o, boolean bln) {
        System.out.println(o + " - " + bln);
        if ("pause".equals(o) && bln) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TopComponent locals = WindowManager.getDefault().findTopComponent("localsView");
                    Node[] activatedNodes = locals.getActivatedNodes();
                    if (activatedNodes != null && activatedNodes.length == 1) {
                        ObjectVariable obj = activatedNodes[0].getLookup().lookup(ObjectVariable.class);
                        if (obj != null) {
                            ObjectVariable pdfObj = (ObjectVariable) obj;
                            if (PdfDocumentUtilities.isPdfDocument(pdfObj)) {
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

    }
}
