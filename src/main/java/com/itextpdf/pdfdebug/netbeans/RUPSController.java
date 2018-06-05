/*
 *  This file is part of the iText (R) project.
 * Copyright (c) 2007-2015 iText Group NV
 * Authors: Bruno Lowagie et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
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
