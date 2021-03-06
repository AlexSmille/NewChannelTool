package com.spearbothy.channel.reader;

import android.content.Context;
import android.text.TextUtils;

import com.spearbothy.channel.common.ApkSignBlockUtil;
import com.spearbothy.channel.common.SignatureNotFoundException;
import com.spearbothy.channel.common.log.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by mahao on 17-8-4.
 */
public class ChannelReader {

    public static String getChannelInfo(Context context) {
        // 初始化Log方式
        LogImpl.init();

        String sourceDir = context.getApplicationInfo().sourceDir;
        Log.d("获取apk路径：" + sourceDir);
        if (TextUtils.isEmpty(sourceDir)) {
            Log.e("apk路径获取失败，exit");
            return null;
        }
        File apkFile = new File(sourceDir);
        if (!apkFile.exists()) {
            Log.e("apk文件不存在，exit");
            return null;
        }
        Log.d("获取apk sign block 中id-values 集合");
        final Map<Integer, ByteBuffer> idValues = getAll(apkFile);
        if (idValues == null) {
            return null;
        }
        Log.d("获取apk sign block 中id=" + ApkSignBlockUtil.APK_SIGNATURE_CHANNEL_ID + "对应value");
        final ByteBuffer byteBuffer = idValues.get(ApkSignBlockUtil.APK_SIGNATURE_CHANNEL_ID);
        if (byteBuffer == null) {
            Log.e("渠道信息不存在");
            return null;
        }
        try {
            String channel = new String(getBytes(byteBuffer), ApkSignBlockUtil.DEFAULT_CHARSET);
            Log.i("获取渠道信息：" + channel);
            return channel;
        } catch (UnsupportedEncodingException e) {
            Log.e("获取渠道信息失败：" + e.getMessage());
        }
        Log.e("获取渠道信息失败");
        return null;
    }

    private static byte[] getBytes(final ByteBuffer byteBuffer) {
        final byte[] array = byteBuffer.array();
        final int arrayOffset = byteBuffer.arrayOffset();
        return Arrays.copyOfRange(array, arrayOffset + byteBuffer.position(),
                arrayOffset + byteBuffer.limit());
    }

    private static Map<Integer, ByteBuffer> getAll(final File apkFile) {
        Map<Integer, ByteBuffer> idValues = null;
        try {
            RandomAccessFile randomAccessFile = null;
            FileChannel fileChannel = null;
            try {
                randomAccessFile = new RandomAccessFile(apkFile, "r");
                fileChannel = randomAccessFile.getChannel();
                final ByteBuffer apkSigningBlock2 = ApkSignBlockUtil.findApkSigningBlock(fileChannel).getFirst();
                idValues = ApkSignBlockUtil.findIdValues(apkSigningBlock2);
            } catch (IOException ignore) {
            } finally {
                try {
                    if (fileChannel != null) {
                        fileChannel.close();
                    }
                } catch (IOException ignore) {
                }
                try {
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException ignore) {
                }
            }
        } catch (SignatureNotFoundException ignore) {}
        return idValues;
    }
}
