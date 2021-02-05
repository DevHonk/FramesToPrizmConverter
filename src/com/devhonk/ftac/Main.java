package com.devhonk.ftac;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Objects;

public class Main {

    private static BufferedImage getScaledImage(Image srcImg, int w, int h) {

        //Create a new image with good size that contains or might contain arbitrary alpha values between and including 0.0 and 1.0.
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        //Create a device-independant object to draw the resized image
        Graphics2D g2 = resizedImg.createGraphics();

        //This could be changed, Cf. http://stackoverflow.com/documentation/java/5482/creating-images-programmatically/19498/specifying-image-rendering-quality
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        //Finally draw the source image in the Graphics2D with the desired size.
        g2.drawImage(srcImg, 0, 0, w, h, null);

        //Disposes of this graphics context and releases any system resources that it is using
        g2.dispose();

        //Return the image used to create the Graphics2D
        return resizedImg;
    }

    public static void main(String[] args) throws Exception {

        File folder = new File(args[0]);
        File fileOutput;


        PrintWriter out;//384*216
        if (!folder.exists() || !folder.isDirectory()) {
            if (!folder.mkdirs()) {
                System.err.println("Oops!");
            }
        }
        int frames = Objects.requireNonNull(folder.listFiles()).length;
        long start = System.currentTimeMillis();
        for (int i = 1; i < frames + 1; i++) {
            fileOutput = new File(String.format("output/output_%d.prm", i));
            if (!fileOutput.exists())
                fileOutput.createNewFile();
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileOutput), StandardCharsets.US_ASCII));

            File f = new File(args[0] + i + "." + args[1]);
            int idx = Integer.parseInt(f.getName().split("\\.")[0]);
            System.out.println("Processing frame " + idx + ", please wait.");
            BufferedImage frame = ImageIO.read(f);





            BufferedImage after = getScaledImage(frame, 64, 64);
            ImageIO.write(after, "PNG", new File("output/output_" + idx + ".png"));

            BitSet data = new BitSet(8);
            byte index = 7;
            Color[] frameArray = convertToColorArray(after);
            for (Color k : frameArray) {
                boolean value = (((k.getRed() + k.getGreen() + k.getBlue()) / 3) > 127);
                data.set(index, value);
                assert (byte) (value ? 1 : 0) == (data.get(index) ? 1 : 0) : String.format("TTFOWMCC, %d != %d", value ? 1 : 0, (data.get(index) ? 1 : 0));
                index--;

                if (index == -1) {
                    index = 7;
                    out.print((char) booleanToInt(
                            new boolean[]{data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5), data.get(6), data.get(7)}
                    ));
                    data = new BitSet(8);
                }

            }
            out.close();
        }
        System.out.printf("Done processing ! Took %d seconds!", (int) ((System.currentTimeMillis() - start) / 1000));

    }

    public static byte getBit(int position, byte b) {
        return (byte) (Byte.toUnsignedInt((byte) (b & (0b1000_0000 >>> position))) > 0 ? 1 : 0);
    }

    public static byte setBit(boolean bit, byte pos) {
        return (byte) ((bit ? 1 : 0) << (7 - pos));
    }


    public static int booleanToInt(boolean[] array) {
        int val = 0;
        int i = 0;
        for(boolean b : array) {

            if(b) {
                val |= (1 << i);
            }

            i++;
        }
        return val;
    }

    public static Color[] convertToColorArray(BufferedImage bi) {
        Color[] cs = new Color[bi.getWidth() * bi.getHeight()];
        for(int y = 0; y < bi.getHeight(); y++) {
            for(int x = 0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y));
                cs[(y * bi.getWidth()) + x] = c;
            }
        }
        return cs;
    }
}
