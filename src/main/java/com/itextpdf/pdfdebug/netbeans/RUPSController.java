package com.itextpdf.pdfdebug.netbeans;

import com.itextpdf.pdfdebug.netbeans.utilities.DebugUtilities;
import com.itextpdf.pdfdebug.netbeans.utilities.PdfDocumentUtilities;
import com.itextpdf.rups.model.LoggerHelper;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author boram
 */
class RUPSController {

    private static final String COMPONENT_NAME = "RUPSTopComponent";
    private static final String MODE_NAVIGATOR = "navigator";

    public static void showRups(final ObjectVariable finalPdfObj) {
        try {

            Runnable openComponent = new Runnable() {
                public void run() {
                    TopComponent rupsComponent = WindowManager.getDefault().findTopComponent(COMPONENT_NAME);
                    if (!rupsComponent.isOpened()) {
                        Mode mode = WindowManager.getDefault().findMode(MODE_NAVIGATOR);
                        mode.dockInto(rupsComponent);
                        rupsComponent.open();
                        rupsComponent.requestActive();
                    }
                }
            };
            final RUPSTopComponent rupsComponent = (RUPSTopComponent) WindowManager.getDefault().findTopComponent(COMPONENT_NAME);
            Runnable loadPdfDocument = new Runnable() {
                public void run() {
                    byte[] rawDocument = PdfDocumentUtilities.getDocumentDebugBytes(finalPdfObj);
                    rupsComponent.setDocumentRawBytes(rawDocument);
                    rupsComponent.setVariableName(DebugUtilities.getVariableName(finalPdfObj));
                    rupsComponent.loadAndHighlightRups();

                    SwingUtilities.invokeLater(openComponent);
                }
            };
            Thread t = new Thread(loadPdfDocument, "PDF loader");
            t.start();

        } catch (Exception e) {
            LoggerHelper.error("Error is accured during read bytes", e, RUPSController.class);
        }

    }

    public static void hideRups() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RUPSTopComponent rupsComponent = (RUPSTopComponent) WindowManager.getDefault().findTopComponent(COMPONENT_NAME);
                rupsComponent.close();
            }
        });
    }
}
