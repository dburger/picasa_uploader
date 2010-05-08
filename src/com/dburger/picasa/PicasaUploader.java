package com.dburger.picasa;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.appsforyourdomain.provisioning.UserFeed;
import com.google.gdata.data.appsforyourdomain.provisioning.UserEntry;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;

import java.io.File;
import java.net.URL;

/*
I don't think you need all of these jars but here is what I compiled / ran against.
/home/dburger/jars
dburger@dell-wildcat$ ls -l
total 956
-rw-r--r-- 1 dburger dburger  55932 2010-05-08 13:21 activation.jar
-rw------- 1 dburger dburger  29875 2010-05-08 13:21 gdata-appsforyourdomain-1.0.jar
-rw------- 1 dburger dburger  47155 2010-05-08 13:21 gdata-base-1.0.jar
-rw------- 1 dburger dburger  21590 2010-05-08 13:21 gdata-calendar-1.0.jar
-rw------- 1 dburger dburger 333521 2010-05-08 13:21 gdata-client-1.0.jar
-rw------- 1 dburger dburger  10094 2010-05-08 13:21 gdata-codesearch-1.0.jar
-rw------- 1 dburger dburger  60982 2010-05-08 13:21 gdata-photos-1.0.jar
-rw------- 1 dburger dburger  24866 2010-05-08 13:21 gdata-spreadsheet-1.0.jar
-rw-r--r-- 1 dburger dburger 356519 2010-05-08 13:21 mail.jar
 */

// compile: javac -classpath "/home/dburger/jars/*" PicasaUploader.java
// execute: java -classpath "/home/dburger/jars/*:." PicasaUploader

public class PicasaUploader {

  public static final String APP = "loosedog@gmail.com-picasa-uploader";

  public static final String BASE_URL =
      "http://picasaweb.google.com/data/feed/api/user/";

  public static void main(String[] args) throws Exception {
    if (args.length < 5) bail();

    String user = args[0];
    String pass = args[1];
    String album = args[2];
    int delay = 0;
    try {
      delay = Integer.parseInt(args[3]);
    } catch (NumberFormatException exc) {
      bail();
    }

    PicasawebService service = new PicasawebService(APP);
    service.setUserCredentials(user, pass);

    if (!albumExists(album, user, service)) createAlbum(album, user, service);

    URL albumUrl = new URL(BASE_URL + user + "/album/" + album);

    for (int i = 4; i < args.length; i++) {
      String file = args[i];
      MediaFileSource media = new MediaFileSource(new File(file), "image/jpeg");
      System.out.print("file: " + file);
      PhotoEntry retPhoto = service.insert(albumUrl, PhotoEntry.class, media);
      System.out.println(" complete.");
      Thread.sleep(delay);
    }
  }

  private static boolean albumExists(String album, String user,
      PicasawebService service) throws Exception {
    URL albumsUrl = new URL(BASE_URL + user + "?kind=album");
    UserFeed userFeed = service.getFeed(albumsUrl, UserFeed.class);
    for (UserEntry ue : userFeed.getEntries()) {
      if (ue.getTitle().getPlainText().equals(album)) return true;
    }
    return false;
  }

  private static AlbumEntry createAlbum(String album, String user,
      PicasawebService service) throws Exception {
    URL postUrl = new URL(BASE_URL + user);
    AlbumEntry entry = new AlbumEntry();
    entry.setTitle(new PlainTextConstruct(album));
    return service.insert(postUrl, entry);
  }

  public static void bail() {
    System.err.println(usage());
    System.exit(1);
  }

  public static String usage() {
    return "provide arguments: user password album delay photos*";
  }

}
