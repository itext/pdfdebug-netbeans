/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfdebug.netbeans.utilities;

import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;

public class DebugUtilities {

    /**
     * Get the name of the variable based on a selection
     *
     * @param selection the selected text
     * @return the name of the variable
     */
    public static String getVariableName(ObjectVariable obj) {
        if (obj instanceof LocalVariable) {
            return ((LocalVariable) obj).getName();
        } else if (obj instanceof Field) {
            return ((Field) obj).getName();
        }
        return "";
    }

}
