/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luvram.pdfdebugmaven.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.SwingHelper;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 * This class contains some static utility methods that act as wrappers around
 * the iText core functionality
 */
public class PdfDocumentUtilities {

    public static final String CLASS_TYPE = "com.itextpdf.kernel.pdf.PdfDocument";
    public static final String METHOD_SIGNATURE = "()[B";
    public static final String METHOD_NAME = "getSerializedBytes";

    private static final String NOT_READY_FOR_PLUGIN_MESSAGE = "Cannot get PdfDocument. "
            + "\nMake sure you create reader from stream or string and writer is set to DebugMode.";

    private static final String DOCUMENT_IS_CLOASED_MESSAGE = "The document was closed.";

    private static final String DEBUG_BYTES_METHOD_NAME = "getDebugBytes";
    private static Method getDebugBytesMethod;

    static {
        try {
            getDebugBytesMethod = PdfWriter.class.getDeclaredMethod(DEBUG_BYTES_METHOD_NAME);
            getDebugBytesMethod.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
    }

//    /**
//     * Return whether a given IJavaVariable represents a PdfDocument object
//     *
//     * @param var the input variable
//     * @return true if the input variable is a PdfDocument object, false otherwise
//     */
//    public static boolean isPdfDocument(ObjectVariable var) {
//        try {
//            ObjectVariable obj = DebugUtilities.getIJavaObject(var);
//            if (obj != null && obj.getJavaType().getName().equals(CLASS_TYPE)) {
//                return true;
//            }
//        } catch (DebugException ignored) {
//        }
//        return false;
//    }
    /**
     * Get the bytes representing the PdfDocument in a given IJavaVariable
     *
     * @param var the input variable
     * @return the bytes representing the PdfDocument
     */
    public static byte[] getDocumentDebugBytes(ObjectVariable var) {
        PdfDocument doc = null;
        doc = getPdfDocument(var);
        if (doc == null) {
            LoggerHelper.error(NOT_READY_FOR_PLUGIN_MESSAGE, PdfDocumentUtilities.class);
            return null;
        }
        if (doc.isClosed()) {
            LoggerHelper.warn(DOCUMENT_IS_CLOASED_MESSAGE, PdfDocumentUtilities.class);
            return null;
        }
        PdfWriter writer = doc.getWriter();
        writer.setCloseStream(true);
        doc.setCloseWriter(false);
        doc.close();
        byte[] documentCopyBytes = null;
        try {
            documentCopyBytes = (byte[]) getDebugBytesMethod.invoke(writer);
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
        try {
            writer.close();
        } catch (IOException e) {
            LoggerHelper.error("Writer cloasing error", e, PdfDocumentUtilities.class);
        }
        return documentCopyBytes;
    }

    /**
     * Get the PdfDocument object represented by a given IJavaVariable
     *
     * @param var the input variable
     * @return the PdfDocument object represented by the input variable
     */
    private static PdfDocument getPdfDocument(ObjectVariable var) {
        byte[] bytes = null;
        try {
            Variable[] arg = {};
            Variable result = var.invokeMethod("getSerializedBytes", "()[B", arg);
            bytes = (byte[])result.createMirrorObject();
        } catch (NoSuchMethodException e) {
            LoggerHelper.error("Getting serialized bytes error", e, PdfDocumentUtilities.class);
        } catch (InvalidExpressionException e) {
            LoggerHelper.error("Getting serialized bytes error", e, PdfDocumentUtilities.class);
        }

        return createDocumentFromBytes(bytes);
    }

    /**
     * Get the bytes representing the PdfDocument in a given IJavaValue
     *
     * @param byteArr the input value
     * @return the bytes representing the PdfDocument
     */
//    private static byte[] getByteArray(IJavaValue byteArr) {
//        byte[] res = null;
//        try {
//            if (byteArr instanceof IJavaArray) {
//                IJavaValue[] arr = ((IJavaArray) byteArr).getValues();
//                res = new byte[arr.length];
//                if (arr.length != 0 && arr[0] instanceof IJavaPrimitiveValue) {
//                    for (int i = 0; i < arr.length; ++i) {
//                        res[i] = ((IJavaPrimitiveValue) arr[i]).getByteValue();
//                    }
//                }
//            }
//        } catch (DebugException ignored) {
//        }
//        return res;
//    }
    /**
     * Creates a PdfDocument from a byte[]
     *
     * @param bytes input byte[]
     * @return a PdfDocument object
     */
    private static PdfDocument createDocumentFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        PdfDocument doc = null;
        try {
            doc = (PdfDocument) new ObjectInputStream(bais).readObject();
        } catch (ClassNotFoundException ignored) {
        } catch (IOException ignored) {
        } finally {
            try {
                bais.close();
            } catch (IOException ignored) {
            }
        }
        return doc;
    }

}
