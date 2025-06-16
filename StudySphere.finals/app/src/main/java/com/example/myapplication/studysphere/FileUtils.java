package com.example.studysphere;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor PdfTextExtractor;

    // Read PDF file using PdfRenderer (renders pages as images, no direct text extraction)
    public static void renderPdf(Context context, Uri uri) {
        ParcelFileDescriptor fileDescriptor = null;
        PdfRenderer pdfRenderer = null;
        try {
            fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                pdfRenderer = new PdfRenderer(fileDescriptor);
                int pageCount = pdfRenderer.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = pdfRenderer.openPage(i);
                    // Render each page to a bitmap
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                    // Do something with the bitmap (e.g., display it in an ImageView)
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error rendering PDF: ", e);
        } finally {
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (fileDescriptor != null) {
                try {
                    fileDescriptor.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing file descriptor", e);
                }
            }
        }
    }

    // Read DOCX file using XmlPullParser (manual parsing of XML content inside DOCX)
    public static String readDocx(Context context, Uri uri) {
        StringBuilder text = new StringBuilder();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (entry.getName().equals("word/document.xml")) {
                        // Process XML content of the DOCX file
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = factory.newPullParser();
                        parser.setInput(zipInputStream, null);

                        int eventType = parser.getEventType();
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && "t".equals(parser.getName())) {
                                text.append(parser.nextText());
                            }
                            eventType = parser.next();
                        }
                    }
                }
                zipInputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading DOCX file: ", e);
        }
        return text.toString();
    }

    // Read text file
    public static String readTextFile(Context context, Uri uri) {
        StringBuilder content = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading text file: ", e);
        }
        return null;
    }
    public static String readPdf(Context context, Uri uri) {
        StringBuilder text = new StringBuilder();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                PdfReader reader = new PdfReader(inputStream);
                PdfDocument pdfDocument = new PdfDocument(reader);

                int numberOfPages = pdfDocument.getNumberOfPages();
                for (int i = 1; i <= numberOfPages; i++) {
                    String pageText = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i));
                    text.append(pageText).append("\n");
                }

                pdfDocument.close();
                reader.close();
            }
        } catch (IOException e) {
            Log.e("FileUtils", "Error reading PDF file: ", e);
        }
        return text.toString();
    }

    // Get file name
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex != -1) {
                    result = cursor.getString(columnIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name", e);
        }
        return result != null ? result : "Unknown File";
    }

}