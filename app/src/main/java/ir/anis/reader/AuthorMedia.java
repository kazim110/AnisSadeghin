package ir.anis.reader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the original embedded photos without modifying the source biography or using the network. */
public final class AuthorMedia {
    private static final Pattern IMAGE = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SOURCE = Pattern.compile("\\bsrc\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    public static final class Part {
        public final String html;
        public final Bitmap image;
        public final int imageNumber;
        Part(String html, Bitmap image, int number) {
            this.html = html; this.image = image; this.imageNumber = number;
        }
    }

    public final List<Part> parts;
    public final List<Bitmap> images;

    public AuthorMedia(String biography) {
        List<Part> content = new ArrayList<>();
        List<Bitmap> photos = new ArrayList<>();
        Matcher tags = IMAGE.matcher(biography);
        int previous = 0;
        while (tags.find()) {
            if (tags.start() > previous) content.add(new Part(biography.substring(previous, tags.start()), null, 0));
            Matcher source = SOURCE.matcher(tags.group());
            if (!source.find()) throw new IllegalArgumentException("Biography image has no source");
            String uri = source.group(1);
            if (!uri.startsWith("data:image/") || !uri.contains(";base64,"))
                throw new IllegalArgumentException("Only original embedded images are supported");
            byte[] bytes = Base64.decode(uri.substring(uri.indexOf(',') + 1), Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (image == null) throw new IllegalArgumentException("Biography image could not be decoded");
            photos.add(image);
            content.add(new Part(null, image, photos.size()));
            previous = tags.end();
        }
        if (previous < biography.length()) content.add(new Part(biography.substring(previous), null, 0));
        parts = Collections.unmodifiableList(content);
        images = Collections.unmodifiableList(photos);
    }
}
