package com.bosphere.filelogger;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.bosphere.filelogger.FLConst.Level.D;
import static com.bosphere.filelogger.FLConst.Level.E;
import static com.bosphere.filelogger.FLConst.Level.G;
import static com.bosphere.filelogger.FLConst.Level.I;
import static com.bosphere.filelogger.FLConst.Level.V;
import static com.bosphere.filelogger.FLConst.Level.W;
import static com.bosphere.filelogger.FLConst.Level.X;

/**
 * Created by yangbo on 22/9/17.
 */

public class FL {

    private volatile static boolean sEnabled;
    private volatile static FLConfig sConfig;
    volatile static FLGameConfig sGameConfig;

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static String getLogFilePath() {
        return sConfig.builder.dirPath;
    }

    public static Boolean isGameFile(String fileName) {
        String[] splitList = fileName.split("_");
        return splitList.length == 3 && splitList[2].matches("\\d*");
    }

    public static void init(Context context) {
        init(new FLConfig.Builder(context).build());
    }

    public static void init(FLConfig config) {
        sConfig = config;
    }

    public static void setGameConfig(FLGameConfig gameConfig) {
        sGameConfig = gameConfig;
    }
    public static void v(String fmt, Object... args) {
        v(null, fmt, args);
    }

    public static void x(String fmt, Object... args) {
        x(null, fmt, args);
    }

    public static void x(String tag, String fmt, Object... args) {
        log(X, tag, FLUtil.format(fmt, args));
    }

    public static void g(String fmt, Object... args) {
        g(null, fmt, args);
    }

    public static void g(String tag, String fmt, Object... args) {
        log(G, tag, FLUtil.format(fmt, args));
    }

    public static void v(String tag, String fmt, Object... args) {
        log(V, tag, FLUtil.format(fmt, args));
    }

    public static void d(String fmt, Object... args) {
        d(null, fmt, args);
    }

    public static void d(String tag, String fmt, Object... args) {
        log(D, tag, FLUtil.format(fmt, args));
    }

    public static void i(String fmt, Object... args) {
        log(I, null, FLUtil.format(fmt, args));
    }

    public static void i(String tag, String fmt, Object... args) {
        log(I, tag, FLUtil.format(fmt, args));
    }

    public static void w(String fmt, Object... args) {
        w(null, fmt, args);
    }

    public static void w(String tag, String fmt, Object... args) {
        log(W, tag, FLUtil.format(fmt, args));
    }

    public static void e(String fmt, Object... args) {
        e((String) null, fmt, args);
    }

    public static void e(String tag, String fmt, Object... args) {
        log(E, tag, FLUtil.format(fmt, args));
    }

    public static void e(Throwable tr) {
        e(null, tr);
    }

    public static void e(String tag, Throwable tr) {
        e(tag, tr, null);
    }

    public static void e(Throwable tr, String fmt, Object... args) {
        e(null, tr, fmt, args);
    }

    public static void e(String tag, Throwable tr, String fmt, Object... args) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(fmt)) {
            sb.append(FLUtil.format(fmt, args));
            sb.append("\n");
        }
        if (tr != null) {
            sb.append(Log.getStackTraceString(tr));
        }
        log(E, tag, sb.toString());
    }

    private static void log(int level, String tag, String log) {
        if (!sEnabled) {
            return;
        }

        ensureStatus();

        FLConfig config = sConfig;
        if (level < config.builder.minLevel) {
            return;
        }

        if (TextUtils.isEmpty(tag)) {
            tag = config.builder.defaultTag;
        }

        Loggable logger = config.builder.logger;
        if (logger != null) {
            switch (level) {
                case V:
                    logger.v(tag, log);
                    break;
                case D:
                    logger.d(tag, log);
                    break;
                case I:
                    logger.i(tag, log);
                    break;
                case W:
                    logger.w(tag, log);
                    break;
                case E:
                    logger.e(tag, log);
                    break;
                case X:
                    logger.w(tag, log);
                    break;
                case G:
                    logger.i(tag, log);
            }
        }

        if(level == FLConst.Level.G && sGameConfig == null) {
            Log.e("FL", "game-config not found ...skipping file log");
            return;
        }

        if (config.builder.logToFile && !TextUtils.isEmpty(config.builder.dirPath)) {
            long timeMs = System.currentTimeMillis();
            String fileName = config.builder.formatter.formatFileName(level);
            String line = config.builder.formatter.formatLine(timeMs, FLConst.LevelName.get(level), tag, log);
            boolean flush = level == E;
            FileLoggerService.instance().logFile(config.builder.context, fileName, config.builder.dirPath, line,
                                                 config.builder.retentionPolicy, config.builder.maxFileCount, config.builder.maxSize, flush);
        }
    }

    private static void ensureStatus() {
        if (sConfig == null) {
            throw new IllegalStateException(
                    "FileLogger is not initialized. Forgot to call FL.init()?");
        }
    }

    public static File compressAllFiles(String zipFileLocation, String zipFileName) {
        File zipFile = new File(zipFileLocation, zipFileName);
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zipOS = new ZipOutputStream(fos);
            File logDir = new File(sConfig.builder.dirPath);
            for (File file : logDir.listFiles()) {
                writeToZipFile(file, zipOS);
            }

            zipOS.close();
            fos.close();
        } catch (Exception ignored) {
        }
        return zipFile;
    }

	public static File compressFiles(String zipFileLocation, String zipFileName, List<String> filesToBeZipped) {
		File zipFile = new File(zipFileLocation, zipFileName);
		try {
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zipOS = new ZipOutputStream(fos);
			File logDir = new File(sConfig.builder.dirPath);
			for (String file : filesToBeZipped) {
				writeToZipFile(new File(logDir, file), zipOS);
			}

			zipOS.close();
			fos.close();
		} catch (Exception ignored) {
		}
		return zipFile;
	}

	public static boolean deleteFile(String logFileName) throws SecurityException {
        File file = new File(sConfig.builder.dirPath, logFileName);
        return file.delete();
    }

    private static void writeToZipFile(File file, ZipOutputStream zipStream) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length = fis.read(bytes);
        while (length >= 0) {
            zipStream.write(bytes, 0, length);
            length = fis.read(bytes);
        }
        zipStream.closeEntry();
        fis.close();
    }
}
