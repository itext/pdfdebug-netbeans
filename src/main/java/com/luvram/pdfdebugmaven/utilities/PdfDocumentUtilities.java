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
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
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

    public static boolean isPdfDocument(ObjectVariable var) {
        if (var != null && var.getClassType().isInstanceOf(CLASS_TYPE)) {
            return true;
        }

        return false;
    }

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

    private static PdfDocument getPdfDocument(ObjectVariable var) {
        byte[] bytes = null;
        try {
            Variable[] arg = {};
            Variable result = var.invokeMethod("getSerializedBytes", "()[B", arg);
            bytes = (byte[]) result.createMirrorObject();
        } catch (NoSuchMethodException e) {
            LoggerHelper.error("Getting serialized bytes error", e, PdfDocumentUtilities.class);
        } catch (InvalidExpressionException e) {
            LoggerHelper.error("Getting serialized bytes error", e, PdfDocumentUtilities.class);
        }

        return createDocumentFromBytes(bytes);
    }

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
