/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2020 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.pdfdebug.netbeans.utilities;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.rups.model.LoggerHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        if (var != null && var.getClassType() != null && var.getClassType().isInstanceOf(CLASS_TYPE)) {
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
        try {
            doc.close();
        } catch (PdfException e) {
            LoggerHelper.error("PdfDocument is empty or has something closing error", e, PdfDocumentUtilities.class);
            return null;
        }

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
