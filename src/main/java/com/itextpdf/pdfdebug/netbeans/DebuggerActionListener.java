/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans;

import com.itextpdf.pdfdebug.netbeans.utilities.PdfDocumentUtilities;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.util.Lookup;
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
//        System.out.println(o + " - " + bln);
        if ("pause".equals(o) && bln) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TopComponent locals = WindowManager.getDefault().findTopComponent("localsView");
                    Lookup lookup = locals.getLookup();
                    Lookup.Result lookupRs = lookup.lookupResult(Object.class);
                    List<? extends Object> list = (List) lookupRs.allInstances();
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
            });
        }

    }
}
