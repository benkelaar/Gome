/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

//#ifndef JSR75
import javax.microedition.rms.RecordStoreException; //#endif

import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.IndexEntry;
import com.indigonauts.gome.io.URLFileEntry;

public class FileImporter extends Fetcher {
  private DownloadCallback callback;
  private FileEntry selectedFile;

  public FileImporter(DownloadCallback callback, FileEntry selectedFile) {
    super(selectedFile);
    this.callback = callback;
    this.selectedFile = selectedFile;
  }

  protected void download() throws IOException {
    if (selectedFile instanceof IndexEntry) {
      Vector fileList = IOManager.singleton.getFileList(selectedFile.getUrl(), this);
      Enumeration allFiles = fileList.elements();
      while (allFiles.hasMoreElements() && status != TERMINATED) {
        Object toDownload = allFiles.nextElement();
        if (toDownload instanceof URLFileEntry) {
          downloadFile(((URLFileEntry) toDownload).getUrl());
        }
      }
    } else
      downloadFile(selectedFile.getUrl());
  }

  /**
   * @throws IOException
   */
  private void downloadFile(String fileUrl) throws IOException {

    //#if JSR75
    new IOException("ui.error.recordStore"); // TODO: implement
    //#else
    //#  try {
    //#  byte[] file = IOManager.singleton.loadFile(fileUrl, this);
    //#  String name = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    //#  gauge.setLabel(name);
    //#  IOManager.singleton.saveLocalStore(name, file);
    //#  } catch (RecordStoreException e) {
    //#  throw new IOException("ui.error.recordStore"); //$NON-NLS-1$
    //#  }
    //#endif
  }

  protected void downloadFinished() {
    callback.done();
  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}
