package com.csquare.lc.ms.orders.kafka.service;

import com.csquare.ms.lib.base.BaseSuper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;

@Slf4j
public abstract class BlobBaseServicesImpl extends BaseSuper {


    protected String checkFolderNameIfNotExist(String name) {
        File file = new File(name);
        if (!file.exists()) {
            return name;
        }
        return String.valueOf(file);
    }
    protected String  createFolderNameIfNotExist(String name) {
        File file = new File(name);
        if (!file.exists()) {
            return String.valueOf(file.mkdirs());
        }
        return String.valueOf(file);
    }

    protected static void showServerReply(FTPClient ftpClient, String method) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
            }
        }
    }
}
