package com.bakiatmaca.poc.embedded.nodejs.embed;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ContentExtractor {

    private static final String TAG = "ContentExtractor";

    private static final int BUFSIZE = 5192;
    private static final String FILTER = "assets";

    public static synchronized void copyAssets(Context context, File extractPath) {
        String apkPath = context.getPackageCodePath();
        String mAppRoot = extractPath.getAbsolutePath(); //context.getFilesDir().toString();

        try {
            File zipFile = new File(apkPath);
            long zipLastModified = zipFile.lastModified();
            ZipFile zip = new ZipFile(apkPath);
            Vector<ZipEntry> files = getAssets(zip);
            int zipFilterLength = FILTER.length();

            Enumeration<?> entries = files.elements();
            while (entries.hasMoreElements()) {

                ZipEntry entry = (ZipEntry) entries.nextElement();
                String path = entry.getName().substring(zipFilterLength);
                File outputFile = new File(mAppRoot, path);
                //Log.d(TAG, "zip asset found path:" + outputFile.getAbsolutePath());
                outputFile.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(outputFile);
                copyStreams(zip.getInputStream(entry), fos);
                fos.close();

                Runtime.getRuntime().exec("chmod 755 " + outputFile.getAbsolutePath());
                Log.d(TAG, "copy asset: " + outputFile.getAbsolutePath());

                if (outputFile.exists() && entry.getSize() == outputFile.length()
                        // && zipLastModified < outputFile.lastModified()
                        && outputFile.getName().endsWith("zip")) {

                    unzip(outputFile, outputFile.getParentFile());
                    Log.d(TAG, "zip extract path: " + outputFile.getAbsolutePath());
                    Runtime.getRuntime().exec("chmod 755 " + outputFile.getParentFile());
                }

            }

            zip.close();

        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    public static synchronized void unzip(File zipFile, File targetDirectory) throws IOException {
        int fileCount = 0;
        Log.d(TAG, "unzip zipFile: " + zipFile.getAbsolutePath() + " target: " + targetDirectory.getAbsolutePath());

        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());

                //Fixing a Zip Path Traversal Vulnerability
                //https://support.google.com/faqs/answer/9294009
                String canonicalPath = file.getCanonicalPath();
                if (!canonicalPath.startsWith(targetDirectory.getCanonicalPath())) {
                    throw new IllegalArgumentException(String.format("Found Zip Path Traversal Vulnerability with %s", canonicalPath));
                }

                File dir = ze.isDirectory() ? file : file.getParentFile();

                //Log.d(TAG, "extract file :" + file.getAbsolutePath());

                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());

                if (ze.isDirectory())
                    continue;

                fileCount++;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            zis.close();
            Log.d(TAG, "extract file count: " + fileCount);
        }
    }

    public static Vector<ZipEntry> getAssets(ZipFile zip) {
        Vector<ZipEntry> list = new Vector<ZipEntry>();
        Enumeration<?> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            if (entry.getName().startsWith(FILTER)) {
                list.add(entry);
            }
        }

        return list;
    }

    public static void copyStreams(InputStream is, FileOutputStream fos) {
        BufferedOutputStream os = null;

        try {
            byte data[] = new byte[BUFSIZE];
            int count;
            os = new BufferedOutputStream(fos, BUFSIZE);

            while ((count = is.read(data, 0, BUFSIZE)) != -1) {
                os.write(data, 0, count);
            }

            os.flush();

        } catch (IOException e) {
            Log.e(TAG, "Exception while copying: " + e);

        } finally {

            try {
                if (os != null) {
                    os.close();
                }

            } catch (IOException e2) {
                Log.e(TAG, "Exception while closing the stream: " + e2);
            }
        }
    }

    public static ArrayList<String> getFileNames
            (final String folder, final String fileNameFilterPattern, final int sort)
            throws PatternSyntaxException
    {
        ArrayList<String> data = new ArrayList<String>();
        File fileDir = new File(folder);
        if(!fileDir.exists() || !fileDir.isDirectory()){
            return null;
        }

        String[] files = fileDir.list();

        if (files == null || files.length == 0){
            return null;
        }

        for (int i = 0; i < files.length; i++) {
            if (fileNameFilterPattern == null ||
                    files[i].matches(fileNameFilterPattern))
                data.add(files[i]);
        }

        if (data.size() == 0)
            return null;

        if (sort != 0)
        {
            Collections.sort(data, String.CASE_INSENSITIVE_ORDER);
            if (sort < 0)
                Collections.reverse(data);
        }

        return data;
    }

    /* -------------------------------NEW METHOD---------------------------------------- */

    public static boolean deleteFolderRecursively(File file) {
        try {
            boolean res = true;

            for (File childFile : file.listFiles()) {
                if (childFile.isDirectory()) {
                    res &= deleteFolderRecursively(childFile);
                } else {
                    res &= childFile.delete();
                }
            }
            res &= file.delete();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            boolean res = true;

            if (files == null || files.length == 0) {
                //If it's a file, it won't have any assets "inside" it.
                res &= copyAsset(assetManager,
                        fromAssetPath,
                        toPath);
            } else {
                new File(toPath).mkdirs();
                for (String file : files)
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
