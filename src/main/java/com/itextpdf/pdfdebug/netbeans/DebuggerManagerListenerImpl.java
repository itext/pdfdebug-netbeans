/*
 *  This file is part of the iText (R) project.
    Copyright (c) 1998-2019 iText Group NV
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

import com.itextpdf.pdfdebug.netbeans.utilities.PdfDocumentUtilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

class DebuggerManagerListenerImpl extends DebuggerManagerAdapter {
    private static final String COMPONENT_NAME = "RUPSTopComponent";
    private static final String VARIABLES_TAB_NAME = "localsView";
    private boolean preventUpdate = false;
    private Lookup variablesTabLookup = null;
    private Lookup.Result variablesTabLookupRs = null;

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
                } else if (!preventUpdate) {
                    RUPSController.hideRups();
                }
            } else if (!preventUpdate) {
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
                            removeVariablesTabListener();
                            registerVariablesTabListener();

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
            } else if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(propName)) {
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

    private void registerVariablesTabListener() {
        TopComponent locals = WindowManager.getDefault().findTopComponent(VARIABLES_TAB_NAME);
        variablesTabLookup = locals.getLookup();
        variablesTabLookupRs = variablesTabLookup.lookupResult(ObjectVariable.class);
        variablesTabLookupRs.allInstances();
        variablesTabLookupRs.addLookupListener(variablesSelectListener);
    }

    private void removeVariablesTabListener() {
        if (variablesTabLookupRs != null) {
            RUPSTopComponent rupsComponent = (RUPSTopComponent) WindowManager.getDefault().findTopComponent(COMPONENT_NAME);
            if (rupsComponent != null) {
                rupsComponent.close();
                rupsComponent.disposePdfWindow();
            }
            variablesTabLookupRs.removeLookupListener(variablesSelectListener);
        }

    }

}
