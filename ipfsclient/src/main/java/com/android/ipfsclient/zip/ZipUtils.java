package com.android.ipfsclient.zip;

import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtils {

    /**
     * 递归zip压缩一个文件夹中所有的文件
     * zip压缩
     */
    private static void compress(File file, com.file.zip.ZipOutputStream out, String basedir)
    {
        /* 判断是目录还是文件 */
        if (file.isDirectory())
        {
            compressDirectory(file, out, basedir);
        }
        else
        {
            compressFile(file, out, basedir);
        }
    }

    /** 压缩一个目录 */
    private static void compressDirectory(File dir, com.file.zip.ZipOutputStream out, String basedir)
    {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /** 压缩一个文件 */
    private static void compressFile(File file, com.file.zip.ZipOutputStream out, String basedir)
    {
        if (!file.exists()) {
            return;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            com.file.zip.ZipEntry entry = new com.file.zip.ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[1024];
            while ((count = bis.read(data)) != -1)
            {
                out.write(data, 0, count);
            }
            bis.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void zip(String srcPathName, String zipFileName)
    {
        File file = new File(srcPathName);
        File zipFile = new File(zipFileName);
        if (!file.exists()) throw new RuntimeException(srcPathName + "不存在！");
        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
            com.file.zip.ZipOutputStream out = new com.file.zip.ZipOutputStream(cos);
            out.setEncoding(System.getProperty("sun.jnu.encoding"));//设置文件名编码方式
            String basedir = "";
            compress(file, out, basedir);
            out.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }




    /**
     * 解压缩zip工具类
     * @param file 压缩文件
     * @param dir 解压缩*目录*
     * @throws IOException
     */
    public static void unzip(File file, String dir) throws IOException {

        //建立目标目录
        File parent = new File(dir);
        if (!parent.exists()) {
            parent.mkdirs();
        }
        ZipFile zipFile = new ZipFile(file, "GBK");//设置压缩文件的编码方式为GBK

        Enumeration<ZipEntry> entris = zipFile.getEntries();
        ZipEntry zipEntry = null;
        File tmpFile = null;
        BufferedOutputStream bos = null;
        InputStream is = null;
        byte[] buf = new byte[1024];
        int len = 0;
        while (entris.hasMoreElements()) {
            zipEntry = entris.nextElement();

            tmpFile = new File(dir + zipEntry.getName());
            File father = new File(tmpFile.getParent());
            if (!father.exists()) {
                father.mkdirs();
            }
            if (zipEntry.isDirectory()) {//当前文件为目录
                if (!tmpFile.exists()) {
                    tmpFile.mkdir();
                }
            } else {
                if (!tmpFile.exists()) {
                    tmpFile.createNewFile();
                }

                is = zipFile.getInputStream(zipEntry);

                bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
                while ((len = is.read(buf)) > 0) {
                    bos.write(buf, 0, len);
                }
                bos.flush();
                bos.close();
            }
        }
    }



    /**
     * 压缩tar.gz包
     * @param files    文件
     * @param destPath 目的路径
     */
    public void doTarGZ(File[] files, String destPath)
            throws IOException {
        /*
         * 定义一个TarArchiveOutputStream 对象
         */
        File tarFile = new File(destPath);
        FileOutputStream fos = new FileOutputStream(tarFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);
        byte[] buf = new byte[1024];
        for (File child : files) {
            if (child.isFile()) { // 文件
                FileInputStream fis = new FileInputStream(child);
                BufferedInputStream bis = new BufferedInputStream(fis);
                TarArchiveEntry tae = new TarArchiveEntry(child.getName());
                tae.setSize(child.length());
                taos.putArchiveEntry(tae);
                int len;
                while ((len = bis.read(buf)) > 0) {
                    taos.write(buf, 0, len);
                }
                bis.close();
                taos.flush();
                taos.closeArchiveEntry();
                continue;
            }
        }
        //建立压缩文件输出流
        FileOutputStream gzFile = new FileOutputStream(destPath + ".gz");
        //建立gzip压缩输出流
        GZIPOutputStream gzout = new GZIPOutputStream(gzFile);
        //打开需压缩文件作为文件输入流
        File file = new File(destPath);
        FileInputStream tarin = new FileInputStream(file);
        int len;
        while ((len = tarin.read(buf)) != -1) {
            gzout.write(buf, 0, len);
        }
        gzout.close();
        gzFile.close();
        tarin.close();
        //删除tar包保留tar.gz
        file.delete();
    }


    /**
     *tar.gz解压缩
     */
    public static void doUnTarGz(File srcfile, String destpath)
            throws IOException {
        byte[] buf = new byte[1024];
        FileInputStream fis = new FileInputStream(srcfile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        GzipCompressorInputStream cis = new GzipCompressorInputStream(bis);
        TarArchiveInputStream tais = new TarArchiveInputStream(cis);
        TarArchiveEntry tae = null;
        int pro = 0;
        while ((tae = tais.getNextTarEntry()) != null) {
            File f = new File(destpath + "/" + tae.getName());
            if (tae.isDirectory()) {
                f.mkdirs();
            } else {
                /*
                 * 父目录不存在则创建
                 */
                File parent = f.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(f);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int len;
                while ((len = tais.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                bos.flush();
                bos.close();
            }
        }
        tais.close();
    }


}
