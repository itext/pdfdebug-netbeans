/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.rups.Rups;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.SwingHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.luvram.pdfdebugmaven//RUPS//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "RUPSTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
@ActionID(category = "Window", id = "com.luvram.pdfdebugmaven.RUPSTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_RUPSAction",
        preferredID = "RUPSTopComponent"
)
@Messages({
    "CTL_RUPSAction=RUPS",
    "CTL_RUPSTopComponent=PDF Debug",
    "HINT_RUPSTopComponent=This is a RUPS window"
})
public final class RUPSTopComponent extends TopComponent {

    private volatile Rups rups = null;

    private volatile PdfDocument prevDoc = null;
    private byte[] documentRawBytes = null;
    private String variableName = "";

    public RUPSTopComponent() {
        initComponents();
        setName(Bundle.CTL_RUPSTopComponent());
        setToolTipText(Bundle.HINT_RUPSTopComponent());
        initRups();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    public void initRups() {
        SwingHelper.invokeSync(new Runnable() {
            public void run() {
                jPanel1.setLayout(new BorderLayout());
                final Dimension dim = new Dimension(400, 300);
                rups = Rups.startNewPlugin(jPanel1, dim, null);
            }
        });
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {

    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    public void showPdfWindow() {
        ByteArrayInputStream bais = null;
        try {
            if (documentRawBytes != null) {
                bais = new ByteArrayInputStream(documentRawBytes);
                PdfReader reader = new PdfReader(bais);
                PdfDocument tempDoc = new PdfDocument(reader);
                boolean isEqual = false;
                if (prevDoc != null) {
                    isEqual = rups.compareWithDocument(tempDoc, true);
                }
                if (!isEqual) {
                    rups.loadDocumentFromRawContent(documentRawBytes, variableName, null, true);
                }
                if (prevDoc != null) {
                    rups.highlightLastSavedChanges();
                }
                prevDoc = tempDoc;
            }
        } catch (final IOException | PdfException | com.itextpdf.io.IOException e) {
            LoggerHelper.error("Error while reading pdf file.", e, getClass());
        } catch (final Exception e) {
            LoggerHelper.error("Unexpected error.", e, getClass());
        } finally {
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    void setDocumentRawBytes(byte[] rawBytes) {
        documentRawBytes = rawBytes;
    }

    void setVariableName(String name) {
        variableName = name;
    }

    public void disposePdfWindow() {
        try {
            rups.clearHighlights();
            rups.closeDocument();
            if (prevDoc != null) {
                prevDoc.close();
                prevDoc = null;
            }
        } catch (Exception any) {
            LoggerHelper.error("Closing error.", any, getClass());
        }
    }
}